package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class GrassStringLoot {
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            // getLootTableKey() returns Optional<RegistryKey<LootTable>> in 1.21.5
            Identifier tableId = key.getValue();

            boolean isShortGrass = Blocks.SHORT_GRASS.getLootTableKey()
                    .map(k -> k.getValue().equals(tableId))
                    .orElse(false);

            boolean isTallGrass = tableId.equals(Identifier.of("minecraft", "blocks/tall_grass"));

            if (isShortGrass || isTallGrass) {
                // In 1.21.5, ItemPredicate.Builder.items() needs a RegistryEntryLookup<Item>
                // obtained from the registries wrapper passed in by the loot event.
                var itemLookup = registries.getOrThrow(RegistryKeys.ITEM);

                LootPool.Builder stringPool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.25f))
                        .conditionally(MatchToolLootCondition.builder(
                                ItemPredicate.Builder.create()
                                        .items(itemLookup, ModItems.FLINT_SWORD)
                        ))
                        .with(ItemEntry.builder(Items.STRING))
                        .apply(SetCountLootFunction.builder(
                                UniformLootNumberProvider.create(1, 2)));

                tableBuilder.pool(stringPool);
            }
        });
    }
}