package com.progressionmod.mixin;

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
 * Enforces the custom tier progression:
 *
 *  Flint     → can mine wood + dirt/gravel/sand (NOT stone)
 *  Wood      → can mine stone (NOT iron ore)
 *  Stone     → can mine copper ore (NOT iron ore)
 *  Copper    → can mine iron ore (NOT gold/diamond)
 *  Iron      → can mine gold ore (NOT diamond)
 *  Gold      → NOT useful for mining (low durability, can mine same as iron)
 *  Diamond   → can mine deepslate ores, ancient debris, etc.
 *
 * This mixin intercepts the effective tool check and overrides it.
 */
@Mixin(PlayerEntity.class)
public class MiningLevelMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void enforceTierProgression(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack held = player.getMainHandStack();
        Item heldItem = held.getItem();

        // ── Determine the "tier" of the held tool ────────────────────────────
        int toolTier = getToolTier(heldItem);
        if (toolTier < 0) return; // Not a tool we're tracking — let vanilla handle it

        // ── Check what block is being targeted ───────────────────────────────

        // STONE TIER blocks (require wood tools minimum)
        if (isStoneBlock(state)) {
            if (toolTier < 2) { // Less than wood (2)
            cir.setReturnValue(0.001f);
            if (ModConfig.get().showMiningTierMessages) {
                sendTierMessage(player, "You need at least a Wooden Pickaxe to mine stone!");
            }
            }
            return;
        }

        // COPPER ORE (requires stone tools minimum)
        if (isCopperBlock(state)) {
            if (toolTier < 3) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Stone Pickaxe to mine copper!");
            }
            return;
        }

        // IRON ORE (requires copper tools — tier 4)
        if (isIronBlock(state)) {
            if (toolTier < 4) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Copper Pickaxe to mine iron!");
            }
            return;
        }

        // GOLD ORE (requires iron tools — tier 5)
        if (isGoldBlock(state)) {
            if (toolTier < 5) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least an Iron Pickaxe to mine gold!");
            }
            return;
        }

        // DIAMOND/DEEPSLATE (requires gold tools — tier 6)
        if (isDiamondOrDeepslateBlock(state)) {
            if (toolTier < 6) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Gold Pickaxe to mine diamond/deepslate!");
            }
            return;
        }
    }

    // ── Tool Tier Mapping ─────────────────────────────────────────────────────
    // Returns -1 if not a recognised pickaxe-type tool
    private int getToolTier(Item item) {
        // Flint = tier 1
        if (item == ModItems.FLINT_PICKAXE || item == ModItems.FLINT_AXE
                || item == ModItems.FLINT_SHOVEL || item == ModItems.FLINT_HOE) return 1;

        // Wood = tier 2
        if (item == Items.WOODEN_PICKAXE || item == Items.WOODEN_AXE
                || item == Items.WOODEN_SHOVEL) return 2;

        // Stone = tier 3
        if (item == Items.STONE_PICKAXE || item == Items.STONE_AXE
                || item == Items.STONE_SHOVEL) return 3;

        // Copper = tier 4
        if (item == ModItems.COPPER_PICKAXE || item == ModItems.COPPER_AXE
                || item == ModItems.COPPER_SHOVEL || item == ModItems.COPPER_HOE) return 4;

        // Iron = tier 5
        if (item == Items.IRON_PICKAXE || item == Items.IRON_AXE
                || item == Items.IRON_SHOVEL) return 5;

        // Gold = tier 6 (above iron — can mine diamond-tier blocks)
        if (item == Items.GOLDEN_PICKAXE || item == Items.GOLDEN_AXE
                || item == Items.GOLDEN_SHOVEL) return 6;

        // Diamond = tier 7
        if (item == Items.DIAMOND_PICKAXE || item == Items.DIAMOND_AXE
                || item == Items.DIAMOND_SHOVEL) return 7;

        // Netherite = tier 8
        if (item == Items.NETHERITE_PICKAXE || item == Items.NETHERITE_AXE
                || item == Items.NETHERITE_SHOVEL) return 8;

        return -1; // Not a tool we track
    }

    // ── Block Category Checks ─────────────────────────────────────────────────

        private boolean isStoneBlock(BlockState state) {
            return state.isIn(BlockTags.PICKAXE_MINEABLE)
                    && !isCopperBlock(state)
                    && !isIronBlock(state)
                    && !isGoldBlock(state)
                    && !isDiamondOrDeepslateBlock(state)
                    && !isObsidianBlock(state);
        }

        // Requires stone tools (tier 3)
        private boolean isCopperBlock(BlockState state) {
            return state.isOf(Blocks.COPPER_ORE)
                || state.isOf(Blocks.RAW_COPPER_BLOCK);
        }

        // Requires copper tools (tier 4)
        private boolean isIronBlock(BlockState state) {
            return state.isOf(Blocks.IRON_ORE)
                || state.isOf(Blocks.RAW_IRON_BLOCK)
                || state.isOf(Blocks.LAPIS_ORE)
                || state.isOf(Blocks.LAPIS_BLOCK);
        }

        // Requires iron tools (tier 5)
        private boolean isGoldBlock(BlockState state) {
            return state.isOf(Blocks.GOLD_ORE)
                || state.isOf(Blocks.RAW_GOLD_BLOCK)
                || state.isOf(Blocks.REDSTONE_ORE)
                || state.isOf(Blocks.EMERALD_ORE);
        }

        // Requires gold tools (tier 6)
        private boolean isDiamondOrDeepslateBlock(BlockState state) {
            return state.isOf(Blocks.DIAMOND_ORE)
                || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
                || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
                || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)
                || state.isOf(Blocks.DEEPSLATE_COAL_ORE)
                || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)
                || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)
                || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)
                || state.isOf(Blocks.ANCIENT_DEBRIS);
        }

        private boolean isObsidianBlock(BlockState state) {
            return state.isOf(Blocks.OBSIDIAN)
                || state.isOf(Blocks.CRYING_OBSIDIAN);
        }
    

    // ── Helper ────────────────────────────────────────────────────────────────
    private long lastMessageTime = 0;

    private void sendTierMessage(PlayerEntity player, String message) {
        // Throttle messages to avoid spamming every tick
        long now = player.getWorld().getTime();
        if (now - lastMessageTime > 40) { // 2 seconds
            player.sendMessage(Text.literal("⛏ " + message).formatted(Formatting.RED), true); // true = action bar
            lastMessageTime = now;
        }
    }
}
