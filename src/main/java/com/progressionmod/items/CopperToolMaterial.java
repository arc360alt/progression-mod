package com.progressionmod.items;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.item.Items;

/**
 * Copper Tool Material - Tier between Stone and Iron
 *
 * Slightly better than stone, worse than iron.
 * Can mine iron ore (which stone cannot).
 */
public enum CopperToolMaterial implements ToolMaterial {
    INSTANCE;

    private static final int MINING_LEVEL = 2;   // Same as stone
    private static final int ITEM_DURABILITY = 200; // Stone=131, Iron=250 — copper sits between
    private static final float MINING_SPEED = 5.5f; // Stone=4.0, Iron=6.0
    private static final float ATTACK_DAMAGE = 1.0f;
    private static final int ENCHANTABILITY = 8;

    @Override public int getDurability() { return ITEM_DURABILITY; }
    @Override public float getMiningSpeedMultiplier() { return MINING_SPEED; }
    @Override public float getAttackDamage() { return ATTACK_DAMAGE; }
    @Override public int getMiningLevel() { return MINING_LEVEL; }
    @Override public int getEnchantability() { return ENCHANTABILITY; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }
}
