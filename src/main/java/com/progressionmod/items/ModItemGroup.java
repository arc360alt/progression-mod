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
                entries.add(ModItems.FLINT_AXE);
                entries.add(ModItems.FLINT_PICKAXE);
                entries.add(ModItems.FLINT_SHOVEL);
                entries.add(ModItems.FLINT_SWORD);
                entries.add(ModItems.FLINT_HOE);
                entries.add(ModItems.COPPER_AXE);
                entries.add(ModItems.COPPER_PICKAXE);
                entries.add(ModItems.COPPER_SHOVEL);
                entries.add(ModItems.COPPER_SWORD);
                entries.add(ModItems.COPPER_HOE);
                entries.add(ModItems.COPPER_HELMET);
                entries.add(ModItems.COPPER_CHESTPLATE);
                entries.add(ModItems.COPPER_LEGGINGS);
                entries.add(ModItems.COPPER_BOOTS);
            })
            .build()
    );

    public static void registerItemGroup() {
        ProgressionMod.LOGGER.info("Registering Item Groups...");
    }
}