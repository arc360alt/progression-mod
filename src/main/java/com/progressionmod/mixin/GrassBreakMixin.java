package com.progressionmod.mixin;

import com.progressionmod.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class GrassBreakMixin {

    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = player.level().getBlockState(pos);

        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() == ModItems.FLINT_SWORD) {
                held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        }
    }
}
