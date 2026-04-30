package com.progressionmod.items;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/**
 * Amethyst Tool Material — between Diamond and Netherite.
 * Diamond-tier inverse tag; MiningDropMixin adds the extra gate for ancient debris.
 *
 * Diamond:   durability=1561, speed=8.0, attackDmg=3.0, enchant=10
 * Netherite: durability=2031, speed=9.0, attackDmg=4.0, enchant=15
 * Amethyst:  durability=1800, speed=8.5, attackDmg=3.5, enchant=12
 */
public class AmethystToolMaterial implements ToolMaterial {

    public static final AmethystToolMaterial INSTANCE = new AmethystToolMaterial();

    @Override public int   getDurability()            { return 1800; }
    @Override public float getMiningSpeedMultiplier() { return 8.5f; }
    @Override public float getAttackDamage()          { return 3.5f; }
    @Override public int   getEnchantability()        { return 12; }

    @Override
    public TagKey<Block> getInverseTag() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.AMETHYST_INGOT);
    }
}
