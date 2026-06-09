package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModItemGroup {
    public static final CreativeModeTab PROGRESSION_GROUP = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "progression_tools"),
        FabricCreativeModeTab.builder()
            .title(Component.literal("Progression Overhaul"))
            .icon(() -> new ItemStack(ModItems.FLINT_AXE))
            .displayItems((context, output) -> {
                output.accept(ModItems.FLINT_AXE);
                output.accept(ModItems.FLINT_PICKAXE);
                output.accept(ModItems.FLINT_SHOVEL);
                output.accept(ModItems.FLINT_SWORD);
                output.accept(ModItems.FLINT_HOE);
                output.accept(Items.COPPER_AXE);
                output.accept(Items.COPPER_PICKAXE);
                output.accept(Items.COPPER_SHOVEL);
                output.accept(Items.COPPER_SWORD);
                output.accept(Items.COPPER_HOE);
                output.accept(Items.COPPER_HELMET);
                output.accept(Items.COPPER_CHESTPLATE);
                output.accept(Items.COPPER_LEGGINGS);
                output.accept(Items.COPPER_BOOTS);
                output.accept(ModItems.AMETHYST_ORE_ITEM);
                output.accept(ModItems.DEEPSLATE_AMETHYST_ORE_ITEM);
                output.accept(ModItems.RAW_AMETHYST);
                output.accept(ModItems.AMETHYST_INGOT);
                output.accept(ModItems.AMETHYST_AXE);
                output.accept(ModItems.AMETHYST_PICKAXE);
                output.accept(ModItems.AMETHYST_SHOVEL);
                output.accept(ModItems.AMETHYST_SWORD);
                output.accept(ModItems.AMETHYST_HOE);
                output.accept(ModItems.AMETHYST_HELMET);
                output.accept(ModItems.AMETHYST_CHESTPLATE);
                output.accept(ModItems.AMETHYST_LEGGINGS);
                output.accept(ModItems.AMETHYST_BOOTS);
                output.accept(ModItems.ENDIUM_ORE_ITEM);
                output.accept(ModItems.RAW_ENDIUM);
                output.accept(ModItems.ENDIUM_CRYSTAL);
                output.accept(ModItems.ENDIUM_AXE);
                output.accept(ModItems.ENDIUM_PICKAXE);
                output.accept(ModItems.ENDIUM_SHOVEL);
                output.accept(ModItems.ENDIUM_HOE);
                output.accept(ModItems.ENDIUM_SWORD);
                output.accept(ModItems.ENDIUM_HELMET);
                output.accept(ModItems.ENDIUM_CHESTPLATE);
                output.accept(ModItems.ENDIUM_LEGGINGS);
                output.accept(ModItems.ENDIUM_BOOTS);
                output.accept(ModItems.ENDIUM_LOCATOR);
            })
            .build()
    );

    public static void registerItemGroup() {
        ProgressionMod.LOGGER.info("Registering Item Groups...");
    }
}
