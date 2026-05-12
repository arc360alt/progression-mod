package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup PROGRESSION_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(ProgressionMod.MOD_ID, "progression_tools"),
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
                // Vanilla copper tools & armor (added in 1.21.9)
                entries.add(Items.COPPER_AXE);
                entries.add(Items.COPPER_PICKAXE);
                entries.add(Items.COPPER_SHOVEL);
                entries.add(Items.COPPER_SWORD);
                entries.add(Items.COPPER_HOE);
                entries.add(Items.COPPER_HELMET);
                entries.add(Items.COPPER_CHESTPLATE);
                entries.add(Items.COPPER_LEGGINGS);
                entries.add(Items.COPPER_BOOTS);
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
                // Endium materials & ore
                entries.add(ModItems.ENDIUM_ORE_ITEM);
                entries.add(ModItems.RAW_ENDIUM);
                entries.add(ModItems.ENDIUM_CRYSTAL);
                // Endium tools
                entries.add(ModItems.ENDIUM_AXE);
                entries.add(ModItems.ENDIUM_PICKAXE);
                entries.add(ModItems.ENDIUM_SHOVEL);
                entries.add(ModItems.ENDIUM_HOE);
                entries.add(ModItems.ENDIUM_SWORD);
                // Endium armor
                entries.add(ModItems.ENDIUM_HELMET);
                entries.add(ModItems.ENDIUM_CHESTPLATE);
                entries.add(ModItems.ENDIUM_LEGGINGS);
                entries.add(ModItems.ENDIUM_BOOTS);
            })
            .build()
    );

    public static void registerItemGroup() {
        ProgressionMod.LOGGER.info("Registering Item Groups...");
    }
}
