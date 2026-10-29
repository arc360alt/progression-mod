package com.progressionmod.client;

import com.progressionmod.quest.QuestDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Builds readable descriptions of quest tasks, used when a task has no {@code label}. */
@Environment(EnvType.CLIENT)
public final class QuestText {

    private QuestText() {}

    public static Component describe(QuestDefinition.Task task) {
        if (!task.label().isEmpty()) return Component.literal(task.label());
        int n = task.count();
        return switch (task.type()) {
            case "mine" -> verb("Mine", n, task.strings("target"), Kind.BLOCK);
            case "craft" -> verb("Craft", n, task.strings("target"), Kind.ITEM);
            case "use" -> verb("Use", n, task.strings("target"), Kind.ITEM);
            case "break" -> verb("Wear out", n, task.strings("target"), Kind.ITEM);
            case "pickup" -> verb("Collect", n, task.strings("target"), Kind.ITEM);
            case "drop" -> verb("Drop", n, task.strings("target"), Kind.ITEM);
            case "kill" -> verb("Defeat", n, task.strings("target"), Kind.ENTITY);
            case "killed_by" -> verb("Be defeated by", n, task.strings("target"), Kind.ENTITY);
            case "custom" -> Component.empty().append(names(task.strings("target"), Kind.STAT)).append(": " + n);
            case "stat" -> Component.literal("Statistic " + task.string("stat", "") + ": " + n);
            case "advancement" -> Component.literal("Advancement: " + pretty(task.string("advancement", "")));
            case "has_item" -> verb("Carry", n, task.strings("item"), Kind.ITEM);
            case "xp_level" -> Component.literal("Reach experience level " + n);
            case "biome" -> Component.literal("Visit ").append(names(task.strings("biome"), Kind.BIOME));
            case "biomes_visited" -> Component.literal("Visit " + n + " different biomes");
            case "dimension" -> Component.literal("Enter " + pretty(task.string("dimension", "")));
            case "y_level" -> task.has("below")
                    ? Component.literal("Reach Y level " + task.integer("below", 0) + " or lower")
                    : Component.literal("Reach Y level " + task.integer("above", 0) + " or higher");
            case "effect" -> Component.literal("Be affected by ").append(names(List.of(task.string("effect", "")), Kind.EFFECT));
            default -> Component.literal(task.type());
        };
    }

    private enum Kind { ITEM, BLOCK, ENTITY, STAT, BIOME, EFFECT }

    private static Component verb(String verb, int count, List<String> targets, Kind kind) {
        MutableComponent out = Component.literal(verb + " ");
        if (count > 1) out.append(count + " × ");
        return out.append(names(targets, kind));
    }

    private static Component names(List<String> targets, Kind kind) {
        MutableComponent out = Component.empty();
        int shown = Math.min(targets.size(), 2);
        for (int i = 0; i < shown; i++) {
            if (i > 0) out.append(" or ");
            out.append(name(targets.get(i), kind));
        }
        if (targets.size() > shown) out.append(" or similar");
        return out;
    }

    private static Component name(String target, Kind kind) {
        if (target.startsWith("#")) return Component.literal("any " + pretty(target.substring(1)));
        Identifier id = Identifier.tryParse(target);
        if (id == null) return Component.literal(target);
        return switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.getOptional(id)
                    .<Component>map(item -> new ItemStack(item).getHoverName()).orElse(Component.literal(pretty(target)));
            case BLOCK -> BuiltInRegistries.BLOCK.getOptional(id)
                    .<Component>map(block -> block.getName()).orElse(Component.literal(pretty(target)));
            case ENTITY -> BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .<Component>map(type -> type.getDescription()).orElse(Component.literal(pretty(target)));
            case STAT -> Component.translatable("stat." + id.getNamespace() + "." + id.getPath());
            case BIOME -> Component.translatable("biome." + id.getNamespace() + "." + id.getPath());
            case EFFECT -> Component.translatable("effect." + id.getNamespace() + "." + id.getPath());
        };
    }

    /** "minecraft:story/mine_stone" -> "Mine Stone", "minecraft:iron_ores" -> "Iron Ores". */
    public static String pretty(String id) {
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
        StringBuilder out = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            boolean minor = !out.isEmpty() && (word.equals("the") || word.equals("of") || word.equals("a"));
            if (!out.isEmpty()) out.append(' ');
            out.append(minor ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return out.toString();
    }
}
