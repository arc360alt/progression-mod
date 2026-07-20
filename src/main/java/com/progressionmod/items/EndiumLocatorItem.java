package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.entities.EndiumMarkerEntity;
import com.progressionmod.entities.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class EndiumLocatorItem extends Item {

    private static final int SEARCH_RADIUS = 5000;
    private static final int GLOW_TICKS = 1000;
    private static final int MAX_MARKERS = 512;
    private static final int COOLDOWN_TICKS = 400; // 20 seconds
    private static final String MARKER_NAME = "endium_marker";

    record MarkerInfo(long expireAt, BlockPos orePos) {}

    public static final Map<UUID, MarkerInfo> activeMarkers = new LinkedHashMap<>();
    private static boolean didStartupCleanup = false;

    public EndiumLocatorItem(Properties props) { super(props); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        if (!level.dimension().equals(Level.END)) {
            player.sendSystemMessage(Component.literal("This item only works in The End!")
                .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        clearMarkers(serverLevel);

        List<BlockPos> ores = findEndiumOres(serverLevel, player.blockPosition());

        if (ores.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                "No Endium Ore found in loaded chunks within " + SEARCH_RADIUS + " blocks.")
                .withStyle(ChatFormatting.YELLOW));
            stack.hurtAndBreak(1, serverLevel, sp, item -> {});
            sp.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            return InteractionResult.SUCCESS;
        }

        // Sort closest-first so the 512-column cap keeps ores nearest the player.
        // Without this, C2ME loading thousands of distant chunks causes the cap to fill
        // with far-away ores that are never in entity-ticking range.
        BlockPos pPos = player.blockPosition();
        ores.sort(Comparator.comparingDouble(ore -> {
            double dx = ore.getX() - pPos.getX();
            double dz = ore.getZ() - pPos.getZ();
            return dx * dx + dz * dz;
        }));

        // One marker per XZ column; take the closest ore in each column
        Map<Long, BlockPos> columnToOre = new LinkedHashMap<>();
        for (BlockPos ore : ores) {
            columnToOre.putIfAbsent(packXZ(ore.getX(), ore.getZ()), ore);
            if (columnToOre.size() >= MAX_MARKERS) break;
        }

        long expireAt = serverLevel.getGameTime() + GLOW_TICKS;
        int spawned = 0, failed = 0;
        for (BlockPos ore : columnToOre.values()) {
            if (spawnMarker(serverLevel, ore, expireAt)) spawned++;
            else failed++;
        }

        stack.hurtAndBreak(1, serverLevel, sp, item -> {});
        sp.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        String detail = ores.size() > spawned
            ? ores.size() + " ore blocks across " + spawned + " locations"
            : spawned + " ore block(s)";
        player.sendSystemMessage(Component.literal(
            "Endium Locator: " + detail + " highlighted for 50 secconds.")
            .withStyle(ChatFormatting.LIGHT_PURPLE));

        ProgressionMod.LOGGER.info("[EndiumLocator] scan: {} ores → {} columns → spawned={} failed={}",
            ores.size(), columnToOre.size(), spawned, failed);
        return InteractionResult.SUCCESS;
    }

    private List<BlockPos> findEndiumOres(ServerLevel level, BlockPos center) {
        List<BlockPos> found = new ArrayList<>();
        int chunkRadius = SEARCH_RADIUS >> 4;
        int ccx = center.getX() >> 4, ccz = center.getZ() >> 4;
        outer:
        for (int cx = ccx - chunkRadius; cx <= ccx + chunkRadius; cx++) {
            for (int cz = ccz - chunkRadius; cz <= ccz + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        for (int y = 10; y <= 80; y++) {
                            BlockPos pos = new BlockPos(cx * 16 + lx, y, cz * 16 + lz);
                            if (chunk.getBlockState(pos).is(ModBlocks.ENDIUM_ORE))
                                found.add(pos.immutable());
                        }
                    }
                }
                if (found.size() >= MAX_MARKERS * 16) break outer;
            }
        }
        return found;
    }

    private boolean spawnMarker(ServerLevel level, BlockPos orePos, long expireAt) {
        EndiumMarkerEntity marker = new EndiumMarkerEntity(ModEntities.ENDIUM_MARKER, level);
        marker.setPos(orePos.getX() + 0.5, orePos.getY(), orePos.getZ() + 0.5);
        marker.setCustomName(Component.literal(MARKER_NAME));
        marker.setCustomNameVisible(false);
        boolean added = level.addFreshEntity(marker);
        if (added) activeMarkers.put(marker.getUUID(), new MarkerInfo(expireAt, orePos.immutable()));
        return added;
    }

    public static void tickEndLevel(ServerLevel level) {
        if (!didStartupCleanup) {
            didStartupCleanup = true;
            sweepOrphanedMarkers(level);
        }
        if (activeMarkers.isEmpty()) return;
        long now = level.getGameTime();
        activeMarkers.entrySet().removeIf(entry -> {
            MarkerInfo info = entry.getValue();
            boolean expired = now >= info.expireAt();
            boolean oreGone = !level.getBlockState(info.orePos()).is(ModBlocks.ENDIUM_ORE);
            if (expired || oreGone) {
                var entity = level.getEntity(entry.getKey());
                if (entity != null) entity.discard();
                return true;
            }
            return false;
        });
    }

    public static void clearMarkers(ServerLevel level) {
        for (UUID uuid : activeMarkers.keySet()) {
            var entity = level.getEntity(uuid);
            if (entity != null) entity.discard();
        }
        activeMarkers.clear();
        sweepOrphanedMarkers(level);
    }

    private static void sweepOrphanedMarkers(ServerLevel level) {
        level.getEntitiesOfClass(
            EndiumMarkerEntity.class,
            new AABB(-3_000_000, -64, -3_000_000, 3_000_000, 320, 3_000_000),
            e -> true
        ).forEach(EndiumMarkerEntity::discard);
    }

    private static long packXZ(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
