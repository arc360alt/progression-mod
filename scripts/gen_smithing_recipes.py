#!/usr/bin/env python3
"""Generate all smithing upgrade recipes and template crafting recipes."""
import json, os

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PM_RECIPE = os.path.join(BASE, "src/main/resources/data/progressionmod/recipe")
MC_RECIPE  = os.path.join(BASE, "src/main/resources/data/minecraft/recipe")
os.makedirs(PM_RECIPE, exist_ok=True)
os.makedirs(MC_RECIPE,  exist_ok=True)

def write(path, data):
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  {os.path.relpath(path, BASE)}")

# ── Template crafting recipes ─────────────────────────────────────────────────
write(f"{PM_RECIPE}/diamond_amethyst_upgrade.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": ["DAD", "A A", "DAD"],
    "key": {
        "D": {"item": "minecraft:diamond"},
        "A": {"item": "progressionmod:amethyst_ingot"}
    },
    "result": {"id": "progressionmod:diamond_amethyst_upgrade", "count": 1}
})

write(f"{PM_RECIPE}/amethyst_netherite_upgrade.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": ["ANA", "N N", "ANA"],
    "key": {
        "A": {"item": "progressionmod:amethyst_ingot"},
        "N": {"item": "minecraft:netherite_ingot"}
    },
    "result": {"id": "progressionmod:amethyst_netherite_upgrade", "count": 1}
})

write(f"{PM_RECIPE}/netherite_endium_upgrade.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": ["NEN", "E E", "NEN"],
    "key": {
        "N": {"item": "minecraft:netherite_ingot"},
        "E": {"item": "progressionmod:endium_crystal"}
    },
    "result": {"id": "progressionmod:netherite_endium_upgrade", "count": 1}
})

# ── Item pairs for the three upgrade tiers ────────────────────────────────────
SLOTS = [
    ("sword",      "sword"),
    ("axe",        "axe"),
    ("pickaxe",    "pickaxe"),
    ("shovel",     "shovel"),
    ("hoe",        "hoe"),
    ("helmet",     "helmet"),
    ("chestplate", "chestplate"),
    ("leggings",   "leggings"),
    ("boots",      "boots"),
]

# ── Diamond → Amethyst smithing recipes (data/progressionmod/recipe/) ─────────
print("\nDiamond → Amethyst:")
for slot, _ in SLOTS:
    write(f"{PM_RECIPE}/amethyst_{slot}_smithing.json", {
        "type": "minecraft:smithing_transform",
        "template": "progressionmod:diamond_amethyst_upgrade",
        "base": f"minecraft:diamond_{slot}",
        "addition": "progressionmod:amethyst_ingot",
        "result": {"id": f"progressionmod:amethyst_{slot}"}
    })

# ── Netherite → Endium smithing recipes (data/progressionmod/recipe/) ─────────
print("\nNetherite → Endium:")
for slot, _ in SLOTS:
    write(f"{PM_RECIPE}/endium_{slot}_smithing.json", {
        "type": "minecraft:smithing_transform",
        "template": "progressionmod:netherite_endium_upgrade",
        "base": f"minecraft:netherite_{slot}",
        "addition": "progressionmod:endium_crystal",
        "result": {"id": f"progressionmod:endium_{slot}"}
    })

# ── Amethyst → Netherite: override vanilla files (data/minecraft/recipe/) ─────
print("\nAmethyst → Netherite (vanilla overrides):")
for slot, _ in SLOTS:
    write(f"{MC_RECIPE}/netherite_{slot}_smithing.json", {
        "type": "minecraft:smithing_transform",
        "template": "progressionmod:amethyst_netherite_upgrade",
        "base": f"progressionmod:amethyst_{slot}",
        "addition": "#minecraft:netherite_tool_materials",
        "result": {"id": f"minecraft:netherite_{slot}"}
    })

print("\nDone.")
