package com.progressionmod.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class CopperArmorMaterial {

    public static final RegistryEntry<ArmorMaterial> INSTANCE = RegistryEntry.of(
        new ArmorMaterial(
            Map.of(
                ArmorItem.Type.BOOTS,      2,
                ArmorItem.Type.LEGGINGS,   4,
                ArmorItem.Type.CHESTPLATE, 5,
                ArmorItem.Type.HELMET,     2,
                ArmorItem.Type.BODY,       3
            ),
            /*enchantability*/   8,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            () -> Ingredient.ofItems(net.minecraft.item.Items.COPPER_INGOT),
            List.of(
                new ArmorMaterial.Layer(Identifier.of("progressionmod", "copper"))
            ),
            /*toughness*/        0.0f,
            /*knockbackResist*/  0.0f
        )
    );
}