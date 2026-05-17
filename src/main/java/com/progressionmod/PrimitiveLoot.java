package com.progressionmod;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class PrimitiveLoot {

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {

            String tablePath = key.identifier().getPath();

            if (ModConfig.get().stickDropFromLeaves
                    && tablePath.startsWith("blocks/") && tablePath.endsWith("_leaves")) {
                LootPool.Builder stickPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f))
                    .add(LootItem.lootTableItem(Items.STICK)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));
                tableBuilder.withPool(stickPool);
            }

            if (ModConfig.get().improvedFlintFromGravel
                    && key.identifier().equals(Identifier.fromNamespaceAndPath("minecraft", "blocks/gravel"))) {
                LootPool.Builder extraFlintPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f))
                    .add(LootItem.lootTableItem(Items.FLINT)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
                tableBuilder.withPool(extraFlintPool);
            }
        });
    }
}
