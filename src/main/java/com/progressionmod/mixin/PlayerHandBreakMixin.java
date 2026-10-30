package com.progressionmod.mixin;

import com.progressionmod.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wood can only be chopped with an axe. Uses the vanilla {@code #minecraft:logs} and
 * {@code #minecraft:bamboo_blocks} tags so every log, wood, stem and hyphae block
 * (including stripped and newly added wood types) is covered.
 */
@Mixin(Player.class)
public class PlayerHandBreakMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.BAMBOO_BLOCKS)) return;

        Player player = (Player) (Object) this;
        ItemStack held = player.getMainHandItem();
        // An axe mines wood faster than a bare hand (speed 1.0); swords, pickaxes,
        // shovels and hoes don't, so they can't be used to get around the rule.
        if (held.getDestroySpeed(state) > 1.0f) return;

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

    private long lastWarnTime = 0;
}
