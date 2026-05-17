package com.progressionmod.mixin;

import com.progressionmod.RecipeUnlocker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void onPickup(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemEntity itemEntity = (ItemEntity)(Object)this;
            RecipeUnlocker.tryUnlock(serverPlayer, itemEntity.getItem().getItem());
        }
    }
}
