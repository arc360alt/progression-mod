package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class GrassStringLoot {
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {
            Identifier tableId = key.identifier();

            boolean isShortGrass = tableId.equals(Identifier.fromNamespaceAndPath("minecraft", "blocks/short_grass"));
            boolean isTallGrass  = tableId.equals(Identifier.fromNamespaceAndPath("minecraft", "blocks/tall_grass"));

            if (isShortGrass || isTallGrass) {
                var itemGetter = holder.lookupOrThrow(Registries.ITEM);
                LootPool.Builder stringPool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(itemGetter, ModItems.FLINT_SWORD)))
                        .add(LootItem.lootTableItem(Items.STRING)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

                tableBuilder.withPool(stringPool);
            }
        });
    }
}
