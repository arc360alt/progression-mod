package com.progressionmod;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class AmethystOreLoot {

    public static void register() {
        LootTableEvents.REPLACE.register((key, original, source, registries) -> {
            Identifier id = key.getValue();

            Item oreItem;
            if (id.equals(Identifier.of(ProgressionMod.MOD_ID, "blocks/amethyst_ore"))) {
                oreItem = ModBlocks.AMETHYST_ORE.asItem();
            } else if (id.equals(Identifier.of(ProgressionMod.MOD_ID, "blocks/deepslate_amethyst_ore"))) {
                oreItem = ModBlocks.DEEPSLATE_AMETHYST_ORE.asItem();
            } else {
                return null;
            }

            var enchantmentLookup = registries.getOrThrow(RegistryKeys.ENCHANTMENT);
            var silkTouchEnchant = enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH);
            var fortune = enchantmentLookup.getOrThrow(Enchantments.FORTUNE);

            LootCondition.Builder silkTouchCondition = new LootCondition.Builder() {
                @Override
                public LootCondition build() {
                    return new LootCondition() {
                        @Override
                        public LootConditionType getType() {
                            return LootConditionTypes.MATCH_TOOL;
                        }

                        @Override
                        public boolean test(LootContext context) {
                            var tool = context.get(LootContextParameters.TOOL);
                            if (tool == null || tool.isEmpty()) return false;
                            var enchants = tool.getEnchantments();
                            if (enchants == null) return false;
                            return enchants.getLevel(silkTouchEnchant) > 0;
                        }
                    };
                }
            };

            var silkTouchEntry = ItemEntry.builder(oreItem)
                    .conditionally(silkTouchCondition);

            var rawAmethystEntry = ItemEntry.builder(ModItems.RAW_AMETHYST)
                    .apply(ApplyBonusLootFunction.oreDrops(fortune))
                    .apply(ExplosionDecayLootFunction.builder());

            LootPool pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(AlternativeEntry.builder(silkTouchEntry, rawAmethystEntry))
                    .build();

            return LootTable.builder().pool(pool).build();
        });
    }
}
