package com.progressionmod.items;

import com.progressionmod.ProgressionMod;
import com.progressionmod.blocks.ModBlocks;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    private static RegistryKey<Item> key(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ProgressionMod.MOD_ID, name));
    }

    // ── Flint Tools ──────────────────────────────────────────────────────────
    public static final Item FLINT_AXE = register("flint_axe",
            new Item(new Item.Settings().registryKey(key("flint_axe"))
                    .axe(FlintToolMaterial.INSTANCE, 5.0f, -3.1f)));

    public static final Item FLINT_PICKAXE = register("flint_pickaxe",
            new Item(new Item.Settings().registryKey(key("flint_pickaxe"))
                    .pickaxe(FlintToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item FLINT_SHOVEL = register("flint_shovel",
            new Item(new Item.Settings().registryKey(key("flint_shovel"))
                    .shovel(FlintToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item FLINT_HOE = register("flint_hoe",
            new Item(new Item.Settings().registryKey(key("flint_hoe"))
                    .hoe(FlintToolMaterial.INSTANCE, -1.0f, -2.0f)));

    public static final Item FLINT_SWORD = register("flint_sword",
            new Item(new Item.Settings().registryKey(key("flint_sword"))
                    .sword(FlintToolMaterial.INSTANCE, 3.0f, -2.4f)));

    // ── Copper Tools ─────────────────────────────────────────────────────────
    public static final Item COPPER_AXE = register("copper_axe",
            new Item(new Item.Settings().registryKey(key("copper_axe"))
                    .axe(CopperToolMaterial.INSTANCE, 6.0f, -3.1f)));

    public static final Item COPPER_PICKAXE = register("copper_pickaxe",
            new Item(new Item.Settings().registryKey(key("copper_pickaxe"))
                    .pickaxe(CopperToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item COPPER_SHOVEL = register("copper_shovel",
            new Item(new Item.Settings().registryKey(key("copper_shovel"))
                    .shovel(CopperToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item COPPER_HOE = register("copper_hoe",
            new Item(new Item.Settings().registryKey(key("copper_hoe"))
                    .hoe(CopperToolMaterial.INSTANCE, -2.0f, -1.0f)));

    public static final Item COPPER_SWORD = register("copper_sword",
            new Item(new Item.Settings().registryKey(key("copper_sword"))
                    .sword(CopperToolMaterial.INSTANCE, 3.0f, -2.4f)));

    // ── Copper Armor ─────────────────────────────────────────────────────────
    public static final Item COPPER_HELMET = register("copper_helmet",
            new Item(new Item.Settings().registryKey(key("copper_helmet"))
                    .armor(CopperArmorMaterial.INSTANCE, EquipmentType.HELMET)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(CopperArmorMaterial.BASE_DURABILITY))));

    public static final Item COPPER_CHESTPLATE = register("copper_chestplate",
            new Item(new Item.Settings().registryKey(key("copper_chestplate"))
                    .armor(CopperArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE)
                    .maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(CopperArmorMaterial.BASE_DURABILITY))));

    public static final Item COPPER_LEGGINGS = register("copper_leggings",
            new Item(new Item.Settings().registryKey(key("copper_leggings"))
                    .armor(CopperArmorMaterial.INSTANCE, EquipmentType.LEGGINGS)
                    .maxDamage(EquipmentType.LEGGINGS.getMaxDamage(CopperArmorMaterial.BASE_DURABILITY))));

    public static final Item COPPER_BOOTS = register("copper_boots",
            new Item(new Item.Settings().registryKey(key("copper_boots"))
                    .armor(CopperArmorMaterial.INSTANCE, EquipmentType.BOOTS)
                    .maxDamage(EquipmentType.BOOTS.getMaxDamage(CopperArmorMaterial.BASE_DURABILITY))));

    // ── Amethyst Raw/Smelted ─────────────────────────────────────────────────
    public static final Item RAW_AMETHYST = register("raw_amethyst",
            new Item(new Item.Settings().registryKey(key("raw_amethyst"))));

    public static final Item AMETHYST_INGOT = register("amethyst_ingot",
            new Item(new Item.Settings().registryKey(key("amethyst_ingot"))));

    // ── Amethyst Ore Block Items ─────────────────────────────────────────────
    public static final Item AMETHYST_ORE_ITEM = register("amethyst_ore",
            new BlockItem(ModBlocks.AMETHYST_ORE,
                    new Item.Settings().registryKey(key("amethyst_ore"))));

    public static final Item DEEPSLATE_AMETHYST_ORE_ITEM = register("deepslate_amethyst_ore",
            new BlockItem(ModBlocks.DEEPSLATE_AMETHYST_ORE,
                    new Item.Settings().registryKey(key("deepslate_amethyst_ore"))));

    // ── Amethyst Tools ───────────────────────────────────────────────────────
    public static final Item AMETHYST_AXE = register("amethyst_axe",
            new Item(new Item.Settings().registryKey(key("amethyst_axe"))
                    .axe(AmethystToolMaterial.INSTANCE, 6.0f, -3.0f)));

    public static final Item AMETHYST_PICKAXE = register("amethyst_pickaxe",
            new Item(new Item.Settings().registryKey(key("amethyst_pickaxe"))
                    .pickaxe(AmethystToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item AMETHYST_SHOVEL = register("amethyst_shovel",
            new Item(new Item.Settings().registryKey(key("amethyst_shovel"))
                    .shovel(AmethystToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item AMETHYST_HOE = register("amethyst_hoe",
            new Item(new Item.Settings().registryKey(key("amethyst_hoe"))
                    .hoe(AmethystToolMaterial.INSTANCE, -3.0f, -1.0f)));

    public static final Item AMETHYST_SWORD = register("amethyst_sword",
            new Item(new Item.Settings().registryKey(key("amethyst_sword"))
                    .sword(AmethystToolMaterial.INSTANCE, 3.0f, -2.4f)));

    // ── Amethyst Armor ───────────────────────────────────────────────────────
    public static final Item AMETHYST_HELMET = register("amethyst_helmet",
            new Item(new Item.Settings().registryKey(key("amethyst_helmet"))
                    .armor(AmethystArmorMaterial.INSTANCE, EquipmentType.HELMET)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_CHESTPLATE = register("amethyst_chestplate",
            new Item(new Item.Settings().registryKey(key("amethyst_chestplate"))
                    .armor(AmethystArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE)
                    .maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_LEGGINGS = register("amethyst_leggings",
            new Item(new Item.Settings().registryKey(key("amethyst_leggings"))
                    .armor(AmethystArmorMaterial.INSTANCE, EquipmentType.LEGGINGS)
                    .maxDamage(EquipmentType.LEGGINGS.getMaxDamage(AmethystArmorMaterial.BASE_DURABILITY))));

    public static final Item AMETHYST_BOOTS = register("amethyst_boots",
            new Item(new Item.Settings().registryKey(key("amethyst_boots"))
                    .armor(AmethystArmorMaterial.INSTANCE, EquipmentType.BOOTS)
                    .maxDamage(EquipmentType.BOOTS.getMaxDamage(AmethystArmorMaterial.BASE_DURABILITY))));

    // ── Endium Raw/Crystal ────────────────────────────────────────────────────
    public static final Item RAW_ENDIUM = register("raw_endium",
            new Item(new Item.Settings().registryKey(key("raw_endium"))));

    public static final Item ENDIUM_CRYSTAL = register("endium_crystal",
            new Item(new Item.Settings().registryKey(key("endium_crystal"))));

    // ── Endium Ore Block Item ─────────────────────────────────────────────────
    public static final Item ENDIUM_ORE_ITEM = register("endium_ore",
            new BlockItem(ModBlocks.ENDIUM_ORE,
                    new Item.Settings().registryKey(key("endium_ore"))));

    // ── Endium Tools ──────────────────────────────────────────────────────────
    public static final Item ENDIUM_AXE = register("endium_axe",
            new Item(new Item.Settings().registryKey(key("endium_axe"))
                    .axe(EndiumToolMaterial.INSTANCE, 7.0f, -3.0f)));

    public static final Item ENDIUM_PICKAXE = register("endium_pickaxe",
            new Item(new Item.Settings().registryKey(key("endium_pickaxe"))
                    .pickaxe(EndiumToolMaterial.INSTANCE, 1.0f, -2.8f)));

    public static final Item ENDIUM_SHOVEL = register("endium_shovel",
            new Item(new Item.Settings().registryKey(key("endium_shovel"))
                    .shovel(EndiumToolMaterial.INSTANCE, 1.5f, -3.0f)));

    public static final Item ENDIUM_HOE = register("endium_hoe",
            new Item(new Item.Settings().registryKey(key("endium_hoe"))
                    .hoe(EndiumToolMaterial.INSTANCE, -4.0f, -1.0f)));

    public static final Item ENDIUM_SWORD = register("endium_sword",
            new Item(new Item.Settings().registryKey(key("endium_sword"))
                    .sword(EndiumToolMaterial.INSTANCE, 4.0f, -2.4f)));

    // ── Endium Armor ──────────────────────────────────────────────────────────
    public static final Item ENDIUM_HELMET = register("endium_helmet",
            new Item(new Item.Settings().registryKey(key("endium_helmet"))
                    .armor(EndiumArmorMaterial.INSTANCE, EquipmentType.HELMET)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_CHESTPLATE = register("endium_chestplate",
            new Item(new Item.Settings().registryKey(key("endium_chestplate"))
                    .armor(EndiumArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE)
                    .maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_LEGGINGS = register("endium_leggings",
            new Item(new Item.Settings().registryKey(key("endium_leggings"))
                    .armor(EndiumArmorMaterial.INSTANCE, EquipmentType.LEGGINGS)
                    .maxDamage(EquipmentType.LEGGINGS.getMaxDamage(EndiumArmorMaterial.BASE_DURABILITY))));

    public static final Item ENDIUM_BOOTS = register("endium_boots",
            new Item(new Item.Settings().registryKey(key("endium_boots"))
                    .armor(EndiumArmorMaterial.INSTANCE, EquipmentType.BOOTS)
                    .maxDamage(EquipmentType.BOOTS.getMaxDamage(EndiumArmorMaterial.BASE_DURABILITY))));

    // ── Helper ───────────────────────────────────────────────────────────────
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ProgressionMod.MOD_ID, name), item);
    }

    public static void registerItems() {
        ProgressionMod.LOGGER.info("Registering Flint, Copper & Amethyst Tools...");
    }
}
