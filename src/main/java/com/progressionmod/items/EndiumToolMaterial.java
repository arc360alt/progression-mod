package com.progressionmod.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class EndiumToolMaterial {

    public static final TagKey<Item> REPAIRS_ENDIUM_TOOLS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("progressionmod", "repairs_endium_tools"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            5000,
            14.0f,
            8.0f,
            20,
            REPAIRS_ENDIUM_TOOLS
    );
}
