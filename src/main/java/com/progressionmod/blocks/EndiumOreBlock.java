package com.progressionmod.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.util.math.intprovider.UniformIntProvider;

/**
 * Endium Ore block — spawns exclusively on End Islands.
 * Drops Raw Endium (or itself with Silk Touch).
 */
public class EndiumOreBlock extends ExperienceDroppingBlock {
    public EndiumOreBlock(AbstractBlock.Settings settings) {
        super(UniformIntProvider.create(3, 7), settings);
    }
}
