package com.progressionmod.mixin;

import com.progressionmod.items.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class GrassBreakMixin {

    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = player.getEntityWorld().getBlockState(pos);

        if (state.isOf(Blocks.SHORT_GRASS) || state.isOf(Blocks.TALL_GRASS)) {
            ItemStack held = player.getMainHandStack();
            if (held.getItem() == ModItems.FLINT_SWORD) {
                // In 1.21.1, ItemStack.damage() takes an EquipmentSlot, not a Hand
                held.damage(1, player, EquipmentSlot.MAINHAND);
            }
        }
    }
}