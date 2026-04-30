package com.progressionmod.items;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/**
 * Copper Tool Material — between Stone and Iron.
 * Stone-tier inverse tag; MiningDropMixin handles custom gating above that.
 */
public class CopperToolMaterial implements ToolMaterial {

    public static final CopperToolMaterial INSTANCE = new CopperToolMaterial();

    @Override public int   getDurability()            { return 200; }
    @Override public float getMiningSpeedMultiplier() { return 5.5f; }
    @Override public float getAttackDamage()          { return 1.0f; }
    @Override public int   getEnchantability()        { return 8; }

    @Override
    public TagKey<Block> getInverseTag() {
        return BlockTags.INCORRECT_FOR_STONE_TOOL;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }
}
