package com.progressionmod.mixin;

import com.progressionmod.quest.AgeLocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The smithing table / anvil result slot overrides mayPickup, so it needs its own check. */
@Mixin(targets = "net.minecraft.world.inventory.ItemCombinerMenu$3")
public class AgeLockCombinerResultMixin {

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void blockLockedResult(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        if (AgeLocks.isLocked(player, self.getItem())) {
            AgeLocks.warn(player, self.getItem());
            cir.setReturnValue(false);
        }
    }
}
