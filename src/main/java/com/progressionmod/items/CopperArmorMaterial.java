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
 * Copper Armor Material — between Leather and Iron.
 *
 * In 1.21.5, ArmorMaterial is a record in net.minecraft.item.equipment.
 * It no longer takes a Layer list; textures are resolved from the EquipmentAsset key.
 * The EquipmentAsset key points to assets/progressionmod/equipment/copper.json
 *
 * Constructor:
 *   ArmorMaterial(int baseDurability, Map<EquipmentType,Integer> defense,
 *                 int enchantability, SoundEvent equipSound,
 *                 float toughness, float knockbackResistance,
 *                 TagKey<Item> repairIngredients, RegistryKey<EquipmentAsset> assetId)
 */
public class CopperArmorMaterial {

    public static final RegistryKey<EquipmentAsset> EQUIPMENT_KEY =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of("progressionmod", "copper"));

    // Base durability multiplier (iron=15, gold=7, leather=5, copper fits ~12)
    public static final int BASE_DURABILITY = 12;

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET,     2,
                    EquipmentType.CHESTPLATE, 5,
                    EquipmentType.LEGGINGS,   4,
                    EquipmentType.BOOTS,      2
            ),
            8,                                     // enchantability
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            0.0f,                                  // toughness
            0.0f,                                  // knockback resistance
            CopperToolMaterial.REPAIRS_COPPER_TOOLS,
            EQUIPMENT_KEY
    );
}