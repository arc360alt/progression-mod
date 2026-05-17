package com.progressionmod.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EndiumOreBlock extends DropExperienceBlock {
    public EndiumOreBlock(BlockBehaviour.Properties properties) {
        super(UniformInt.of(3, 7), properties);
    }
}
