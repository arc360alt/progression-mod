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
 * Endium Armor Material — above Netherite.
 *
 * Netherite: 3/8/6/3 = 20 total, toughness=3, knockback=0.1, durability=37
 * Endium:    4/10/8/4 = 26 total, toughness=5.0, knockback=0.2, durability=80
 *            (~2–2.5x better than Netherite)
 *
 * The EquipmentAsset key points to assets/progressionmod/equipment/endium.json
 */
public class EndiumArmorMaterial {

    public static final RegistryKey<EquipmentAsset> EQUIPMENT_KEY =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of("progressionmod", "endium"));

    // Netherite uses 37 — endium sits at 80 (~2.2x)
    public static final int BASE_DURABILITY = 80;

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET,     4,
                    EquipmentType.CHESTPLATE, 10,
                    EquipmentType.LEGGINGS,   8,
                    EquipmentType.BOOTS,      4
            ),
            20,                                        // enchantability
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            5.0f,                                      // toughness (netherite is 3)
            0.2f,                                      // knockback resistance (netherite is 0.1)
            EndiumToolMaterial.REPAIRS_ENDIUM_TOOLS,
            EQUIPMENT_KEY
    );
}
