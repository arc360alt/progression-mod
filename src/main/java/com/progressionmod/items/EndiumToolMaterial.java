package com.progressionmod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Endium Tool Material — above Netherite.
 *
 * Netherite: durability=2031, speed=9.0, attackDmg=4.0, enchant=15
 * Endium:    durability=5000, speed=14.0, attackDmg=8.0, enchant=20
 *            (~2–2.5x better than Netherite across the board)
 */
public class EndiumToolMaterial {

    public static final TagKey<Item> REPAIRS_ENDIUM_TOOLS =
            TagKey.of(Registries.ITEM.getKey(), Identifier.of("progressionmod", "repairs_endium_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // can mine same blocks as netherite
            5000,                                    // durability (~2.5x netherite)
            14.0f,                                   // mining speed (~1.5x netherite)
            8.0f,                                    // attack damage bonus (2x netherite)
            20,                                      // enchantability
            REPAIRS_ENDIUM_TOOLS                     // repair item tag
    );
}
