package com.progressionmod.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum CopperArmorMaterial implements ArmorMaterial {
    INSTANCE;

    // Protection values: Iron is 2/5/6/2, we do slightly less
    private static final int[] PROTECTION = {1, 4, 5, 2}; // feet, legs, chest, head
    private static final int DURABILITY_BASE = 160; // Iron=165, ours is just under
    private static final int[] DURABILITY_MULTIPLIERS = {13, 15, 16, 11};

    @Override
    public int getDurability(ArmorItem.Type type) {
        return DURABILITY_MULTIPLIERS[type.ordinal()] * DURABILITY_BASE / 100;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION[type.ordinal()];
    }

    @Override
    public int getEnchantability() { return 8; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_IRON; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(net.minecraft.item.Items.COPPER_INGOT);
    }

    @Override
    public String getName() { return "progressionmod:copper"; }

    @Override
    public float getToughness() { return 0.0f; }

    @Override
    public float getKnockbackResistance() { return 0.0f; }
}