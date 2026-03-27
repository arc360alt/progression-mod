package com.progressionmod.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * Amethyst Armor Material — between Diamond and Netherite.
 *
 * Diamond protection:  3/8/6/3 = 20 total, toughness=2
 * Netherite protection: 3/8/6/3 = 20 total, toughness=3, knockback=0.1
 * Amethyst sits between: same base protection, toughness=2.5
 */
public enum AmethystArmorMaterial implements ArmorMaterial {
    INSTANCE;

    // feet=3, legs=8, chest=7, head=3  (diamond: 3/8/6/3, netherite: 3/8/6/3)
    private static final int[] PROTECTION          = {3, 8, 7, 3};
    private static final int[] DURABILITY_MULTIPLIERS = {13, 15, 16, 11};
    private static final int   DURABILITY_BASE     = 37; // Diamond=33, Netherite=37 — we match netherite base

    @Override
    public int getDurability(ArmorItem.Type type) {
        return DURABILITY_MULTIPLIERS[type.ordinal()] * DURABILITY_BASE;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION[type.ordinal()];
    }

    @Override public int        getEnchantability()  { return 12; }
    @Override public SoundEvent getEquipSound()      { return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.AMETHYST_INGOT);
    }

    @Override public String getName()                { return "progressionmod:amethyst"; }
    @Override public float  getToughness()           { return 2.5f; }
    @Override public float  getKnockbackResistance() { return 0.05f; }
}
