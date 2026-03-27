package com.progressionmod.mixin;

import com.progressionmod.RecipeUnlocker;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ItemEntity itemEntity = (ItemEntity)(Object)this;
            RecipeUnlocker.tryUnlock(serverPlayer, itemEntity.getStack().getItem());
        }
    }
}