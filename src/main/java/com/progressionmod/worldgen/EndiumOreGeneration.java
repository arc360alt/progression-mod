package com.progressionmod.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndiumOreGeneration {

    private static final ResourceKey<PlacedFeature> ENDIUM_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath("progressionmod", "endium_ore"));

    public static void register() {
        BiomeModifications.addFeature(
                ctx -> ctx.getBiomeKey().equals(Biomes.THE_END)
                    || ctx.getBiomeKey().equals(Biomes.END_HIGHLANDS)
                    || ctx.getBiomeKey().equals(Biomes.END_MIDLANDS)
                    || ctx.getBiomeKey().equals(Biomes.END_BARRENS)
                    || ctx.getBiomeKey().equals(Biomes.SMALL_END_ISLANDS),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ENDIUM_ORE
        );
    }
}
