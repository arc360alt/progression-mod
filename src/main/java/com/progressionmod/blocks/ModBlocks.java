package com.progressionmod.blocks;

import com.progressionmod.ProgressionMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    /**
     * Amethyst Ore — spawns in stone layers.
     * Settings copied from iron ore, with tweaked hardness.
     */
    public static final Block AMETHYST_ORE = register("amethyst_ore",
            new AmethystOreBlock(
                    AbstractBlock.Settings.copy(Blocks.IRON_ORE)
                            .hardness(3.0f)
                            .resistance(3.0f)
            ));

    /**
     * Deepslate Amethyst Ore — deeper variant, slightly harder.
     */
    public static final Block DEEPSLATE_AMETHYST_ORE = register("deepslate_amethyst_ore",
            new AmethystOreBlock(
                    AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)
                            .hardness(4.5f)
                            .resistance(3.0f)
            ));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ProgressionMod.MOD_ID, name), block);
    }

    public static void registerBlocks() {
        ProgressionMod.LOGGER.info("Registering Amethyst Ore Blocks...");
    }
}