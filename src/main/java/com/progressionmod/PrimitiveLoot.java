package com.progressionmod;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;

public class PrimitiveLoot {

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            String tablePath = key.getValue().getPath();

            if (ModConfig.get().stickDropFromLeaves
                    && tablePath.startsWith("blocks/") && tablePath.endsWith("_leaves")) {
                LootPool.Builder stickPool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(0.14f))
                    .with(ItemEntry.builder(Items.STICK))
                    .apply(SetCountLootFunction.builder(
                        UniformLootNumberProvider.create(1, 2)));
                tableBuilder.pool(stickPool);
            }

            if (ModConfig.get().improvedFlintFromGravel
                    && key.getValue().equals(Identifier.of("minecraft", "blocks/gravel"))) {
                LootPool.Builder extraFlintPool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(0.18f))
                    .with(ItemEntry.builder(Items.FLINT))
                    .apply(SetCountLootFunction.builder(
                        ConstantLootNumberProvider.create(1)));
                tableBuilder.pool(extraFlintPool);
            }
        });
    }
}