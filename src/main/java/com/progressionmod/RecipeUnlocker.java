package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUnlocker {

    public static final Map<Item, List<String>> UNLOCK_MAP = new HashMap<>();

    static {
        List<String> allFlintRecipes = List.of(
                "progressionmod:flint_axe",
                "progressionmod:flint_pickaxe",
                "progressionmod:flint_shovel",
                "progressionmod:flint_sword",
                "progressionmod:flint_hoe"
        );

        List<String> allCopperRecipes = List.of(
                "minecraft:copper_axe",
                "minecraft:copper_pickaxe",
                "minecraft:copper_shovel",
                "minecraft:copper_sword",
                "minecraft:copper_hoe",
                "minecraft:copper_helmet",
                "minecraft:copper_chestplate",
                "minecraft:copper_leggings",
                "minecraft:copper_boots"
        );

        List<String> allAmethystRecipes = List.of(
                "progressionmod:amethyst_axe",
                "progressionmod:amethyst_pickaxe",
                "progressionmod:amethyst_shovel",
                "progressionmod:amethyst_sword",
                "progressionmod:amethyst_hoe",
                "progressionmod:amethyst_helmet",
                "progressionmod:amethyst_chestplate",
                "progressionmod:amethyst_leggings",
                "progressionmod:amethyst_boots",
                "progressionmod:amethyst_ingot_from_smelting",
                "progressionmod:amethyst_ingot_from_blasting"
        );

        List<String> allEndiumRecipes = List.of(
                "progressionmod:endium_axe",
                "progressionmod:endium_pickaxe",
                "progressionmod:endium_shovel",
                "progressionmod:endium_sword",
                "progressionmod:endium_hoe",
                "progressionmod:endium_helmet",
                "progressionmod:endium_chestplate",
                "progressionmod:endium_leggings",
                "progressionmod:endium_boots",
                "progressionmod:endium_crystal_from_smelting",
                "progressionmod:endium_crystal_from_blasting"
        );

        UNLOCK_MAP.put(Items.FLINT,           allFlintRecipes);
        UNLOCK_MAP.put(Items.COPPER_INGOT,    allCopperRecipes);
        UNLOCK_MAP.put(ModItems.RAW_AMETHYST, allAmethystRecipes);
        UNLOCK_MAP.put(ModItems.RAW_ENDIUM,   allEndiumRecipes);

        UNLOCK_MAP.put(Items.GRAVEL,     List.of("progressionmod:sticks_from_gravel"));
        UNLOCK_MAP.put(Items.OAK_LEAVES, List.of("progressionmod:sticks_from_leaves"));
    }

    @SuppressWarnings("unchecked")
    public static void tryUnlock(ServerPlayer player, Item pickedUp) {
        List<String> toUnlock = UNLOCK_MAP.get(pickedUp);
        if (toUnlock == null) return;

        var keys = toUnlock.stream()
                .map(id -> ResourceKey.<Recipe<?>>create(Registries.RECIPE, Identifier.parse(id)))
                .toList();
        player.awardRecipesByKey(keys);
    }

    public static void register() {
        // Registration is handled via the ItemPickupMixin
    }
}
