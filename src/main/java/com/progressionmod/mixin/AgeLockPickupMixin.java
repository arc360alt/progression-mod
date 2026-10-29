package com.progressionmod.mixin;

import com.progressionmod.quest.AgeLocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Items from a locked age stay on the ground. */
@Mixin(ItemEntity.class)
public class AgeLockPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void blockLockedPickup(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (AgeLocks.isLocked(player, self.getItem())) {
            AgeLocks.warn(player, self.getItem());
            ci.cancel();
        }
    }
}
