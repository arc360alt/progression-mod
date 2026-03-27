# Progression Overhaul Mod
### Fabric 1.20.1

A complete Minecraft progression overhaul that forces you to earn every tier.

---

## 📋 Progression Tiers

| Tier | Tools Needed | What You Can Mine |
|------|-------------|-------------------|
| **Flint** | Flint Axe/Pick/Shovel | Wood, dirt, sand, gravel, leaves |
| **Wood** | Wooden tools | Stone, coal ore |
| **Stone** | Stone tools | Copper ore |
| **Copper*** | Iron tools | Iron ore |
| **Iron** | Iron tools | Gold ore |
| **Diamond** | Diamond tools | Deepslate ores, Ancient Debris |

*Note: Vanilla has no copper tools. In this mod, **Golden tools** fill the Copper tier role. You craft a Golden Pickaxe using gold nuggets (found from washing gravel) before getting iron.*

---

## 🪨 How to Start (Step-by-Step)

1. **Punch gravel** — gravel can be broken by hand
2. **Collect Flint** — drops from gravel naturally
3. **Get Sticks** — craft 3 leaves → 2 sticks, OR craft 2 gravel → 2 sticks
4. **Craft a Flint Axe** in your 2x2 inventory crafting grid:
   ```
   F F
   F S
     S
   ```
5. Use the Flint Axe to chop **wood logs**
6. Craft a **crafting table** → then craft wooden tools normally
7. Use wooden tools to mine **stone** → craft stone tools
8. Stone tools let you mine **copper ore** → smelt copper ingots
9. Continue up the chain!

---

## ⚠️ Key Changes from Vanilla

- **You cannot break wood with your bare hands.** You need a Flint Axe minimum.
- **Stone tools cannot mine iron ore** — you need copper-tier (golden) tools first.
- **Iron tools cannot mine diamond ore** — you need diamond tools.
- Messages appear on your hotbar when you try to mine something you can't yet.

---

## 🎨 Texture Installation

The mod ships with **placeholder textures** for flint tools (simple grey 16×16 pixels).

**To use your own custom textures:**

1. Create five 16×16 PNG files (use any pixel art editor — Aseprite, GIMP, etc.)
2. Name them exactly:
   - `flint_axe.png`
   - `flint_pickaxe.png`
   - `flint_shovel.png`
   - `flint_sword.png`
   - `flint_hoe.png`
3. Place them in:
   ```
   src/main/resources/assets/progressionmod/textures/item/
   ```
4. Rebuild the mod with `./gradlew build`

**Style tips:**
- Make them look rough/primitive — chipped grey stone with darker outlines
- Use a similar art style to vanilla tools (dark outlines, slightly transparent background)
- Look at vanilla's `stone_axe.png` in the Minecraft assets for reference

---

## 🔨 Building the Mod

**Requirements:**
- Java 17 (JDK)
- That's it! Gradle is included via the wrapper.

**Steps:**
```bash
# On Mac/Linux:
./gradlew build

# On Windows:
gradlew.bat build
```

Output JAR will be at:
```
build/libs/progression-overhaul-1.0.0.jar
```

Copy this JAR into your `.minecraft/mods/` folder alongside the **Fabric API** mod.

---

## 📁 Project Structure

```
src/main/java/com/progressionmod/
├── ProgressionMod.java          ← Main entrypoint
├── items/
│   ├── FlintToolMaterial.java   ← Flint tool stats (durability, speed, etc.)
│   └── ModItems.java            ← Registers all flint tools
└── mixin/
    ├── PlayerHandBreakMixin.java ← Prevents hand-breaking wood
    └── MiningLevelMixin.java    ← Enforces tier restrictions

src/main/resources/
├── fabric.mod.json
├── progressionmod.mixins.json
├── assets/progressionmod/
│   ├── lang/en_us.json          ← Item display names
│   ├── models/item/             ← Item model JSON files
│   └── textures/item/           ← ← PUT YOUR TEXTURES HERE
└── data/progressionmod/
    └── recipes/                 ← All custom recipes
```

---

## 🧪 Testing Tips

- Use `/gamemode creative` to quickly test tool restrictions
- Use `/give @p progressionmod:flint_axe` to get tools
- The action bar (above hotbar) shows restriction messages when you try to mine the wrong tier

---

## 🛠️ Customization

Want to tweak values? Edit these files:

- **Flint tool stats** (durability, speed): `FlintToolMaterial.java`
- **What each tier can mine**: `MiningLevelMixin.java` — look for the `getToolTier()` method
- **Recipes**: JSON files in `data/progressionmod/recipes/`
