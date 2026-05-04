package com.progressionmod.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Registers Endium Ore generation exclusively in The End dimension biomes.
 * The actual ore config is in data/progressionmod/worldgen/configured_feature/endium_ore.json
 * and data/progressionmod/worldgen/placed_feature/endium_ore.json
 */
public class EndiumOreGeneration {

    private static final RegistryKey<PlacedFeature> ENDIUM_ORE =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    Identifier.of("progressionmod", "endium_ore"));

    public static void register() {
        // Only spawn in The End — small islands and the main island biome
        BiomeModifications.addFeature(
                ctx -> ctx.getBiomeKey().equals(BiomeKeys.THE_END)
                    || ctx.getBiomeKey().equals(BiomeKeys.END_HIGHLANDS)
                    || ctx.getBiomeKey().equals(BiomeKeys.END_MIDLANDS)
                    || ctx.getBiomeKey().equals(BiomeKeys.END_BARRENS)
                    || ctx.getBiomeKey().equals(BiomeKeys.SMALL_END_ISLANDS),
                GenerationStep.Feature.UNDERGROUND_ORES,
                ENDIUM_ORE
        );
    }
}
