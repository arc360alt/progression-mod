package com.progressionmod.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class GoldToolMiningMixin {

    @Inject(method = "isCorrectToolForDrops", at = @At("RETURN"), cancellable = true)
    private void allowGoldToMineAnything(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        Item item = self.getItem();

        boolean isGoldTool = item == Items.GOLDEN_PICKAXE
                          || item == Items.GOLDEN_AXE
                          || item == Items.GOLDEN_SHOVEL;

        if (isGoldTool) {
            cir.setReturnValue(true);
        }
    }
}
