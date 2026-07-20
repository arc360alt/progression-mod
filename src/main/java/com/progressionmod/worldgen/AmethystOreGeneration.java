package com.progressionmod.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class AmethystOreGeneration {

    private static final ResourceKey<PlacedFeature> AMETHYST_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath("progressionmod", "amethyst_ore"));

    private static final ResourceKey<PlacedFeature> DEEPSLATE_AMETHYST_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath("progressionmod", "deepslate_amethyst_ore"));

    public static void register() {
        // Stone amethyst ore disabled — only deepslate variant spawns
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                DEEPSLATE_AMETHYST_ORE
        );
    }
}
