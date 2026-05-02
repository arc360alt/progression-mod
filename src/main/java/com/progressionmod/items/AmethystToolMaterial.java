package com.progressionmod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Amethyst Tool Material — between Diamond and Netherite.
 *
 * In 1.21.2+, ToolMaterial is a record, not an interface.
 *
 * Diamond:   durability=1561, speed=8.0, attackDmg=3.0, enchant=10
 * Netherite: durability=2031, speed=9.0, attackDmg=4.0, enchant=15
 * Amethyst:  durability=1800, speed=8.5, attackDmg=3.5, enchant=12
 */
public class AmethystToolMaterial {

    public static final TagKey<Item> REPAIRS_AMETHYST_TOOLS =
            TagKey.of(Registries.ITEM.getKey(), Identifier.of("progressionmod", "repairs_amethyst_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, // can mine same blocks as diamond
            1800,                                  // durability
            8.5f,                                  // mining speed
            3.5f,                                  // attack damage bonus
            12,                                    // enchantability
            REPAIRS_AMETHYST_TOOLS                 // repair item tag
    );
}
