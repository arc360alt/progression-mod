package com.progressionmod.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class FlintToolMaterial {

    public static final TagKey<Item> REPAIRS_FLINT_TOOLS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("progressionmod", "repairs_flint_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            45,
            1.1f,
            0.5f,
            5,
            REPAIRS_FLINT_TOOLS
    );
}
