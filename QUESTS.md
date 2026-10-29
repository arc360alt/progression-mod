# Writing Quests

Quests are plain JSON, loaded from datapacks. The built-in 200 quests are in
`src/main/resources/data/progressionmod/progression/quests/`, one file per age.

## Ages

Players start in the **Flint Age** and move to the next age when every
non-optional quest in their current age is done:

Flint → Wooden → Stone → Copper → Iron → Gold → Diamond → Amethyst → Netherite → Endium

Each age's tools and armor stay locked until the player reaches that age. Locked
items can't be picked up, taken from chests, crafting grids, furnaces, smithing
tables or villager trades, used, attacked with, or worn. The locked items for each
age come from the item tag `progressionmod:age/<age>`
(`data/progressionmod/tags/item/age/*.json`), so a datapack can add or remove items.

## Adding quests

Put a file at `data/<your_namespace>/progression/quests/<anything>.json` in a
datapack (or in the mod's resources):

```json
{
  "age": "iron",
  "quests": [
    {
      "id": "mypack/iron/rails",
      "title": "Rail Baron",
      "description": "Lay down a proper track.",
      "icon": "minecraft:rail",
      "task": { "type": "craft", "target": "minecraft:rail", "count": 64 },
      "rewards": { "xp": 20, "items": [ { "item": "minecraft:minecart", "count": 1 } ] }
    }
  ]
}
```

Run `/reload` and the quest appears in every player's quest book.

| Field | Required | Meaning |
|---|---|---|
| `id` | yes | Unique id. Reusing an existing id **replaces** that quest. |
| `title` | yes | Shown in the book and HUD. |
| `description` | no | One or two short sentences. |
| `icon` | no | Item id. Defaults to `minecraft:book`. |
| `task` / `tasks` | yes | One task object, or a list of them (all must be completed). |
| `rewards` | no | `xp` (points) and/or `items` (list of `{item, count}`). |
| `optional` | no | `true` = not needed to advance to the next age. |
| `requires` | no | List of quest ids that must be completed first. |
| `age` | no | Overrides the file's `age` for this one quest. |

Every task accepts `count` (default 1) and `label` (custom text instead of the
auto-generated description).

## Task types

Most tasks read the vanilla statistics, so progress made before a quest is visible
counts too. Wherever a `target` is accepted you can give one id, a list of ids
(summed), or a tag such as `#minecraft:logs`.

| Type | Fields | Counts |
|---|---|---|
| `mine` | `target`: block | blocks mined |
| `craft` | `target`: item | items crafted, smelted or smithed |
| `use` | `target`: item | items used, placed or eaten |
| `break` | `target`: item | tools worn out |
| `pickup` | `target`: item | items picked up off the ground (not from chests) |
| `drop` | `target`: item | items dropped |
| `kill` | `target`: entity | mobs killed |
| `killed_by` | `target`: entity | deaths to that mob |
| `custom` | `target`: custom stat | any `minecraft:custom` stat, e.g. `minecraft:jump`, `minecraft:bell_ring`. Stats ending in `_one_cm` are counted in blocks. |
| `stat` | `stat`: stat type, `target` | any stat type, including ones from other mods |
| `advancement` | `advancement`: id | 1 when the advancement is done |
| `has_item` | `item`: item or tag | how many the player is carrying right now |
| `xp_level` | — | the player's experience level |
| `biome` | `biome`: id, list or tag | 1 once the player has visited a match |
| `biomes_visited` | — | number of different biomes visited |
| `dimension` | `dimension`: id | 1 once the player has been there |
| `y_level` | `below` or `above`: Y | 1 once the player has been that low / high |
| `effect` | `effect`: id | 1 while the player has that effect |

Optional `divisor` scales a stat down (e.g. `damage_blocked_by_shield` is stored
in tenths, so `"divisor": 10`).

The mod also adds three custom stats you can use with `custom`:
`progressionmod:quests_completed`, `progressionmod:ages_reached` and
`progressionmod:biomes_visited`.

## Checking your quests

When the server starts and after every `/reload`, the mod logs a warning for any
quest that references an unknown item, block, entity, stat, tag, advancement, biome,
effect or quest. Search the log for `Quest '`.

Keep the mining tiers in mind when choosing an age: copper ore needs a stone
pickaxe, iron and lapis a copper one, gold, redstone and emerald an iron one,
diamond a gold one, amethyst ore a diamond one, ancient debris an amethyst one, and
endium ore a netherite one. Don't ask for items from a later age: the player can't
pick them up yet.

## Removing built-in quests

Override a built-in file with an empty list. The datapack needs the same path, for
example `data/progressionmod/progression/quests/04_iron.json` containing
`{"age": "iron", "quests": []}`. You can also replace a single quest by reusing its id.

## Commands

| Command | Who | Does |
|---|---|---|
| `/quests` | everyone | opens the quest book (default key: **K**) |
| `/quests book` | everyone | gives a new quest book |
| `/quests info <player>` | ops | shows age and quest counts |
| `/quests age <player> <age\|ascended>` | ops | sets the player's age |
| `/quests complete <player> <quest id>` | ops | completes a quest (and gives its rewards) |
| `/quests reset <player>` | ops | wipes the player's quest progress |
