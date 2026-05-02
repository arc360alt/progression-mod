package com.progressionmod.items;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Amethyst Armor Material — between Diamond and Netherite.
 *
 * Diamond:   3/8/6/3 = 20 total, toughness=2
 * Netherite: 3/8/6/3 = 20 total, toughness=3, knockback=0.1
 * Amethyst:  3/8/7/3 = 21 total, toughness=2.5, knockback=0.05
 *
 * The EquipmentAsset key points to assets/progressionmod/equipment/amethyst.json
 */
public class AmethystArmorMaterial {

    public static final RegistryKey<EquipmentAsset> EQUIPMENT_KEY =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of("progressionmod", "amethyst"));

    // Netherite uses 37, Diamond uses 33 — amethyst sits between at 35
    public static final int BASE_DURABILITY = 35;

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET,     3,
                    EquipmentType.CHESTPLATE, 7,
                    EquipmentType.LEGGINGS,   8,
                    EquipmentType.BOOTS,      3
            ),
            12,                                        // enchantability
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            2.5f,                                      // toughness
            0.05f,                                     // knockback resistance
            AmethystToolMaterial.REPAIRS_AMETHYST_TOOLS,
            EQUIPMENT_KEY
    );
}