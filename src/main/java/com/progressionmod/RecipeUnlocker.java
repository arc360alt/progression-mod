package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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
                "progressionmod:copper_axe",
                "progressionmod:copper_pickaxe",
                "progressionmod:copper_shovel",
                "progressionmod:copper_sword",
                "progressionmod:copper_hoe",
                "progressionmod:copper_helmet",
                "progressionmod:copper_chestplate",
                "progressionmod:copper_leggings",
                "progressionmod:copper_boots"
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

        UNLOCK_MAP.put(Items.FLINT,         allFlintRecipes);
        UNLOCK_MAP.put(Items.COPPER_INGOT,  allCopperRecipes);
        UNLOCK_MAP.put(ModItems.RAW_AMETHYST, allAmethystRecipes);

        UNLOCK_MAP.put(Items.GRAVEL,    List.of("progressionmod:sticks_from_gravel"));
        UNLOCK_MAP.put(Items.OAK_LEAVES, List.of("progressionmod:sticks_from_leaves"));
    }

    public static void tryUnlock(ServerPlayerEntity player, Item pickedUp) {
        List<String> toUnlock = UNLOCK_MAP.get(pickedUp);
        if (toUnlock == null) return;

        Identifier[] ids = toUnlock.stream()
                .map(Identifier::new)
                .toArray(Identifier[]::new);

        player.unlockRecipes(ids);
    }

    public static void register() {
        // Registration is handled via the ItemPickupMixin
    }
}
