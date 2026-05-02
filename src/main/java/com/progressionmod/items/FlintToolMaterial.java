package com.progressionmod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Flint Tool Material — below Wood tier.
 *
 * In 1.21.2+, ToolMaterial is a record, not an interface.
 * Constructor signature (Yarn 1.21.5):
 *   ToolMaterial(TagKey<Block> inverseTag, int durability, float speed,
 *                float attackDamage, int enchantability, TagKey<Item> repairItems)
 */
public class FlintToolMaterial {

    // Tag pointing to minecraft:flint so the anvil repair works
    public static final TagKey<Item> REPAIRS_FLINT_TOOLS =
            TagKey.of(Registries.ITEM.getKey(), Identifier.of("progressionmod", "repairs_flint_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, // can mine same blocks as wood
            45,                                   // durability
            1.1f,                                 // mining speed
            0.5f,                                 // attack damage bonus
            5,                                    // enchantability
            REPAIRS_FLINT_TOOLS                   // repair item tag
    );
}
