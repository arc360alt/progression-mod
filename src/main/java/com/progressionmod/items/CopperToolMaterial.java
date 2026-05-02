package com.progressionmod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Copper Tool Material — between Stone and Iron.
 *
 * In 1.21.2+, ToolMaterial is a record, not an interface.
 */
public class CopperToolMaterial {

    public static final TagKey<Item> REPAIRS_COPPER_TOOLS =
            TagKey.of(Registries.ITEM.getKey(), Identifier.of("progressionmod", "repairs_copper_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, // can mine same blocks as stone
            200,                                 // durability
            5.5f,                                // mining speed
            1.0f,                                // attack damage bonus
            8,                                   // enchantability
            REPAIRS_COPPER_TOOLS                 // repair item tag
    );
}
