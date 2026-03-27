package com.progressionmod.jade;

import com.progressionmod.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ProgressionToolProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier ID =
            new Identifier("progressionmod", "required_tool");

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
            Text.translatable("jade.progressionmod.required_tool",
                Text.translatable(translationKey).formatted(Formatting.YELLOW))
                .formatted(Formatting.GRAY)
        );
    }

    private String getRequiredToolKey(BlockState state) {
        if (isCopperBlock(state))    return "jade.progressionmod.tool.stone_pickaxe";
        if (isIronBlock(state))      return "jade.progressionmod.tool.copper_pickaxe";
        if (isGoldBlock(state))      return "jade.progressionmod.tool.iron_pickaxe";
        if (isDiamondBlock(state))   return "jade.progressionmod.tool.gold_pickaxe";
        if (isAmethystOre(state))    return "jade.progressionmod.tool.diamond_pickaxe";
        if (isNetheriteBlock(state)) return "jade.progressionmod.tool.amethyst_pickaxe";
        return null;
    }

    private boolean isCopperBlock(BlockState state) {
        return state.isOf(Blocks.COPPER_ORE)
            || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)
            || state.isOf(Blocks.RAW_COPPER_BLOCK);
    }

    private boolean isIronBlock(BlockState state) {
        return state.isOf(Blocks.IRON_ORE)
            || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
            || state.isOf(Blocks.RAW_IRON_BLOCK)
            || state.isOf(Blocks.LAPIS_ORE)
            || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.isOf(Blocks.LAPIS_BLOCK);
    }

    private boolean isGoldBlock(BlockState state) {
        return state.isOf(Blocks.GOLD_ORE)
            || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
            || state.isOf(Blocks.RAW_GOLD_BLOCK)
            || state.isOf(Blocks.REDSTONE_ORE)
            || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.isOf(Blocks.EMERALD_ORE)
            || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    private boolean isDiamondBlock(BlockState state) {
        return state.isOf(Blocks.DIAMOND_ORE)
            || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private boolean isAmethystOre(BlockState state) {
        return state.isOf(ModBlocks.AMETHYST_ORE)
            || state.isOf(ModBlocks.DEEPSLATE_AMETHYST_ORE);
    }

    private boolean isNetheriteBlock(BlockState state) {
        return state.isOf(Blocks.ANCIENT_DEBRIS);
    }
}