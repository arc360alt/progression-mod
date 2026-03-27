package com.progressionmod.mixin;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.progressionmod.ModConfig;
import net.minecraft.block.Blocks;

/**
 * Enforces custom tier progression by overriding break speed.
 * If you have the RIGHT tier or higher → return without override (vanilla speed).
 * If you have the WRONG tier → set speed to 0.001 and show message.
 *
 * Tier ladder:
 *  1 Flint    → wood logs, dirt, gravel, sand
 *  2 Wood     → stone-tier blocks
 *  3 Stone    → copper ore
 *  4 Copper   → iron ore
 *  5 Iron     → gold ore
 *  6 Gold     → diamond + all deepslate ores
 *  7 Diamond  → amethyst ore
 *  8 Amethyst → ancient debris
 *  9 Netherite→ everything
 */
@Mixin(PlayerEntity.class)
public class MiningLevelMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void enforceTierProgression(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack held = player.getMainHandStack();
        Item heldItem = held.getItem();

        int toolTier = getToolTier(heldItem);
        if (toolTier < 0) return; // not a tracked tool, let vanilla handle it

        // Stone-tier blocks — require Wood (2)
        if (isStoneBlock(state)) {
            if (toolTier < 2) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Wooden Pickaxe to mine stone!");
            }
            return;
        }

        // Copper ore — require Stone (3)
        if (isCopperBlock(state)) {
            if (toolTier < 3) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Stone Pickaxe to mine copper!");
            }
            return;
        }

        // Iron ore — require Copper (4)
        if (isIronBlock(state)) {
            if (toolTier < 4) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Copper Pickaxe to mine iron!");
            }
            return;
        }

        // Gold ore — require Iron (5)
        if (isGoldBlock(state)) {
            if (toolTier < 5) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least an Iron Pickaxe to mine gold!");
            }
            return;
        }

        // Diamond / deepslate ores — require Gold (6)
        if (isDiamondOrDeepslateBlock(state)) {
            if (toolTier < 6) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Gold Pickaxe to mine diamond!");
            }
            return;
        }

        // Amethyst ore — require Diamond (7)
        // IMPORTANT: do NOT override speed here if tier >= 7, let vanilla give full speed
        if (isAmethystOreBlock(state)) {
            if (toolTier < 7) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Diamond Pickaxe to mine Amethyst Ore!");
            }
            return;
        }

        // Ancient debris — require Amethyst (8)
        if (isNetheriteBlock(state)) {
            if (toolTier < 8) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least an Amethyst Pickaxe to mine Ancient Debris!");
            }
            return;
        }
    }

    // ── Tier mapping ──────────────────────────────────────────────────────────
    private int getToolTier(Item item) {
        if (item == ModItems.FLINT_PICKAXE    || item == ModItems.FLINT_AXE
         || item == ModItems.FLINT_SHOVEL     || item == ModItems.FLINT_HOE)    return 1;

        if (item == Items.WOODEN_PICKAXE      || item == Items.WOODEN_AXE
         || item == Items.WOODEN_SHOVEL)                                         return 2;

        if (item == Items.STONE_PICKAXE       || item == Items.STONE_AXE
         || item == Items.STONE_SHOVEL)                                          return 3;

        if (item == ModItems.COPPER_PICKAXE   || item == ModItems.COPPER_AXE
         || item == ModItems.COPPER_SHOVEL    || item == ModItems.COPPER_HOE)   return 4;

        if (item == Items.IRON_PICKAXE        || item == Items.IRON_AXE
         || item == Items.IRON_SHOVEL)                                           return 5;

        if (item == Items.GOLDEN_PICKAXE      || item == Items.GOLDEN_AXE
         || item == Items.GOLDEN_SHOVEL)                                         return 6;

        if (item == Items.DIAMOND_PICKAXE     || item == Items.DIAMOND_AXE
         || item == Items.DIAMOND_SHOVEL)                                        return 7;

        if (item == ModItems.AMETHYST_PICKAXE || item == ModItems.AMETHYST_AXE
         || item == ModItems.AMETHYST_SHOVEL  || item == ModItems.AMETHYST_HOE) return 8;

        if (item == Items.NETHERITE_PICKAXE   || item == Items.NETHERITE_AXE
         || item == Items.NETHERITE_SHOVEL)                                      return 9;

        return -1;
    }

    // ── Block category checks ─────────────────────────────────────────────────

    private boolean isStoneBlock(BlockState state) {
        return state.isIn(BlockTags.PICKAXE_MINEABLE)
                && !isCopperBlock(state)
                && !isIronBlock(state)
                && !isGoldBlock(state)
                && !isDiamondOrDeepslateBlock(state)
                && !isAmethystOreBlock(state)
                && !isNetheriteBlock(state)
                && !isObsidianBlock(state);
    }

    private boolean isCopperBlock(BlockState state) {
        return state.isOf(Blocks.COPPER_ORE)
            || state.isOf(Blocks.RAW_COPPER_BLOCK);
    }

    private boolean isIronBlock(BlockState state) {
        return state.isOf(Blocks.IRON_ORE)
            || state.isOf(Blocks.RAW_IRON_BLOCK)
            || state.isOf(Blocks.LAPIS_ORE)
            || state.isOf(Blocks.LAPIS_BLOCK);
    }

    private boolean isGoldBlock(BlockState state) {
        return state.isOf(Blocks.GOLD_ORE)
            || state.isOf(Blocks.RAW_GOLD_BLOCK)
            || state.isOf(Blocks.REDSTONE_ORE)
            || state.isOf(Blocks.EMERALD_ORE);
    }

    private boolean isDiamondOrDeepslateBlock(BlockState state) {
        return state.isOf(Blocks.DIAMOND_ORE)
            || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
            || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
            || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
            || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)
            || state.isOf(Blocks.DEEPSLATE_COAL_ORE)
            || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    private boolean isAmethystOreBlock(BlockState state) {
        return state.isOf(ModBlocks.AMETHYST_ORE)
            || state.isOf(ModBlocks.DEEPSLATE_AMETHYST_ORE);
    }

    private boolean isNetheriteBlock(BlockState state) {
        return state.isOf(Blocks.ANCIENT_DEBRIS);
    }

    private boolean isObsidianBlock(BlockState state) {
        return state.isOf(Blocks.OBSIDIAN)
            || state.isOf(Blocks.CRYING_OBSIDIAN);
    }

    // ── Message throttle ──────────────────────────────────────────────────────
    private long lastMessageTime = 0;

    private void sendTierMessage(PlayerEntity player, String message) {
        long now = player.getWorld().getTime();
        if (now - lastMessageTime > 40) {
            player.sendMessage(Text.literal("⛏ " + message).formatted(Formatting.RED), true);
            lastMessageTime = now;
        }
    }
}