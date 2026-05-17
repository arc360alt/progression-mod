package com.progressionmod.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class AmethystToolMaterial {

    public static final TagKey<Item> REPAIRS_AMETHYST_TOOLS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("progressionmod", "repairs_amethyst_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1800,
            8.5f,
            3.5f,
            12,
            REPAIRS_AMETHYST_TOOLS
    );
}
