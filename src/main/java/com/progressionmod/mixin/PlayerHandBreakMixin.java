package com.progressionmod.mixin;

import com.progressionmod.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerHandBreakMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        ItemStack held = player.getMainHandItem();

        boolean isUsingHand = !held.has(DataComponents.TOOL)
                           && !held.has(DataComponents.WEAPON);

        if (isUsingHand) {
            if (state.is(Blocks.OAK_LOG)
             || state.is(Blocks.BIRCH_LOG)
             || state.is(Blocks.SPRUCE_LOG)
             || state.is(Blocks.JUNGLE_LOG)
             || state.is(Blocks.ACACIA_LOG)
             || state.is(Blocks.DARK_OAK_LOG)
             || state.is(Blocks.MANGROVE_LOG)
             || state.is(Blocks.CHERRY_LOG)
             || state.is(Blocks.BAMBOO_BLOCK)
             || state.is(Blocks.OAK_WOOD)
             || state.is(Blocks.BIRCH_WOOD)
             || state.is(Blocks.SPRUCE_WOOD)
             || state.is(Blocks.JUNGLE_WOOD)
             || state.is(Blocks.ACACIA_WOOD)
             || state.is(Blocks.DARK_OAK_WOOD)
             || state.is(Blocks.MANGROVE_WOOD)
             || state.is(Blocks.CHERRY_WOOD)) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showHandBreakWarning) {
                    long now = player.level().getGameTime();
                    if (now - lastWarnTime > 40) {
                        player.sendOverlayMessage(
                                Component.literal("You need at least a Flint Axe to chop wood!")
                                        .withStyle(ChatFormatting.RED));
                        lastWarnTime = now;
                    }
                }
            }
        }
    }

    private long lastWarnTime = 0;
}
