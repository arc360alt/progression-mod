package com.progressionmod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.*;

/**
 * Team-wide quest state used when shared progress is on, stored as a persistent attachment on
 * the overworld so it is saved with the world (single player, LAN and dedicated servers alike).
 *
 * <ul>
 *   <li>{@code completedBy}: quest id to the name of the player who completed it.</li>
 *   <li>{@code contributions}: player UUID to quest id to that player's own per-task progress,
 *       as last reported. Teammates' values are combined with a player's live values, so
 *       several players can work on one quest, even while some of them are offline.</li>
 *   <li>Exploration (biomes, dimensions, lowest/highest Y): the union of every player's.</li>
 *   <li>{@code epoch}: goes up on a team reset so players who were offline get reset when they join.</li>
 * </ul>
 *
 * <p>Immutable: every change attaches a new instance, which also marks the level dirty.
 */
public record SharedQuestProgress(Map<String, String> completedBy, int epoch,
                                  Map<String, Map<String, List<Integer>>> contributions,
                                  Set<String> biomes, Set<String> dimensions, int lowestY, int highestY) {

    public static final SharedQuestProgress EMPTY = new SharedQuestProgress(
            Map.of(), 0, Map.of(), Set.of(), Set.of(), Integer.MAX_VALUE, Integer.MIN_VALUE);

    private static final Codec<Set<String>> STRING_SET = Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf);

    public static final Codec<SharedQuestProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("completed_by", Map.of())
                    .forGetter(SharedQuestProgress::completedBy),
            Codec.INT.optionalFieldOf("epoch", 0).forGetter(SharedQuestProgress::epoch),
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT.listOf()))
                    .optionalFieldOf("contributions", Map.of()).forGetter(SharedQuestProgress::contributions),
            STRING_SET.optionalFieldOf("visited_biomes", Set.of()).forGetter(SharedQuestProgress::biomes),
            STRING_SET.optionalFieldOf("visited_dimensions", Set.of()).forGetter(SharedQuestProgress::dimensions),
            Codec.INT.optionalFieldOf("lowest_y", Integer.MAX_VALUE).forGetter(SharedQuestProgress::lowestY),
            Codec.INT.optionalFieldOf("highest_y", Integer.MIN_VALUE).forGetter(SharedQuestProgress::highestY)
    ).apply(i, SharedQuestProgress::new));

    public static final AttachmentType<SharedQuestProgress> ATTACHMENT = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "shared_quest_progress"),
            builder -> builder.persistent(CODEC).initializer(() -> EMPTY));

    public SharedQuestProgress {
        completedBy = Map.copyOf(completedBy);
        contributions = Map.copyOf(contributions);
        biomes = Set.copyOf(biomes);
        dimensions = Set.copyOf(dimensions);
    }

    public static SharedQuestProgress get(MinecraftServer server) {
        return server.overworld().getAttachedOrCreate(ATTACHMENT);
    }

    private static void set(MinecraftServer server, SharedQuestProgress value) {
        server.overworld().setAttached(ATTACHMENT, value);
    }

    /**
     * Records a completion unless the quest is already in the shared set, and drops every
     * player's contribution to it. Returns true if it was new.
     */
    public static boolean record(MinecraftServer server, String questId, String playerName) {
        SharedQuestProgress s = get(server);
        if (s.completedBy.containsKey(questId)) return false;
        Map<String, String> completed = new HashMap<>(s.completedBy);
        completed.put(questId, playerName);
        Map<String, Map<String, List<Integer>>> contributions = new HashMap<>();
        for (var entry : s.contributions.entrySet()) {
            Map<String, List<Integer>> quests = new HashMap<>(entry.getValue());
            quests.remove(questId);
            contributions.put(entry.getKey(), Map.copyOf(quests));
        }
        set(server, new SharedQuestProgress(completed, s.epoch, contributions, s.biomes, s.dimensions, s.lowestY, s.highestY));
        return true;
    }

    /** Replaces one player's contributions and adds their exploration. Only re-attaches if something changed. */
    public static void report(MinecraftServer server, UUID player, Map<String, List<Integer>> values, QuestData data) {
        SharedQuestProgress s = get(server);
        String key = player.toString();
        boolean sameValues = values.equals(s.contributions.getOrDefault(key, Map.of()));
        boolean sameExploration = s.biomes.containsAll(data.visitedBiomes) && s.dimensions.containsAll(data.visitedDimensions)
                && s.lowestY <= data.lowestY && s.highestY >= data.highestY;
        if (sameValues && sameExploration) return;

        Map<String, Map<String, List<Integer>>> contributions = s.contributions;
        if (!sameValues) {
            contributions = new HashMap<>(s.contributions);
            contributions.put(key, Map.copyOf(values));
        }
        Set<String> biomes = s.biomes, dimensions = s.dimensions;
        if (!sameExploration) {
            biomes = new HashSet<>(s.biomes);
            biomes.addAll(data.visitedBiomes);
            dimensions = new HashSet<>(s.dimensions);
            dimensions.addAll(data.visitedDimensions);
        }
        set(server, new SharedQuestProgress(s.completedBy, s.epoch, contributions, biomes, dimensions,
                Math.min(s.lowestY, data.lowestY), Math.max(s.highestY, data.highestY)));
    }

    /** Clears everything shared and starts a new epoch. Returns the new epoch. */
    public static int reset(MinecraftServer server) {
        int epoch = get(server).epoch + 1;
        set(server, new SharedQuestProgress(Map.of(), epoch, Map.of(), Set.of(), Set.of(), Integer.MAX_VALUE, Integer.MIN_VALUE));
        return epoch;
    }

    /** A QuestData holding only the team's combined exploration, for evaluating exploration tasks. */
    public QuestData explorationView() {
        return QuestData.exploration(biomes, dimensions, lowestY, highestY);
    }

    public static void register() {
        // Class load registers the attachment.
    }
}
