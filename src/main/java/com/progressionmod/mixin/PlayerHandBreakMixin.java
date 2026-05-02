package com.progressionmod.mixin;

import com.progressionmod.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents players from breaking wood-type blocks with their bare hands.
 * Wood requires at least a Flint Axe.
 *
 * In 1.21.5, MiningToolItem and SwordItem are completely removed.
 * Tool/weapon behaviour is now stored in data components:
 *   - DataComponentTypes.TOOL  → mining tools (pickaxes, axes, shovels, hoes)
 *   - DataComponentTypes.WEAPON → swords and other weapons
 * Use ItemStack#contains(DataComponentTypes.TOOL) etc. to check.
 */
@Mixin(PlayerEntity.class)
public class PlayerHandBreakMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack held = player.getMainHandStack();

        // Consider bare-hand if not holding a mining tool or weapon
        boolean isUsingHand = !held.contains(DataComponentTypes.TOOL)
                           && !held.contains(DataComponentTypes.WEAPON);

        if (isUsingHand) {
            if (state.isOf(net.minecraft.block.Blocks.OAK_LOG)
             || state.isOf(net.minecraft.block.Blocks.BIRCH_LOG)
             || state.isOf(net.minecraft.block.Blocks.SPRUCE_LOG)
             || state.isOf(net.minecraft.block.Blocks.JUNGLE_LOG)
             || state.isOf(net.minecraft.block.Blocks.ACACIA_LOG)
             || state.isOf(net.minecraft.block.Blocks.DARK_OAK_LOG)
             || state.isOf(net.minecraft.block.Blocks.MANGROVE_LOG)
             || state.isOf(net.minecraft.block.Blocks.CHERRY_LOG)
             || state.isOf(net.minecraft.block.Blocks.BAMBOO_BLOCK)
             || state.isOf(net.minecraft.block.Blocks.OAK_WOOD)
             || state.isOf(net.minecraft.block.Blocks.BIRCH_WOOD)
             || state.isOf(net.minecraft.block.Blocks.SPRUCE_WOOD)
             || state.isOf(net.minecraft.block.Blocks.JUNGLE_WOOD)
             || state.isOf(net.minecraft.block.Blocks.ACACIA_WOOD)
             || state.isOf(net.minecraft.block.Blocks.DARK_OAK_WOOD)
             || state.isOf(net.minecraft.block.Blocks.MANGROVE_WOOD)
             || state.isOf(net.minecraft.block.Blocks.CHERRY_WOOD)) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showHandBreakWarning) {
                    long now = player.getWorld().getTime();
                    if (now - lastWarnTime > 40) {
                        player.sendMessage(
                                Text.literal("✋ You need at least a Flint Axe to chop wood!")
                                        .formatted(Formatting.RED), true);
                        lastWarnTime = now;
                    }
                }
            }
        }
    }

    private long lastWarnTime = 0;
}