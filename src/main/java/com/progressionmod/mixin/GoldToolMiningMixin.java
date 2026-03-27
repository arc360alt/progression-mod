package com.progressionmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class GoldToolMiningMixin {

    @Inject(method = "isSuitableFor", at = @At("RETURN"), cancellable = true)
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