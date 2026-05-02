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

    /**
     * Amethyst Ore — spawns in stone layers.
     *
     * In 1.21.2+, AbstractBlock.Settings must have a registryKey set so the
     * block's translation key (block.progressionmod.amethyst_ore) resolves correctly.
     *
     * IMPORTANT: .copy() also copies the source block's loot table key, so we must
     * explicitly override it with .lootTable() to point at our own loot table JSON.
     * .lootTable() requires an Optional<RegistryKey<LootTable>>.
     */
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

    /**
     * Deepslate Amethyst Ore — deeper variant, slightly harder.
     */
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

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ProgressionMod.MOD_ID, name), block);
    }

    public static void registerBlocks() {
        ProgressionMod.LOGGER.info("Registering Amethyst Ore Blocks...");
    }
}