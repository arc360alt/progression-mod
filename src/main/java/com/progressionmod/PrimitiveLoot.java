package com.progressionmod;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class PrimitiveLoot {

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {

            String tablePath = key.identifier().getPath();

            // Leaves are the only early source of sticks (there's no wood without an axe),
            // so every kind of leaves has a 30% chance to drop 1-2 sticks on top of vanilla's 2%.
            if (tablePath.startsWith("blocks/") && tablePath.endsWith("_leaves")) {
                LootPool.Builder stickPool = LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.30f))
                    .add(LootItem.lootTableItem(Items.STICK)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))));
                tableBuilder.withPool(stickPool);
            }

            if (ModConfig.get().improvedFlintFromGravel
                    && key.identifier().equals(Identifier.fromNamespaceAndPath("minecraft", "blocks/gravel"))) {
                LootPool.Builder extraFlintPool = LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f))
                    .add(LootItem.lootTableItem(Items.FLINT)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1))));
                tableBuilder.withPool(extraFlintPool);
            }
        });
    }
}
