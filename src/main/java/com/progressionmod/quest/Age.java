package com.progressionmod.quest;

import com.progressionmod.ProgressionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * The ages a player progresses through, in order. Every item in an age's item tag
 * ({@code progressionmod:age/<id>}) is locked until the player reaches that age.
 * The tags are plain datapack JSON, so packs can add or remove locked items.
 *
 * A player's age is stored as an index into {@link #values()}; an index equal to
 * {@link #COUNT} means every age has been completed.
 */
public enum Age {
    FLINT    ("flint",     "Flint Age",     "progressionmod:flint_axe",        0xA8A29E),
    WOODEN   ("wooden",    "Wooden Age",    "minecraft:wooden_pickaxe",        0xB08850),
    STONE    ("stone",     "Stone Age",     "minecraft:stone_pickaxe",         0x9CA3AF),
    COPPER   ("copper",    "Copper Age",    "minecraft:copper_pickaxe",        0xE07B4C),
    IRON     ("iron",      "Iron Age",      "minecraft:iron_pickaxe",          0xE5E7EB),
    GOLD     ("gold",      "Gold Age",      "minecraft:golden_pickaxe",        0xFACC15),
    DIAMOND  ("diamond",   "Diamond Age",   "minecraft:diamond_pickaxe",       0x5EEAD4),
    AMETHYST ("amethyst",  "Amethyst Age",  "progressionmod:amethyst_pickaxe", 0xC084FC),
    NETHERITE("netherite", "Netherite Age", "minecraft:netherite_pickaxe",     0x8B6F6F),
    ENDIUM   ("endium",    "Endium Age",    "progressionmod:endium_pickaxe",   0x4ADE80);

    public static final int COUNT = values().length;

    public final String id;
    public final String displayName;
    public final Identifier icon;
    public final int color;
    public final TagKey<Item> itemTag;

    Age(String id, String displayName, String icon, int color) {
        this.id = id;
        this.displayName = displayName;
        this.icon = Identifier.parse(icon);
        this.color = color;
        this.itemTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "age/" + id));
    }

    public static Age byId(String id) {
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        for (Age age : values()) if (age.id.equals(path)) return age;
        return null;
    }

    public static Age byIndex(int index) {
        return index >= 0 && index < COUNT ? values()[index] : null;
    }

    public static String nameOf(int index) {
        Age age = byIndex(index);
        return age != null ? age.displayName : "Ascended";
    }
}
