package com.progressionmod.blocks;

import com.progressionmod.ProgressionMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ModBlocks {

    public static final Block AMETHYST_ORE = register("amethyst_ore",
            new AmethystOreBlock(
                    AbstractBlock.Settings.copy(Blocks.IRON_ORE)
                            .registryKey(RegistryKey.of(RegistryKeys.BLOCK,
                                    Identifier.of(ProgressionMod.MOD_ID, "amethyst_ore")))
                            .lootTable(Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                                    Identifier.of(ProgressionMod.MOD_ID, "blocks/amethyst_ore"))))
                            .hardness(3.0f)
                            .resistance(3.0f)
            ));

    public static final Block DEEPSLATE_AMETHYST_ORE = register("deepslate_amethyst_ore",
            new AmethystOreBlock(
                    AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)
                            .registryKey(RegistryKey.of(RegistryKeys.BLOCK,
                                    Identifier.of(ProgressionMod.MOD_ID, "deepslate_amethyst_ore")))
                            .lootTable(Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                                    Identifier.of(ProgressionMod.MOD_ID, "blocks/deepslate_amethyst_ore"))))
                            .hardness(4.5f)
                            .resistance(3.0f)
            ));

    public static final Block ENDIUM_ORE = register("endium_ore",
            new EndiumOreBlock(
                    AbstractBlock.Settings.copy(Blocks.END_STONE)
                            .registryKey(RegistryKey.of(RegistryKeys.BLOCK,
                                    Identifier.of(ProgressionMod.MOD_ID, "endium_ore")))
                            .lootTable(Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                                    Identifier.of(ProgressionMod.MOD_ID, "blocks/endium_ore"))))
                            .hardness(5.0f)
                            .resistance(6.0f)
                            .requiresTool()
            ));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ProgressionMod.MOD_ID, name), block);
    }

    public static void registerBlocks() {
        ProgressionMod.LOGGER.info("Registering Amethyst Ore Blocks...");
    }
}
