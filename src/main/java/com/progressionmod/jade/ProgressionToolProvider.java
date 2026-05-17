package com.progressionmod.jade;

import com.progressionmod.blocks.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ProgressionToolProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier ID = Identifier.fromNamespaceAndPath("progressionmod", "required_tool");

    @Override
    public Identifier getUid() {
        return ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        String translationKey = getRequiredToolKey(state);
        if (translationKey == null) return;

        tooltip.add(
            Component.translatable("jade.progressionmod.required_tool",
                Component.translatable(translationKey).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY)
        );
    }

    private String getRequiredToolKey(BlockState state) {
        if (isCopperBlock(state))    return "jade.progressionmod.tool.stone_pickaxe";
        if (isIronBlock(state))      return "jade.progressionmod.tool.copper_pickaxe";
        if (isGoldBlock(state))      return "jade.progressionmod.tool.iron_pickaxe";
        if (isDiamondBlock(state))   return "jade.progressionmod.tool.gold_pickaxe";
        if (isAmethystOre(state))    return "jade.progressionmod.tool.diamond_pickaxe";
        if (isNetheriteBlock(state)) return "jade.progressionmod.tool.amethyst_pickaxe";
        if (isEndiumOre(state))      return "jade.progressionmod.tool.netherite_pickaxe";
        return null;
    }

    private boolean isCopperBlock(BlockState state) {
        return state.is(Blocks.COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.RAW_COPPER_BLOCK);
    }

    private boolean isIronBlock(BlockState state) {
        return state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.RAW_IRON_BLOCK)
            || state.is(Blocks.LAPIS_ORE)
            || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.LAPIS_BLOCK);
    }

    private boolean isGoldBlock(BlockState state) {
        return state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.RAW_GOLD_BLOCK)
            || state.is(Blocks.REDSTONE_ORE)
            || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.is(Blocks.EMERALD_ORE)
            || state.is(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    private boolean isDiamondBlock(BlockState state) {
        return state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private boolean isAmethystOre(BlockState state) {
        return state.is(ModBlocks.AMETHYST_ORE)
            || state.is(ModBlocks.DEEPSLATE_AMETHYST_ORE);
    }

    private boolean isNetheriteBlock(BlockState state) {
        return state.is(Blocks.ANCIENT_DEBRIS);
    }

    private boolean isEndiumOre(BlockState state) {
        return state.is(ModBlocks.ENDIUM_ORE);
    }
}
