package com.progressionmod.items;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class EndiumArmorMaterial {

    public static final ResourceKey<EquipmentAsset> EQUIPMENT_KEY =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("progressionmod", "endium"));

    public static final int BASE_DURABILITY = 80;

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET,     4,
                    ArmorType.CHESTPLATE, 10,
                    ArmorType.LEGGINGS,   8,
                    ArmorType.BOOTS,      4
            ),
            20,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            5.0f,
            0.2f,
            EndiumToolMaterial.REPAIRS_ENDIUM_TOOLS,
            EQUIPMENT_KEY
    );
}
