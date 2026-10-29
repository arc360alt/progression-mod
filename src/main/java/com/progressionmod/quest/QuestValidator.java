package com.progressionmod.quest;

import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs a warning for every quest that references something that doesn't exist, so
 * typos in datapack quests show up in the server log instead of as quests that can
 * never be completed. Runs after the server starts and after every {@code /reload}.
 */
public final class QuestValidator {

    private QuestValidator() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(QuestValidator::validate);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> validate(server));
    }

    public static void validate(MinecraftServer server) {
        List<String> problems = new ArrayList<>();
        for (QuestDefinition quest : QuestRegistry.SERVER.all()) {
            String q = "Quest '" + quest.id() + "': ";
            if (!BuiltInRegistries.ITEM.containsKey(quest.icon())) problems.add(q + "unknown icon " + quest.icon());
            for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
                if (!BuiltInRegistries.ITEM.containsKey(reward.item())) problems.add(q + "unknown reward item " + reward.item());
            }
            for (String req : quest.requires()) {
                QuestDefinition other = QuestRegistry.SERVER.get(req);
                if (other == null) problems.add(q + "requires unknown quest " + req);
                else if (other.age() > quest.age()) problems.add(q + "requires '" + req + "' from a later age");
            }
            for (QuestDefinition.Task task : quest.tasks()) checkTask(server, task, q, problems);
        }
        problems.forEach(ProgressionMod.LOGGER::warn);
        if (!problems.isEmpty()) ProgressionMod.LOGGER.warn("{} problem(s) found in quest definitions", problems.size());
    }

    private static void checkTask(MinecraftServer server, QuestDefinition.Task task, String q, List<String> problems) {
        String statType = TaskTypes.STAT_SHORTHANDS.get(task.type());
        if (statType == null && task.type().equals("stat")) statType = task.string("stat", "minecraft:custom");
        if (statType != null) {
            StatType<?> type = BuiltInRegistries.STAT_TYPE.getValue(Identifier.parse(statType));
            if (type == null) {
                problems.add(q + "unknown stat type " + statType);
                return;
            }
            if (task.strings("target").isEmpty()) problems.add(q + task.type() + " task has no target");
            for (String target : task.strings("target")) checkId(type.getRegistry(), target, q, problems);
            return;
        }
        switch (task.type()) {
            case "advancement" -> {
                String id = task.string("advancement", "");
                if (server.getAdvancements().get(Identifier.parse(id)) == null) problems.add(q + "unknown advancement " + id);
            }
            case "has_item" -> task.strings("item").forEach(id -> checkId(BuiltInRegistries.ITEM, id, q, problems));
            case "effect" -> checkId(BuiltInRegistries.MOB_EFFECT, task.string("effect", ""), q, problems);
            case "biome" -> {
                Registry<?> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
                task.strings("biome").forEach(id -> checkId(biomes, id, q, problems));
            }
            case "dimension" -> {
                Identifier id = Identifier.parse(task.string("dimension", ""));
                boolean exists = server.levelKeys().stream().anyMatch(key -> key.identifier().equals(id));
                if (!exists) problems.add(q + "unknown dimension " + id);
            }
            case "y_level" -> {
                if (!task.has("below") && !task.has("above")) problems.add(q + "y_level task needs \"below\" or \"above\"");
            }
            default -> { }
        }
    }

    private static <T> void checkId(Registry<T> registry, String id, String q, List<String> problems) {
        if (id.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(id.substring(1));
            if (tagId == null || registry.get(TagKey.create(registry.key(), tagId)).isEmpty()) {
                problems.add(q + "unknown tag " + id + " in " + registry.key().identifier());
            }
            return;
        }
        Identifier parsed = Identifier.tryParse(id);
        if (parsed == null || !registry.containsKey(parsed)) {
            problems.add(q + "unknown id " + id + " in " + registry.key().identifier());
        }
    }
}
