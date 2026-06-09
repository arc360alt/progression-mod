package com.progressionmod.items;

import com.progressionmod.blocks.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class EndiumLocatorItem extends Item {

    public static final String MARKER_TAG = "progressionmod_endium_marker";

    private static final int SEARCH_RADIUS = 5000;
    private static final int GLOW_TICKS = 6000; // 5 minutes
    private static final int MAX_MARKERS = 512;

    record MarkerInfo(long expireAt, BlockPos pos) {}

    // UUID of each spawned armor stand → expiry tick + block position
    public static final Map<UUID, MarkerInfo> activeMarkers = new LinkedHashMap<>();

    // Run stale-marker cleanup once after the End loads (e.g. after server restart)
    private static boolean didStartupCleanup = false;

    public EndiumLocatorItem(Properties props) {
        super(props);
    }

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

        // Wipe any markers from a previous scan
        clearMarkers(serverLevel);

        List<BlockPos> ores = findEndiumOres(serverLevel, player.blockPosition());

        if (ores.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                "No Endium Ore found in loaded chunks within " + SEARCH_RADIUS + " blocks.")
                .withStyle(ChatFormatting.YELLOW));
            stack.hurtAndBreak(1, serverLevel, sp, item -> {});
            return InteractionResult.SUCCESS;
        }

        long expireAt = serverLevel.getGameTime() + GLOW_TICKS;
        int toSpawn = Math.min(ores.size(), MAX_MARKERS);
        for (int i = 0; i < toSpawn; i++) {
            spawnMarker(serverLevel, ores.get(i), expireAt);
        }

        stack.hurtAndBreak(1, serverLevel, sp, item -> {});

        String countMsg = ores.size() > MAX_MARKERS
            ? ores.size() + " found, showing nearest " + MAX_MARKERS
            : String.valueOf(ores.size());
        player.sendSystemMessage(Component.literal(
            "Endium Locator: " + countMsg + " ore block(s) highlighted for 5 minutes.")
            .withStyle(ChatFormatting.LIGHT_PURPLE));

        return InteractionResult.SUCCESS;
    }

    // Scans only already-loaded chunks — no chunk loading triggered.
    private List<BlockPos> findEndiumOres(ServerLevel level, BlockPos center) {
        List<BlockPos> found = new ArrayList<>();
        int chunkRadius = SEARCH_RADIUS >> 4;
        int ccx = center.getX() >> 4;
        int ccz = center.getZ() >> 4;

        outer:
        for (int cx = ccx - chunkRadius; cx <= ccx + chunkRadius; cx++) {
            for (int cz = ccz - chunkRadius; cz <= ccz + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        for (int y = 10; y <= 80; y++) {
                            BlockPos pos = new BlockPos(cx * 16 + lx, y, cz * 16 + lz);
                            if (chunk.getBlockState(pos).is(ModBlocks.ENDIUM_ORE)) {
                                found.add(pos.immutable());
                            }
                        }
                    }
                }
                if (found.size() >= MAX_MARKERS * 4) break outer;
            }
        }
        return found;
    }

    private void spawnMarker(ServerLevel level, BlockPos pos, long expireAt) {
        ArmorStand stand = new ArmorStand(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        stand.setInvisible(true);
        // stand.setMarker(false);
        stand.setNoGravity(true);
        stand.setGlowingTag(true);
        stand.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS + 200, 0, false, false));
        stand.setSilent(true);
        stand.addTag(MARKER_TAG);
        level.addFreshEntity(stand);
        activeMarkers.put(stand.getUUID(), new MarkerInfo(expireAt, pos.immutable()));
    }

    // Expires markers whose timer elapsed or whose ore block was broken.
    public static void tickEndLevel(ServerLevel level) {
        if (!didStartupCleanup) {
            didStartupCleanup = true;
            clearPersistedMarkers(level);
        }
        if (activeMarkers.isEmpty()) return;
        long now = level.getGameTime();
        activeMarkers.entrySet().removeIf(entry -> {
            MarkerInfo info = entry.getValue();
            boolean expired = now >= info.expireAt();
            boolean oreGone = !level.getBlockState(info.pos()).is(ModBlocks.ENDIUM_ORE);
            if (expired || oreGone) {
                var entity = level.getEntity(entry.getKey());
                if (entity != null) entity.discard();
                return true;
            }
            return false;
        });
    }

    // Removes all tracked markers immediately (called before a new scan).
    public static void clearMarkers(ServerLevel level) {
        for (UUID uuid : activeMarkers.keySet()) {
            var entity = level.getEntity(uuid);
            if (entity != null) entity.discard();
        }
        activeMarkers.clear();
    }

    // Called on first End tick to discard any markers that survived a server restart.
    public static void clearPersistedMarkers(ServerLevel level) {
        level.getEntitiesOfClass(
            ArmorStand.class,
            new AABB(-3_000_000, -64, -3_000_000, 3_000_000, 320, 3_000_000),
            stand -> stand.entityTags().contains(MARKER_TAG)
        ).forEach(ArmorStand::discard);
    }
}
