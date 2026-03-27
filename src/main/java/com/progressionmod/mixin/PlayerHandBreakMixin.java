package com.progressionmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents players from breaking wood-type blocks with their bare hands.
 * Wood requires at least a Flint Axe.
 */
@Mixin(PlayerEntity.class)
public class PlayerHandBreakMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack held = player.getMainHandStack();

        boolean isUsingHand = !(held.getItem() instanceof ToolItem);

        if (isUsingHand) {
            if (state.isOf(net.minecraft.block.Blocks.OAK_LOG)
            || state.isOf(net.minecraft.block.Blocks.BIRCH_LOG)
            || state.isOf(net.minecraft.block.Blocks.SPRUCE_LOG)
            || state.isOf(net.minecraft.block.Blocks.JUNGLE_LOG)
            || state.isOf(net.minecraft.block.Blocks.ACACIA_LOG)
            || state.isOf(net.minecraft.block.Blocks.DARK_OAK_LOG)
            || state.isOf(net.minecraft.block.Blocks.MANGROVE_LOG)
            || state.isOf(net.minecraft.block.Blocks.CHERRY_LOG)
            || state.isOf(net.minecraft.block.Blocks.BAMBOO_BLOCK)
            || state.isOf(net.minecraft.block.Blocks.OAK_WOOD)
            || state.isOf(net.minecraft.block.Blocks.BIRCH_WOOD)
            || state.isOf(net.minecraft.block.Blocks.SPRUCE_WOOD)
            || state.isOf(net.minecraft.block.Blocks.JUNGLE_WOOD)
            || state.isOf(net.minecraft.block.Blocks.ACACIA_WOOD)
            || state.isOf(net.minecraft.block.Blocks.DARK_OAK_WOOD)
            || state.isOf(net.minecraft.block.Blocks.MANGROVE_WOOD)
            || state.isOf(net.minecraft.block.Blocks.CHERRY_WOOD)) {
                cir.setReturnValue(0.001f);
            }
        }
    }
}
