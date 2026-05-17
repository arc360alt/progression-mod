package com.progressionmod.blocks;

import com.progressionmod.ProgressionMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block AMETHYST_ORE = register("amethyst_ore",
            new AmethystOreBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .setId(ResourceKey.create(Registries.BLOCK,
                                    Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "amethyst_ore")))
                            .strength(3.0f, 3.0f)
            ));

    public static final Block DEEPSLATE_AMETHYST_ORE = register("deepslate_amethyst_ore",
            new AmethystOreBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                            .setId(ResourceKey.create(Registries.BLOCK,
                                    Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "deepslate_amethyst_ore")))
                            .strength(4.5f, 3.0f)
            ));

    public static final Block ENDIUM_ORE = register("endium_ore",
            new EndiumOreBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE)
                            .setId(ResourceKey.create(Registries.BLOCK,
                                    Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "endium_ore")))
                            .strength(5.0f, 6.0f)
                            .requiresCorrectToolForDrops()
            ));

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, name), block);
    }

    public static void registerBlocks() {
        ProgressionMod.LOGGER.info("Registering Amethyst Ore Blocks...");
    }
}
