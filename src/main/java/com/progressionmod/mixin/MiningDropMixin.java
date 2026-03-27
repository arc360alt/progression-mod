package com.progressionmod.mixin;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MiningDropMixin {

    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void preventWrongTierMining(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = player.getWorld().getBlockState(pos);
        Item held = player.getMainHandStack().getItem();
        int tier = getToolTier(held);

        if (tier < 0) return;

        if (isCopperBlock(state)    && tier < 3) { cir.setReturnValue(false); return; }
        if (isIronBlock(state)      && tier < 4) { cir.setReturnValue(false); return; }
        if (isGoldBlock(state)      && tier < 5) { cir.setReturnValue(false); return; }
        if (isDiamondBlock(state)   && tier < 6) { cir.setReturnValue(false); return; }
        if (isAmethystOre(state)    && tier < 7) { cir.setReturnValue(false); return; }
        if (isNetheriteBlock(state) && tier < 8) { cir.setReturnValue(false); return; }
    }

    private int getToolTier(Item item) {
        if (item == ModItems.FLINT_PICKAXE    || item == ModItems.FLINT_AXE
         || item == ModItems.FLINT_SHOVEL)                                       return 1;
        if (item == Items.WOODEN_PICKAXE      || item == Items.WOODEN_AXE
         || item == Items.WOODEN_SHOVEL)                                         return 2;
        if (item == Items.STONE_PICKAXE       || item == Items.STONE_AXE
         || item == Items.STONE_SHOVEL)                                          return 3;
        if (item == ModItems.COPPER_PICKAXE   || item == ModItems.COPPER_AXE
         || item == ModItems.COPPER_SHOVEL)                                      return 4;
        if (item == Items.IRON_PICKAXE        || item == Items.IRON_AXE
         || item == Items.IRON_SHOVEL)                                           return 5;
        if (item == Items.GOLDEN_PICKAXE      || item == Items.GOLDEN_AXE
         || item == Items.GOLDEN_SHOVEL)                                         return 6;
        if (item == Items.DIAMOND_PICKAXE     || item == Items.DIAMOND_AXE
         || item == Items.DIAMOND_SHOVEL)                                        return 7;
        if (item == ModItems.AMETHYST_PICKAXE || item == ModItems.AMETHYST_AXE
         || item == ModItems.AMETHYST_SHOVEL)                                    return 8;
        if (item == Items.NETHERITE_PICKAXE   || item == Items.NETHERITE_AXE
         || item == Items.NETHERITE_SHOVEL)                                      return 9;
        return -1;
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