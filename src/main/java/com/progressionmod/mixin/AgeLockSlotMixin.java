package com.progressionmod.mixin;

import com.progressionmod.quest.AgeLocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Locked items can't be taken out of chests, crafting results, furnaces, villager
 * trades or any other container. Slots of the player's own inventory are left alone
 * so a locked item that is already there can still be moved or dropped.
 */
@Mixin(Slot.class)
public class AgeLockSlotMixin {

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void blockLockedTake(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        if (self.container instanceof Inventory) return;
        if (AgeLocks.isLocked(player, self.getItem())) {
            AgeLocks.warn(player, self.getItem());
            cir.setReturnValue(false);
        }
    }
}
