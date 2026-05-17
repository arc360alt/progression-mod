package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import com.progressionmod.blocks.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public class ModItems {

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, name));
    }

    // ── Flint Tools ──────────────────────────────────────────────────────────
    public static final Item FLINT_AXE = register("flint_axe",
            new Item(new Item.Properties().setId(key("flint_axe"))
                    .axe(FlintToolMaterial.INSTANCE, 5.0f, -3.1f)));

    public static final Item FLINT_PICKAXE = register("flint_pickaxe",
            new Item(new Item.Properties().setId(key("flint_pickaxe"))
                    .pickaxe(FlintToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item FLINT_SHOVEL = register("flint_shovel",
            new Item(new Item.Properties().setId(key("flint_shovel"))
                    .shovel(FlintToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item FLINT_HOE = register("flint_hoe",
            new Item(new Item.Properties().setId(key("flint_hoe"))
                    .hoe(FlintToolMaterial.INSTANCE, -1.0f, -2.0f)));

    public static final Item FLINT_SWORD = register("flint_sword",
            new Item(new Item.Properties().setId(key("flint_sword"))
                    .sword(FlintToolMaterial.INSTANCE, 3.0f, -2.4f)));

    // ── Amethyst Raw/Smelted ─────────────────────────────────────────────────
    public static final Item RAW_AMETHYST = register("raw_amethyst",
            new Item(new Item.Properties().setId(key("raw_amethyst"))));

    public static final Item AMETHYST_INGOT = register("amethyst_ingot",
            new Item(new Item.Properties().setId(key("amethyst_ingot"))));

    // ── Amethyst Ore Block Items ─────────────────────────────────────────────
    public static final Item AMETHYST_ORE_ITEM = register("amethyst_ore",
            new BlockItem(ModBlocks.AMETHYST_ORE,
                    new Item.Properties().setId(key("amethyst_ore"))));

    public static final Item DEEPSLATE_AMETHYST_ORE_ITEM = register("deepslate_amethyst_ore",
            new BlockItem(ModBlocks.DEEPSLATE_AMETHYST_ORE,
                    new Item.Properties().setId(key("deepslate_amethyst_ore"))));

    // ── Amethyst Tools ───────────────────────────────────────────────────────
    public static final Item AMETHYST_AXE = register("amethyst_axe",
            new Item(new Item.Properties().setId(key("amethyst_axe"))
                    .axe(AmethystToolMaterial.INSTANCE, 6.0f, -3.0f)));

    public static final Item AMETHYST_PICKAXE = register("amethyst_pickaxe",
            new Item(new Item.Properties().setId(key("amethyst_pickaxe"))
                    .pickaxe(AmethystToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item AMETHYST_SHOVEL = register("amethyst_shovel",
            new Item(new Item.Properties().setId(key("amethyst_shovel"))
                    .shovel(AmethystToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item AMETHYST_HOE = register("amethyst_hoe",
            new Item(new Item.Properties().setId(key("amethyst_hoe"))
                    .hoe(AmethystToolMaterial.INSTANCE, -3.0f, -1.0f)));

    public static final Item AMETHYST_SWORD = register("amethyst_sword",
            new Item(new Item.Properties().setId(key("amethyst_sword"))
                    .sword(AmethystToolMaterial.INSTANCE, 3.0f, -2.4f)));

    // ── Amethyst Armor ───────────────────────────────────────────────────────
    public static final Item AMETHYST_HELMET = register("amethyst_helmet",
            new Item(new Item.Properties().setId(key("amethyst_helmet"))
                    .humanoidArmor(AmethystArmorMaterial.INSTANCE, ArmorType.HELMET)
                    .durability(ArmorType.HELMET.getDurability(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_CHESTPLATE = register("amethyst_chestplate",
            new Item(new Item.Properties().setId(key("amethyst_chestplate"))
                    .humanoidArmor(AmethystArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
                    .durability(ArmorType.CHESTPLATE.getDurability(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_LEGGINGS = register("amethyst_leggings",
            new Item(new Item.Properties().setId(key("amethyst_leggings"))
                    .humanoidArmor(AmethystArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
                    .durability(ArmorType.LEGGINGS.getDurability(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_BOOTS = register("amethyst_boots",
            new Item(new Item.Properties().setId(key("amethyst_boots"))
                    .humanoidArmor(AmethystArmorMaterial.INSTANCE, ArmorType.BOOTS)
                    .durability(ArmorType.BOOTS.getDurability(AmethystArmorMaterial.BASE_DURABILITY))));

    // ── Endium Raw/Crystal ────────────────────────────────────────────────────
    public static final Item RAW_ENDIUM = register("raw_endium",
            new Item(new Item.Properties().setId(key("raw_endium"))));

    public static final Item ENDIUM_CRYSTAL = register("endium_crystal",
            new Item(new Item.Properties().setId(key("endium_crystal"))));

    // ── Endium Ore Block Item ─────────────────────────────────────────────────
    public static final Item ENDIUM_ORE_ITEM = register("endium_ore",
            new BlockItem(ModBlocks.ENDIUM_ORE,
                    new Item.Properties().setId(key("endium_ore"))));

    // ── Endium Tools ──────────────────────────────────────────────────────────
    public static final Item ENDIUM_AXE = register("endium_axe",
            new Item(new Item.Properties().setId(key("endium_axe"))
                    .axe(EndiumToolMaterial.INSTANCE, 7.0f, -3.0f)));

    public static final Item ENDIUM_PICKAXE = register("endium_pickaxe",
            new Item(new Item.Properties().setId(key("endium_pickaxe"))
                    .pickaxe(EndiumToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item ENDIUM_SHOVEL = register("endium_shovel",
            new Item(new Item.Properties().setId(key("endium_shovel"))
                    .shovel(EndiumToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item ENDIUM_HOE = register("endium_hoe",
            new Item(new Item.Properties().setId(key("endium_hoe"))
                    .hoe(EndiumToolMaterial.INSTANCE, -4.0f, -1.0f)));

    public static final Item ENDIUM_SWORD = register("endium_sword",
            new Item(new Item.Properties().setId(key("endium_sword"))
                    .sword(EndiumToolMaterial.INSTANCE, 4.0f, -2.4f)));

    // ── Endium Armor ──────────────────────────────────────────────────────────
    public static final Item ENDIUM_HELMET = register("endium_helmet",
            new Item(new Item.Properties().setId(key("endium_helmet"))
                    .humanoidArmor(EndiumArmorMaterial.INSTANCE, ArmorType.HELMET)
                    .durability(ArmorType.HELMET.getDurability(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_CHESTPLATE = register("endium_chestplate",
            new Item(new Item.Properties().setId(key("endium_chestplate"))
                    .humanoidArmor(EndiumArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
                    .durability(ArmorType.CHESTPLATE.getDurability(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_LEGGINGS = register("endium_leggings",
            new Item(new Item.Properties().setId(key("endium_leggings"))
                    .humanoidArmor(EndiumArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
                    .durability(ArmorType.LEGGINGS.getDurability(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_BOOTS = register("endium_boots",
            new Item(new Item.Properties().setId(key("endium_boots"))
                    .humanoidArmor(EndiumArmorMaterial.INSTANCE, ArmorType.BOOTS)
                    .durability(ArmorType.BOOTS.getDurability(EndiumArmorMaterial.BASE_DURABILITY))));

    // ── Helper ───────────────────────────────────────────────────────────────
    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, name), item);
    }

    public static void registerItems() {
        ProgressionMod.LOGGER.info("Registering Flint, Copper & Amethyst Tools...");
    }
}
