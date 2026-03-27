package com.progressionmod.items;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.item.Items;

/**
 * Amethyst Tool Material — between Diamond and Netherite.
 *
 * Diamond: durability=1561, speed=8.0, attackDmg=3.0, mining=3, enchant=10
 * Netherite: durability=2031, speed=9.0, attackDmg=4.0, mining=4, enchant=15
 * Amethyst sits between: durable, fast, high damage.
 */
public enum AmethystToolMaterial implements ToolMaterial {
    INSTANCE;

    private static final int    MINING_LEVEL    = 4;    // Same as diamond/netherite — can mine netherite
    private static final int    ITEM_DURABILITY = 1800; // Diamond=1561, Netherite=2031
    private static final float  MINING_SPEED    = 8.5f; // Diamond=8.0, Netherite=9.0
    private static final float  ATTACK_DAMAGE   = 3.5f; // Diamond=3.0, Netherite=4.0
    private static final int    ENCHANTABILITY  = 12;   // Diamond=10, Netherite=15

    @Override public int    getDurability()              { return ITEM_DURABILITY; }
    @Override public float  getMiningSpeedMultiplier()   { return MINING_SPEED; }
    @Override public float  getAttackDamage()            { return ATTACK_DAMAGE; }
    @Override public int    getMiningLevel()             { return MINING_LEVEL; }
    @Override public int    getEnchantability()          { return ENCHANTABILITY; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.AMETHYST_INGOT);
    }
}
