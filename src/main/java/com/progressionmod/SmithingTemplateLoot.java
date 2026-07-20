package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public class SmithingTemplateLoot {

    private static final Identifier NETHER_BRIDGE       = Identifier.fromNamespaceAndPath("minecraft", "chests/nether_bridge");
    private static final Identifier BASTION_OTHER       = Identifier.fromNamespaceAndPath("minecraft", "chests/bastion_other");
    private static final Identifier BASTION_BRIDGE      = Identifier.fromNamespaceAndPath("minecraft", "chests/bastion_bridge");
    private static final Identifier BASTION_STABLE      = Identifier.fromNamespaceAndPath("minecraft", "chests/bastion_hoglin_stable");
    private static final Identifier BASTION_TREASURE    = Identifier.fromNamespaceAndPath("minecraft", "chests/bastion_treasure");

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {
            Identifier id = key.identifier();
            if (!id.equals(NETHER_BRIDGE) && !id.equals(BASTION_OTHER)
                    && !id.equals(BASTION_BRIDGE) && !id.equals(BASTION_STABLE)
                    && !id.equals(BASTION_TREASURE)) return;

            // Diamond→Amethyst template: 30% chance in each chest
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.30f))
                    .add(LootItem.lootTableItem(ModItems.DIAMOND_AMETHYST_UPGRADE)));

            // Amethyst→Netherite template: 20% chance
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.20f))
                    .add(LootItem.lootTableItem(ModItems.AMETHYST_NETHERITE_UPGRADE)));

            // Netherite→Endium template: 10% chance (rarest upgrade)
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f))
                    .add(LootItem.lootTableItem(ModItems.NETHERITE_ENDIUM_UPGRADE)));
        });
    }
}
