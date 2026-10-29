package com.progressionmod.quest;

import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Server-side quest logic: once a second per player it records what we track
 * ourselves (biomes, dimensions, Y-levels), evaluates every available quest,
 * completes finished ones, advances the player's age, and syncs progress to the client.
 */
public final class QuestManager {

    private static final int UPDATE_INTERVAL = 20;

    /** Hash of the last progress payload sent to each player, to skip unchanged syncs. */
    private static final Map<UUID, Integer> lastSync = new HashMap<>();

    private QuestManager() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTickCount();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if ((tick + player.getId()) % UPDATE_INTERVAL == 0) update(player);
                if (tick % 10 == 0) AgeLocks.enforceEquipment(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> lastSync.remove(handler.player.getUUID()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sync(newPlayer, true));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendDefinitions(player);
                sync(player, true);
            }
        });
    }

    private static void onJoin(ServerPlayer player) {
        QuestData data = QuestData.get(player);
        if (!data.bookGiven) {
            data.bookGiven = true;
            player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUEST_BOOK), Prediction.SERVER_ONLY);
        }
        sendDefinitions(player);
        update(player);
        sync(player, true);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public static boolean isVisible(QuestDefinition quest, int age) {
        return quest.age() <= age;
    }

    public static boolean requirementsMet(QuestDefinition quest, Set<String> completed, QuestRegistry registry) {
        for (String req : quest.requires()) {
            // Requirements pointing at quests that don't exist are ignored rather than blocking forever.
            if (!completed.contains(req) && registry.get(req) != null) return false;
        }
        return true;
    }

    // ── Update loop ──────────────────────────────────────────────────────────

    public static void update(ServerPlayer player) {
        if (player.isSpectator()) return;
        QuestData data = QuestData.get(player);
        trackExploration(player, data);

        QuestRegistry registry = QuestRegistry.SERVER;
        if (registry.size() == 0) return;

        // Loop so that reaching a new age immediately checks that age's quests too.
        for (int guard = 0; guard <= Age.COUNT; guard++) {
            boolean changed = false;
            for (QuestDefinition quest : registry.all()) {
                if (!isVisible(quest, data.age) || data.completed.contains(quest.id())) continue;
                if (!requirementsMet(quest, data.completed, registry)) continue;
                if (isFinished(player, data, quest)) {
                    complete(player, data, quest, true);
                    changed = true;
                }
            }
            if (tryAdvanceAge(player, data)) continue;
            if (!changed) break;
        }
        sync(player, false);
    }

    private static boolean isFinished(ServerPlayer player, QuestData data, QuestDefinition quest) {
        for (QuestDefinition.Task task : quest.tasks()) {
            if (TaskTypes.evaluate(player, data, task) < task.count()) return false;
        }
        return true;
    }

    private static void trackExploration(ServerPlayer player, QuestData data) {
        BlockPos pos = player.blockPosition();
        player.level().getBiome(pos).unwrapKey().ifPresent(key -> {
            if (data.visitedBiomes.add(key.identifier().toString())) {
                player.getStats().setValue(player, Stats.CUSTOM.get(ModStats.BIOMES_VISITED), data.visitedBiomes.size());
            }
        });
        data.visitedDimensions.add(player.level().dimension().identifier().toString());
        data.lowestY = Math.min(data.lowestY, pos.getY());
        data.highestY = Math.max(data.highestY, pos.getY());
    }

    // ── Completion & ages ────────────────────────────────────────────────────

    public static void complete(ServerPlayer player, QuestData data, QuestDefinition quest, boolean announce) {
        if (!data.completed.add(quest.id())) return;
        for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
            Item item = BuiltInRegistries.ITEM.getValue(reward.item());
            if (item != Items.AIR) player.getInventory().placeItemBackInInventory(new ItemStack(item, reward.count()), Prediction.SERVER_ONLY);
        }
        if (quest.rewardXp() > 0) player.giveExperiencePoints(quest.rewardXp());
        player.awardStat(ModStats.QUESTS_COMPLETED);
        if (quest.id().equals(data.tracked)) data.tracked = "";

        if (announce) {
            player.sendSystemMessage(Component.literal("✔ Quest complete: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(quest.title()).withStyle(ChatFormatting.WHITE)));
            ServerPlayNetworking.send(player, new QuestNetworking.EventPayload(false, quest.id(), data.age));
        }
    }

    private static boolean tryAdvanceAge(ServerPlayer player, QuestData data) {
        if (data.age >= Age.COUNT) return false;
        for (QuestDefinition quest : QuestRegistry.SERVER.forAge(data.age)) {
            if (!quest.optional() && !data.completed.contains(quest.id())) return false;
        }
        Age finished = Age.byIndex(data.age);
        data.age++;
        player.awardStat(ModStats.AGES_REACHED);
        announceAge(player, finished, data.age);
        return true;
    }

    private static void announceAge(ServerPlayer player, Age finished, int newAge) {
        Age next = Age.byIndex(newAge);
        Component title;
        Component subtitle;
        if (next != null) {
            title = Component.literal(next.displayName).withColor(next.color);
            subtitle = Component.literal("New tools & armor unlocked");
            player.sendSystemMessage(Component.literal("★ You have entered the ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(next.displayName).withColor(next.color))
                    .append(Component.literal("! Its tools and armor are now unlocked.").withStyle(ChatFormatting.GOLD)));
        } else {
            title = Component.literal("Ascended").withStyle(ChatFormatting.LIGHT_PURPLE);
            subtitle = Component.literal("Every age is complete");
            player.sendSystemMessage(Component.literal("★ You completed the " + finished.displayName
                    + " and every quest in the game. Well played!").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        ServerPlayNetworking.send(player, new QuestNetworking.EventPayload(true, "", newAge));
    }

    // ── Admin / player actions ───────────────────────────────────────────────

    public static void setAge(ServerPlayer player, int age) {
        QuestData data = QuestData.get(player);
        data.age = Math.max(0, Math.min(Age.COUNT, age));
        sync(player, true);
    }

    public static boolean forceComplete(ServerPlayer player, String questId) {
        QuestDefinition quest = QuestRegistry.SERVER.get(questId);
        if (quest == null) return false;
        complete(player, QuestData.get(player), quest, true);
        update(player);
        return true;
    }

    public static void reset(ServerPlayer player) {
        QuestData fresh = new QuestData();
        fresh.bookGiven = true;
        player.setAttached(QuestData.ATTACHMENT, fresh);
        sync(player, true);
    }

    public static void setTracked(ServerPlayer player, String questId) {
        QuestData data = QuestData.get(player);
        QuestDefinition quest = QuestRegistry.SERVER.get(questId);
        data.tracked = quest != null && isVisible(quest, data.age) && !data.completed.contains(questId) ? questId : "";
        sync(player, false);
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    public static void sendDefinitions(ServerPlayer player) {
        ServerPlayNetworking.send(player, new QuestNetworking.DefinitionsPayload(QuestRegistry.SERVER.toJson()));
    }

    public static void sync(ServerPlayer player, boolean force) {
        QuestData data = QuestData.get(player);
        QuestRegistry registry = QuestRegistry.SERVER;
        Map<String, int[]> progress = new HashMap<>();
        int hash = Objects.hash(data.age, data.completed, data.tracked);
        for (QuestDefinition quest : registry.all()) {
            if (!isVisible(quest, data.age) || data.completed.contains(quest.id())) continue;
            List<QuestDefinition.Task> tasks = quest.tasks();
            int[] values = new int[tasks.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = Math.min(tasks.get(i).count(), TaskTypes.evaluate(player, data, tasks.get(i)));
            }
            progress.put(quest.id(), values);
            hash = 31 * hash + quest.id().hashCode() * 17 + Arrays.hashCode(values);
        }

        Integer previous = lastSync.get(player.getUUID());
        if (!force && previous != null && previous == hash) return;
        lastSync.put(player.getUUID(), hash);
        ServerPlayNetworking.send(player, new QuestNetworking.ProgressPayload(
                data.age, Set.copyOf(data.completed), progress, data.tracked));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) sync(player, true);
    }
}
