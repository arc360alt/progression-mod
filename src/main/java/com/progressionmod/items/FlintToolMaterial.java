package com.progressionmod.items;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class FlintToolMaterial implements ToolMaterial {

    public static final FlintToolMaterial INSTANCE = new FlintToolMaterial();

    @Override public int   getDurability()            { return 45; }
    @Override public float getMiningSpeedMultiplier() { return 1.1f; }
    @Override public float getAttackDamage()          { return 0.5f; }
    @Override public int   getEnchantability()        { return 5; }

    // Replaces getMiningLevel() in 1.21.1 — defines blocks this tier CANNOT mine
    @Override
    public TagKey<Block> getInverseTag() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(net.minecraft.item.Items.FLINT);
    }
}
