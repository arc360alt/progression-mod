package com.progressionmod.quest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Every task type a quest can use, and how its progress is measured on the server.
 *
 * Most types are thin wrappers over the vanilla statistics system. The rest
 * (biomes, dimensions, Y-levels) are tracked by {@link QuestManager} in {@link QuestData}.
 */
public final class TaskTypes {

    /** Shorthand task type -> vanilla stat type. */
    public static final Map<String, String> STAT_SHORTHANDS = Map.of(
            "mine", "minecraft:mined",
            "craft", "minecraft:crafted",
            "use", "minecraft:used",
            "break", "minecraft:broken",
            "pickup", "minecraft:picked_up",
            "drop", "minecraft:dropped",
            "kill", "minecraft:killed",
            "killed_by", "minecraft:killed_by",
            "custom", "minecraft:custom"
    );

    private static final Set<String> OTHER_TYPES = Set.of(
            "stat", "advancement", "has_item", "xp_level", "biome", "biomes_visited",
            "dimension", "y_level", "effect"
    );

    private TaskTypes() {}

    public static boolean isKnown(String type) {
        return STAT_SHORTHANDS.containsKey(type) || OTHER_TYPES.contains(type);
    }

    /** Current raw progress of a task, uncapped. Compare against {@code task.count()}. */
    public static int evaluate(ServerPlayer player, QuestData data, QuestDefinition.Task task) {
        String type = task.type();
        String statType = STAT_SHORTHANDS.get(type);
        if (statType == null && type.equals("stat")) statType = task.string("stat", "minecraft:custom");
        if (statType != null) return statProgress(player, statType, task);

        return switch (type) {
            case "advancement" -> advancementDone(player, task.string("advancement", "")) ? 1 : 0;
            case "has_item" -> countItems(player.getInventory(), task.strings("item"));
            case "xp_level" -> player.experienceLevel;
            case "biome" -> visitedBiome(player, data, task.strings("biome")) ? 1 : 0;
            case "biomes_visited" -> data.visitedBiomes.size();
            case "dimension" -> data.visitedDimensions.contains(normalize(task.string("dimension", ""))) ? 1 : 0;
            case "y_level" -> reachedY(data, task) ? 1 : 0;
            case "effect" -> hasEffect(player, task.string("effect", "")) ? 1 : 0;
            default -> 0;
        };
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    private static int statProgress(ServerPlayer player, String statTypeId, QuestDefinition.Task task) {
        StatType<?> statType = BuiltInRegistries.STAT_TYPE.getValue(Identifier.parse(statTypeId));
        if (statType == null) return 0;
        long total = 0;
        for (String target : task.strings("target")) total += sumStat(player, statType, target);

        // Distance stats are stored in centimetres; quests count in blocks.
        int divisor = task.integer("divisor", 1);
        if (!task.has("divisor") && task.strings("target").stream().allMatch(t -> t.endsWith("_one_cm"))) divisor = 100;
        return (int) Math.min(Integer.MAX_VALUE, total / Math.max(1, divisor));
    }

    private static <T> long sumStat(ServerPlayer player, StatType<T> type, String target) {
        Registry<T> registry = type.getRegistry();
        long sum = 0;
        if (target.startsWith("#")) {
            TagKey<T> tag = TagKey.create(registry.key(), Identifier.parse(target.substring(1)));
            for (Holder<T> holder : registry.getTagOrEmpty(tag)) sum += player.getStats().getValue(type, holder.value());
        } else {
            Optional<T> value = registry.getOptional(Identifier.parse(target));
            if (value.isPresent()) sum += player.getStats().getValue(type, value.get());
        }
        return sum;
    }

    // ── Other trackers ───────────────────────────────────────────────────────

    private static boolean advancementDone(ServerPlayer player, String id) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(Identifier.parse(id));
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private static int countItems(Inventory inventory, List<String> targets) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && matchesAny(stack, targets)) count += stack.getCount();
        }
        return count;
    }

    public static boolean matchesAny(ItemStack stack, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                if (stack.is(TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1))))) return true;
            } else {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(target));
                if (stack.is(item)) return true;
            }
        }
        return false;
    }

    private static boolean visitedBiome(ServerPlayer player, QuestData data, List<String> targets) {
        var biomes = player.level().registryAccess().lookupOrThrow(Registries.BIOME);
        for (String target : targets) {
            if (target.startsWith("#")) {
                var tag = TagKey.create(Registries.BIOME, Identifier.parse(target.substring(1)));
                var set = biomes.get(tag);
                if (set.isPresent()) {
                    for (var holder : set.get()) {
                        if (holder.unwrapKey().map(k -> data.visitedBiomes.contains(k.identifier().toString())).orElse(false)) return true;
                    }
                }
            } else if (data.visitedBiomes.contains(normalize(target))) {
                return true;
            }
        }
        return false;
    }

    private static boolean reachedY(QuestData data, QuestDefinition.Task task) {
        if (task.has("below") && data.lowestY <= task.integer("below", 0)) return true;
        return task.has("above") && data.highestY >= task.integer("above", 0);
    }

    private static boolean hasEffect(ServerPlayer player, String id) {
        Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id));
        return effect.isPresent() && player.hasEffect(effect.get());
    }

    private static String normalize(String id) {
        return Identifier.parse(id).toString();
    }
}
