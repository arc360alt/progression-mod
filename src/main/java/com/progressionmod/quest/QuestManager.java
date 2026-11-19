package com.progressionmod.quest;

import com.progressionmod.ModConfig;
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
 *
 * <p>When {@link ModConfig#shareQuestProgress} is on (the default), a quest completed by one
 * player is completed for everyone: online players get it straight away, and everyone else
 * catches up from {@link SharedQuestProgress} when they next join.
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
        if (sharingEnabled()) data = mergeShared(player, data);
        trackExploration(player, data);

        QuestRegistry registry = QuestRegistry.SERVER;
        if (registry.size() == 0) return;
        if (sharingEnabled()) reportContribution(player, data);
        Team team = team(player);

        // Loop so that reaching a new age immediately checks that age's quests too.
        for (int guard = 0; guard <= Age.COUNT; guard++) {
            boolean changed = false;
            for (QuestDefinition quest : registry.all()) {
                if (!isVisible(quest, data.age) || data.completed.contains(quest.id())) continue;
                if (!requirementsMet(quest, data.completed, registry)) continue;
                if (isFinished(player, data, quest, team)) {
                    finish(player, data, quest);
                    changed = true;
                }
            }
            if (tryAdvanceAge(player, data)) continue;
            if (!changed) break;
        }
        sync(player, false);
    }

    private static boolean isFinished(ServerPlayer player, QuestData data, QuestDefinition quest, Team team) {
        int[] values = taskValues(player, data, quest, team);
        for (int i = 0; i < values.length; i++) {
            if (values[i] < quest.tasks().get(i).count()) return false;
        }
        return true;
    }

    // ── Team progress ────────────────────────────────────────────────────────

    /**
     * Snapshot of what teammates have done, used to combine their progress with a player's own.
     * {@code null} when progress isn't shared.
     */
    private record Team(SharedQuestProgress shared, QuestData exploration, String self) {}

    private static Team team(ServerPlayer player) {
        if (!sharingEnabled()) return null;
        SharedQuestProgress shared = SharedQuestProgress.get(player.level().getServer());
        return new Team(shared, shared.explorationView(), player.getUUID().toString());
    }

    /**
     * Uncapped progress of each task. Alone that is the player's own; with a team, counting tasks
     * add up every teammate's last reported value and other tasks take the best one, while
     * exploration tasks use everyone's combined biomes, dimensions and heights.
     */
    private static int[] taskValues(ServerPlayer player, QuestData data, QuestDefinition quest, Team team) {
        List<QuestDefinition.Task> tasks = quest.tasks();
        int[] values = new int[tasks.size()];
        for (int i = 0; i < values.length; i++) {
            QuestDefinition.Task task = tasks.get(i);
            values[i] = TaskTypes.evaluate(player, data, task);
            if (team != null && TaskTypes.isExploration(task)) {
                values[i] = Math.max(values[i], TaskTypes.evaluate(player, team.exploration(), task));
            }
        }
        if (team == null) return values;

        for (Map.Entry<String, Map<String, List<Integer>>> entry : team.shared().contributions().entrySet()) {
            if (entry.getKey().equals(team.self())) continue;
            List<Integer> theirs = entry.getValue().get(quest.id());
            if (theirs == null || theirs.size() != values.length) continue; // quest changed since they reported
            for (int i = 0; i < values.length; i++) {
                int other = theirs.get(i);
                values[i] = TaskTypes.isAdditive(tasks.get(i))
                        ? (int) Math.min(Integer.MAX_VALUE, (long) values[i] + other)
                        : Math.max(values[i], other);
            }
        }
        return values;
    }

    /**
     * Saves this player's own progress on every open quest (and their exploration) to the shared
     * store, so it keeps counting for teammates after they log off.
     */
    private static void reportContribution(ServerPlayer player, QuestData data) {
        Map<String, List<Integer>> values = new HashMap<>();
        for (QuestDefinition quest : QuestRegistry.SERVER.all()) {
            if (!isVisible(quest, data.age) || data.completed.contains(quest.id())) continue;
            List<Integer> own = new ArrayList<>(quest.tasks().size());
            boolean any = false;
            for (QuestDefinition.Task task : quest.tasks()) {
                int value = TaskTypes.evaluate(player, data, task);
                own.add(value);
                any |= value > 0;
            }
            if (any) values.put(quest.id(), List.copyOf(own));
        }
        SharedQuestProgress.report(player.level().getServer(), player.getUUID(), values, data);
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

    public static boolean sharingEnabled() {
        return ModConfig.get().shareQuestProgress;
    }

    /** The player finished a quest themselves: complete it for them and, when sharing, for everyone online. */
    private static void finish(ServerPlayer player, QuestData data, QuestDefinition quest) {
        if (data.completed.contains(quest.id())) return;
        String name = player.getName().getString();
        complete(player, data, quest, name, true);
        if (!sharingEnabled()) return;

        MinecraftServer server = player.level().getServer();
        SharedQuestProgress.record(server, quest.id(), name);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player) continue;
            QuestData otherData = QuestData.get(other);
            if (otherData.completed.contains(quest.id())) continue;
            complete(other, otherData, quest, name, true);
            // Their own update tick handles any age-up; this just refreshes their quest book now.
            sync(other, false);
        }
    }

    /**
     * Two-way merge between a player and the shared store: quests they completed while sharing
     * was off (or before it existed) are added to it, and quests the team completed while they
     * were away are completed for them, rewards included, with one summary message.
     * Returns the player's data, which is a fresh object if a team reset was applied.
     */
    private static QuestData mergeShared(ServerPlayer player, QuestData data) {
        MinecraftServer server = player.level().getServer();
        SharedQuestProgress shared = SharedQuestProgress.get(server);
        if (data.sharedEpoch != shared.epoch()) {
            // The team's progress was reset while this player was away.
            if (data.sharedEpoch < shared.epoch() && !data.completed.isEmpty()) {
                data = resetData(player, shared.epoch());
                player.sendSystemMessage(Component.literal("Quest progress was reset for everyone while you were away.")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                data.sharedEpoch = shared.epoch();
            }
        }

        String name = player.getName().getString();
        for (String id : data.completed) {
            if (!shared.completedBy().containsKey(id)) {
                SharedQuestProgress.record(server, id, data.completedBy.getOrDefault(id, name));
            }
        }

        QuestRegistry registry = QuestRegistry.SERVER;
        int caughtUp = 0;
        for (Map.Entry<String, String> entry : shared.completedBy().entrySet()) {
            if (data.completed.contains(entry.getKey())) continue;
            QuestDefinition quest = registry.get(entry.getKey());
            if (quest == null) continue; // removed by a data pack; kept in case it comes back
            complete(player, data, quest, entry.getValue(), false);
            caughtUp++;
        }
        if (caughtUp > 0) {
            player.sendSystemMessage(Component.literal("✔ Caught up on " + caughtUp + (caughtUp == 1 ? " quest" : " quests")
                    + " completed by other players.").withStyle(ChatFormatting.GREEN));
        }
        return data;
    }

    /**
     * Marks a quest complete for one player and gives them its rewards.
     *
     * @param completer name of the player who actually finished it
     * @param announce  send the chat message and HUD banner
     */
    public static void complete(ServerPlayer player, QuestData data, QuestDefinition quest, String completer, boolean announce) {
        if (!data.completed.add(quest.id())) return;
        data.completedBy.put(quest.id(), completer);
        for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
            Item item = BuiltInRegistries.ITEM.getValue(reward.item());
            if (item != Items.AIR) player.getInventory().placeItemBackInInventory(new ItemStack(item, reward.count()), Prediction.SERVER_ONLY);
        }
        if (quest.rewardXp() > 0) player.giveExperiencePoints(quest.rewardXp());
        player.awardStat(ModStats.QUESTS_COMPLETED);
        if (quest.id().equals(data.tracked)) data.tracked = "";

        if (announce) {
            Component message = Component.literal("✔ Quest complete: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(quest.title()).withStyle(ChatFormatting.WHITE));
            if (!completer.equals(player.getName().getString())) {
                message = Component.empty().append(message)
                        .append(Component.literal(" (by " + completer + ")").withStyle(ChatFormatting.GRAY));
            }
            player.sendSystemMessage(message);
            ServerPlayNetworking.send(player, new QuestNetworking.EventPayload(false, quest.id(), data.age, completer));
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
        ServerPlayNetworking.send(player, new QuestNetworking.EventPayload(true, "", newAge, ""));
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
        finish(player, QuestData.get(player), quest);
        update(player);
        return true;
    }

    /**
     * Resets quest progress. With shared progress on this resets the whole team (everyone online
     * now, everyone else when they next join), since one player's progress would just be
     * re-synced from the others. Returns true if the whole team was reset.
     */
    public static boolean reset(ServerPlayer player) {
        if (!sharingEnabled()) {
            resetData(player, QuestData.get(player).sharedEpoch);
            return false;
        }
        MinecraftServer server = player.level().getServer();
        int epoch = SharedQuestProgress.reset(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) resetData(p, epoch);
        return true;
    }

    private static QuestData resetData(ServerPlayer player, int epoch) {
        QuestData fresh = new QuestData();
        fresh.bookGiven = true;
        fresh.sharedEpoch = epoch;
        player.setAttached(QuestData.ATTACHMENT, fresh);
        sync(player, true);
        return fresh;
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
        Team team = team(player);
        for (QuestDefinition quest : registry.all()) {
            if (!isVisible(quest, data.age) || data.completed.contains(quest.id())) continue;
            List<QuestDefinition.Task> tasks = quest.tasks();
            int[] values = taskValues(player, data, quest, team);
            for (int i = 0; i < values.length; i++) values[i] = Math.min(tasks.get(i).count(), values[i]);
            progress.put(quest.id(), values);
            hash = 31 * hash + quest.id().hashCode() * 17 + Arrays.hashCode(values);
        }

        Integer previous = lastSync.get(player.getUUID());
        if (!force && previous != null && previous == hash) return;
        lastSync.put(player.getUUID(), hash);
        ServerPlayNetworking.send(player, new QuestNetworking.ProgressPayload(
                data.age, Set.copyOf(data.completed), progress, data.tracked, Map.copyOf(data.completedBy)));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) sync(player, true);
    }
}
