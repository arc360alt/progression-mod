package com.progressionmod.items;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class AmethystArmorMaterial {

    public static final ResourceKey<EquipmentAsset> EQUIPMENT_KEY =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("progressionmod", "amethyst"));

    // Netherite uses 37, Diamond uses 33 — amethyst sits between at 35
    public static final int BASE_DURABILITY = 35;

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET,     3,
                    ArmorType.CHESTPLATE, 7,
                    ArmorType.LEGGINGS,   8,
                    ArmorType.BOOTS,      3
            ),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.5f,
            0.05f,
            AmethystToolMaterial.REPAIRS_AMETHYST_TOOLS,
            EQUIPMENT_KEY
    );
}
