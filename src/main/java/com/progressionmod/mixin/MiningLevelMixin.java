package com.progressionmod.mixin;

import com.progressionmod.ModConfig;
import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MiningLevelMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void enforceTierProgression(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        ItemStack held = player.getMainHandItem();
        Item heldItem = held.getItem();

        int toolTier = getToolTier(heldItem);

        if (isStoneBlock(state)) {
            if (toolTier < 2) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Wooden Pickaxe to mine stone!");
            }
            return;
        }

        if (isCopperBlock(state)) {
            if (toolTier < 3) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Stone Pickaxe to mine copper!");
            }
            return;
        }

        if (isIronBlock(state)) {
            if (toolTier < 4) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Copper Pickaxe to mine iron!");
            }
            return;
        }

        if (isGoldBlock(state)) {
            if (toolTier < 5) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least an Iron Pickaxe to mine gold!");
            }
            return;
        }

        if (isDiamondBlock(state)) {
            if (toolTier < 6) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Gold Pickaxe to mine diamond!");
            }
            return;
        }

        if (isAmethystOreBlock(state)) {
            if (toolTier < 7) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Diamond Pickaxe to mine Amethyst Ore!");
            }
            return;
        }

        if (isNetheriteBlock(state)) {
            if (toolTier < 8) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least an Amethyst Pickaxe to mine Ancient Debris!");
            }
            return;
        }

        if (isEndiumOreBlock(state)) {
            if (toolTier < 9) {
                cir.setReturnValue(0.001f);
                if (ModConfig.get().showMiningTierMessages)
                    sendTierMessage(player, "You need at least a Netherite Pickaxe to mine Endium Ore!");
            }
            return;
        }
    }

    private int getToolTier(Item item) {
        if (item == ModItems.FLINT_PICKAXE    || item == ModItems.FLINT_AXE
         || item == ModItems.FLINT_SHOVEL     || item == ModItems.FLINT_HOE)    return 1;

        if (item == Items.WOODEN_PICKAXE      || item == Items.WOODEN_AXE
         || item == Items.WOODEN_SHOVEL)                                         return 2;

        if (item == Items.STONE_PICKAXE       || item == Items.STONE_AXE
         || item == Items.STONE_SHOVEL)                                          return 3;

        if (item == Items.COPPER_PICKAXE      || item == Items.COPPER_AXE
         || item == Items.COPPER_SHOVEL       || item == Items.COPPER_HOE)      return 4;

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

    private boolean isStoneBlock(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                && !isCopperBlock(state)
                && !isIronBlock(state)
                && !isGoldBlock(state)
                && !isDiamondBlock(state)
                && !isAmethystOreBlock(state)
                && !isNetheriteBlock(state)
                && !isEndiumOreBlock(state)
                && !isObsidianBlock(state);
    }

    private boolean isCopperBlock(BlockState state) {
        return state.is(Blocks.COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.RAW_COPPER_BLOCK);
    }

    private boolean isIronBlock(BlockState state) {
        return state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.RAW_IRON_BLOCK)
            || state.is(Blocks.LAPIS_ORE)
            || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.LAPIS_BLOCK);
    }

    private boolean isGoldBlock(BlockState state) {
        return state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.RAW_GOLD_BLOCK)
            || state.is(Blocks.REDSTONE_ORE)
            || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.is(Blocks.EMERALD_ORE)
            || state.is(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    private boolean isDiamondBlock(BlockState state) {
        return state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private boolean isAmethystOreBlock(BlockState state) {
        return state.is(ModBlocks.AMETHYST_ORE)
            || state.is(ModBlocks.DEEPSLATE_AMETHYST_ORE);
    }

    private boolean isNetheriteBlock(BlockState state) {
        return state.is(Blocks.ANCIENT_DEBRIS);
    }

    private boolean isEndiumOreBlock(BlockState state) {
        return state.is(ModBlocks.ENDIUM_ORE);
    }

    private boolean isObsidianBlock(BlockState state) {
        return state.is(Blocks.OBSIDIAN)
            || state.is(Blocks.CRYING_OBSIDIAN);
    }

    private long lastMessageTime = 0;

    private void sendTierMessage(Player player, String message) {
        long now = player.level().getGameTime();
        if (now - lastMessageTime > 40) {
            player.sendOverlayMessage(
                    Component.literal("⛏ " + message).withStyle(ChatFormatting.RED));
            lastMessageTime = now;
        }
    }
}
