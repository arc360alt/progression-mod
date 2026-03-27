package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup PROGRESSION_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        new Identifier(ProgressionMod.MOD_ID, "progression_tools"),
        FabricItemGroup.builder()
            .displayName(Text.literal("Progression Overhaul"))
            .icon(() -> new ItemStack(ModItems.FLINT_AXE))
            .entries((context, entries) -> {
                // Flint tools
                entries.add(ModItems.FLINT_AXE);
                entries.add(ModItems.FLINT_PICKAXE);
                entries.add(ModItems.FLINT_SHOVEL);
                entries.add(ModItems.FLINT_SWORD);
                entries.add(ModItems.FLINT_HOE);
                // Copper tools & armor
                entries.add(ModItems.COPPER_AXE);
                entries.add(ModItems.COPPER_PICKAXE);
                entries.add(ModItems.COPPER_SHOVEL);
                entries.add(ModItems.COPPER_SWORD);
                entries.add(ModItems.COPPER_HOE);
                entries.add(ModItems.COPPER_HELMET);
                entries.add(ModItems.COPPER_CHESTPLATE);
                entries.add(ModItems.COPPER_LEGGINGS);
                entries.add(ModItems.COPPER_BOOTS);
                // Amethyst materials & ores
                entries.add(ModItems.AMETHYST_ORE_ITEM);
                entries.add(ModItems.DEEPSLATE_AMETHYST_ORE_ITEM);
                entries.add(ModItems.RAW_AMETHYST);
                entries.add(ModItems.AMETHYST_INGOT);
                // Amethyst tools & armor
                entries.add(ModItems.AMETHYST_AXE);
                entries.add(ModItems.AMETHYST_PICKAXE);
                entries.add(ModItems.AMETHYST_SHOVEL);
                entries.add(ModItems.AMETHYST_SWORD);
                entries.add(ModItems.AMETHYST_HOE);
                entries.add(ModItems.AMETHYST_HELMET);
                entries.add(ModItems.AMETHYST_CHESTPLATE);
                entries.add(ModItems.AMETHYST_LEGGINGS);
                entries.add(ModItems.AMETHYST_BOOTS);
            })
            .build()
    );

    public static void registerItemGroup() {
        ProgressionMod.LOGGER.info("Registering Item Groups...");
    }
}
