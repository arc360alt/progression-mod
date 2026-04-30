package com.progressionmod.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

/**
 * Amethyst Armor Material — between Diamond and Netherite.
 *
 * Diamond:   3/8/6/3 = 20 total, toughness=2
 * Netherite: 3/8/6/3 = 20 total, toughness=3, knockback=0.1
 * Amethyst:  3/8/7/3 = 21 total, toughness=2.5, knockback=0.05
 */
public class AmethystArmorMaterial {

    public static final RegistryEntry<ArmorMaterial> INSTANCE = RegistryEntry.of(
        new ArmorMaterial(
            Map.of(
                ArmorItem.Type.BOOTS,       3,
                ArmorItem.Type.LEGGINGS,    8,
                ArmorItem.Type.CHESTPLATE,  7,
                ArmorItem.Type.HELMET,      3,
                ArmorItem.Type.BODY,        5
            ),
            /*enchantability*/  12,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.ofItems(ModItems.AMETHYST_INGOT),
            List.of(
                new ArmorMaterial.Layer(Identifier.of("progressionmod", "amethyst"))
            ),
            /*toughness*/       2.5f,
            /*knockbackResist*/ 0.05f
        )
    );
}