package com.progressionmod.items;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum FlintToolMaterial implements ToolMaterial {
    INSTANCE;

    private static final int MINING_LEVEL = 1; 
    private static final int ITEM_DURABILITY = 45; 
    private static final float MINING_SPEED = 1.1f; 
    private static final float ATTACK_DAMAGE = 0.5f; 
    private static final int ENCHANTABILITY = 5; 

    @Override
    public int getDurability() {
        return ITEM_DURABILITY;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return MINING_SPEED;
    }

    @Override
    public float getAttackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    public int getMiningLevel() {
        return MINING_LEVEL;
    }

    @Override
    public int getEnchantability() {
        return ENCHANTABILITY;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(net.minecraft.item.Items.FLINT);
    }
}
