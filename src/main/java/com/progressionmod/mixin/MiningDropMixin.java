package com.progressionmod.mixin;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MiningDropMixin {

    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void preventWrongTierMining(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = player.level().getBlockState(pos);
        Item held = player.getMainHandItem().getItem();
        int tier = getToolTier(held);

        if (isEndiumOre(state)) {
            System.out.println("[DEBUG] Endium ore break attempt — tier: " + tier + " — item: " + held);
        }

        if (isEndiumOre(state) && tier < 9) { cir.setReturnValue(false); return; }

        if (isCopperBlock(state)    && tier < 3) { cir.setReturnValue(false); return; }
        if (isIronBlock(state)      && tier < 4) { cir.setReturnValue(false); return; }
        if (isGoldBlock(state)      && tier < 5) { cir.setReturnValue(false); return; }
        if (isDiamondBlock(state)   && tier < 6) { cir.setReturnValue(false); return; }
        if (isAmethystOre(state)    && tier < 7) { cir.setReturnValue(false); return; }
        if (isNetheriteBlock(state) && tier < 8) { cir.setReturnValue(false); return; }
    }

    private int getToolTier(Item item) {
        if (item == ModItems.FLINT_PICKAXE    || item == ModItems.FLINT_AXE
         || item == ModItems.FLINT_SHOVEL)                                       return 1;
        if (item == Items.WOODEN_PICKAXE      || item == Items.WOODEN_AXE
         || item == Items.WOODEN_SHOVEL)                                         return 2;
        if (item == Items.STONE_PICKAXE       || item == Items.STONE_AXE
         || item == Items.STONE_SHOVEL)                                          return 3;
        if (item == Items.COPPER_PICKAXE      || item == Items.COPPER_AXE
         || item == Items.COPPER_SHOVEL)                                         return 4;
        if (item == Items.IRON_PICKAXE        || item == Items.IRON_AXE
         || item == Items.IRON_SHOVEL)                                           return 5;
        if (item == Items.GOLDEN_PICKAXE      || item == Items.GOLDEN_AXE
         || item == Items.GOLDEN_SHOVEL)                                         return 6;
        if (item == Items.DIAMOND_PICKAXE     || item == Items.DIAMOND_AXE
         || item == Items.DIAMOND_SHOVEL)                                        return 7;
        if (item == ModItems.AMETHYST_PICKAXE || item == ModItems.AMETHYST_AXE
         || item == ModItems.AMETHYST_SHOVEL)                                    return 8;
        if (item == Items.NETHERITE_PICKAXE   || item == Items.NETHERITE_AXE
         || item == Items.NETHERITE_SHOVEL)                                      return 9;
        return -1;
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

    private boolean isAmethystOre(BlockState state) {
        return state.is(ModBlocks.AMETHYST_ORE)
            || state.is(ModBlocks.DEEPSLATE_AMETHYST_ORE);
    }

    private boolean isNetheriteBlock(BlockState state) {
        return state.is(Blocks.ANCIENT_DEBRIS);
    }

    private boolean isEndiumOre(BlockState state) {
        return state.is(ModBlocks.ENDIUM_ORE);
    }
}
