package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import com.progressionmod.blocks.ModBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.ArmorItem;

public class ModItems {

    // ── Flint Tools ──────────────────────────────────────────────────────────
    public static final Item FLINT_AXE = register("flint_axe",
            new AxeItem(FlintToolMaterial.INSTANCE, 5.0f, -3.1f, new FabricItemSettings()));

    public static final Item FLINT_PICKAXE = register("flint_pickaxe",
            new PickaxeItem(FlintToolMaterial.INSTANCE, 1, -2.8f, new FabricItemSettings()));

    public static final Item FLINT_SHOVEL = register("flint_shovel",
            new ShovelItem(FlintToolMaterial.INSTANCE, 1.5f, -3.0f, new FabricItemSettings()));

    public static final Item FLINT_HOE = register("flint_hoe",
            new HoeItem(FlintToolMaterial.INSTANCE, -1, -2.0f, new FabricItemSettings()));

    public static final Item FLINT_SWORD = register("flint_sword",
            new SwordItem(FlintToolMaterial.INSTANCE, 3, -2.4f, new FabricItemSettings()));

    // ── Copper Tools ─────────────────────────────────────────────────────────
    public static final Item COPPER_AXE = register("copper_axe",
            new AxeItem(CopperToolMaterial.INSTANCE, 6.0f, -3.1f, new FabricItemSettings()));

    public static final Item COPPER_PICKAXE = register("copper_pickaxe",
            new PickaxeItem(CopperToolMaterial.INSTANCE, 1, -2.8f, new FabricItemSettings()));

    public static final Item COPPER_SHOVEL = register("copper_shovel",
            new ShovelItem(CopperToolMaterial.INSTANCE, 1.5f, -3.0f, new FabricItemSettings()));

    public static final Item COPPER_HOE = register("copper_hoe",
            new HoeItem(CopperToolMaterial.INSTANCE, -2, -1.0f, new FabricItemSettings()));

    public static final Item COPPER_SWORD = register("copper_sword",
            new SwordItem(CopperToolMaterial.INSTANCE, 3, -2.4f, new FabricItemSettings()));

    // ── Copper Armor ─────────────────────────────────────────────────────────
    public static final Item COPPER_HELMET = register("copper_helmet",
            new ArmorItem(CopperArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));

    public static final Item COPPER_CHESTPLATE = register("copper_chestplate",
            new ArmorItem(CopperArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));

    public static final Item COPPER_LEGGINGS = register("copper_leggings",
            new ArmorItem(CopperArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));

    public static final Item COPPER_BOOTS = register("copper_boots",
            new ArmorItem(CopperArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ── Amethyst Raw/Smelted ─────────────────────────────────────────────────
    public static final Item RAW_AMETHYST = register("raw_amethyst",
            new Item(new FabricItemSettings()));

    public static final Item AMETHYST_INGOT = register("amethyst_ingot",
            new Item(new FabricItemSettings()));

    // ── Amethyst Ore Block Items ─────────────────────────────────────────────
    public static final Item AMETHYST_ORE_ITEM = register("amethyst_ore",
            new BlockItem(ModBlocks.AMETHYST_ORE, new FabricItemSettings()));

    public static final Item DEEPSLATE_AMETHYST_ORE_ITEM = register("deepslate_amethyst_ore",
            new BlockItem(ModBlocks.DEEPSLATE_AMETHYST_ORE, new FabricItemSettings()));

    // ── Amethyst Tools ───────────────────────────────────────────────────────
    public static final Item AMETHYST_AXE = register("amethyst_axe",
            new AxeItem(AmethystToolMaterial.INSTANCE, 6.0f, -3.0f, new FabricItemSettings()));

    public static final Item AMETHYST_PICKAXE = register("amethyst_pickaxe",
            new PickaxeItem(AmethystToolMaterial.INSTANCE, 1, -2.8f, new FabricItemSettings()));

    public static final Item AMETHYST_SHOVEL = register("amethyst_shovel",
            new ShovelItem(AmethystToolMaterial.INSTANCE, 1.5f, -3.0f, new FabricItemSettings()));

    public static final Item AMETHYST_HOE = register("amethyst_hoe",
            new HoeItem(AmethystToolMaterial.INSTANCE, -3, -1.0f, new FabricItemSettings()));

    public static final Item AMETHYST_SWORD = register("amethyst_sword",
            new SwordItem(AmethystToolMaterial.INSTANCE, 3, -2.4f, new FabricItemSettings()));

    // ── Amethyst Armor ───────────────────────────────────────────────────────
    public static final Item AMETHYST_HELMET = register("amethyst_helmet",
            new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));

    public static final Item AMETHYST_CHESTPLATE = register("amethyst_chestplate",
            new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));

    public static final Item AMETHYST_LEGGINGS = register("amethyst_leggings",
            new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));

    public static final Item AMETHYST_BOOTS = register("amethyst_boots",
            new ArmorItem(AmethystArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ── Helper ───────────────────────────────────────────────────────────────
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(ProgressionMod.MOD_ID, name), item);
    }

    public static void registerItems() {
        ProgressionMod.LOGGER.info("Registering Flint, Copper & Amethyst Tools...");
    }
}
