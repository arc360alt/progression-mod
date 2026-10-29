package com.progressionmod.quest;

import com.progressionmod.ProgressionMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

/**
 * Custom statistics added to the vanilla stats screen. Quests can use them like any
 * vanilla custom stat: {@code {"type": "custom", "target": "progressionmod:biomes_visited"}}.
 */
public final class ModStats {

    public static final Identifier QUESTS_COMPLETED = register("quests_completed");
    public static final Identifier AGES_REACHED = register("ages_reached");
    public static final Identifier BIOMES_VISITED = register("biomes_visited");

    private ModStats() {}

    private static Identifier register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, StatFormatter.DEFAULT);
        return id;
    }

    public static void register() {
        // Class load registers the stats.
    }
}
