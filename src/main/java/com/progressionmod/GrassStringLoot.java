package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;

public class GrassStringLoot {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(Blocks.GRASS.getLootTableId())
             || id.equals(new Identifier("minecraft", "blocks/tall_grass"))) {

                // String drop — only with flint sword
                LootPool.Builder stringPool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(0.25f))
                    .conditionally(MatchToolLootCondition.builder(
                        ItemPredicate.Builder.create()
                            .items(ModItems.FLINT_SWORD)
                    ))
                    .with(ItemEntry.builder(Items.STRING))
                    .apply(SetCountLootFunction.builder(
                        UniformLootNumberProvider.create(1, 2)));

                tableBuilder.pool(stringPool);
            }
        });
    }
}