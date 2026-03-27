package com.progressionmod.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Hooks the datapack-defined placed features into overworld biomes at runtime.
 * The actual ore configuration lives in:
 *   data/progressionmod/worldgen/configured_feature/
 *   data/progressionmod/worldgen/placed_feature/
 */
public class AmethystOreGeneration {

    private static final RegistryKey<PlacedFeature> AMETHYST_ORE =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    new Identifier("progressionmod", "amethyst_ore"));

    private static final RegistryKey<PlacedFeature> DEEPSLATE_AMETHYST_ORE =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    new Identifier("progressionmod", "deepslate_amethyst_ore"));

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                AMETHYST_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                DEEPSLATE_AMETHYST_ORE
        );
    }
}