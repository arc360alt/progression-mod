#!/usr/bin/env python3
"""
fix_recipes.py — Converts Minecraft recipe JSON files from 1.21.1 format to 1.21.5 format.

Changes made:
  - Shaped recipes: key ingredient objects {"item": "mod:id"} -> plain string "mod:id"
                    tag ingredient objects {"tag": "mod:id"}  -> plain string "#mod:id"
  - Shapeless recipes: ingredients list of objects -> list of plain strings
  - Smelting/blasting: ingredient object -> plain string
  - Result: {"id": "mod:item"} stays the same (result format did NOT change)

Usage:
  python3 fix_recipes.py                          # fixes all .json in current dir
  python3 fix_recipes.py path/to/recipe/folder    # fixes all .json in that folder
  python3 fix_recipes.py file1.json file2.json    # fixes specific files
"""

import json
import sys
import os
import glob
from pathlib import Path


def ingredient_to_string(ingredient):
    """
    Convert a 1.21.1 ingredient object to a 1.21.5 plain string.
    {"item": "minecraft:flint"} -> "minecraft:flint"
    {"tag": "minecraft:logs"}   -> "#minecraft:logs"
    Already a string -> return as-is
    """
    if isinstance(ingredient, str):
        return ingredient
    if isinstance(ingredient, dict):
        if "item" in ingredient:
            return ingredient["item"]
        if "tag" in ingredient:
            return "#" + ingredient["tag"]
    # Fallback — leave unchanged (will cause a warning)
    print(f"  WARNING: Unknown ingredient format: {ingredient}")
    return ingredient


def fix_recipe(data):
    """Fix a single parsed recipe dict in place. Returns the modified dict."""
    recipe_type = data.get("type", "")

    # ── Shaped crafting ──────────────────────────────────────────────────────
    if recipe_type == "minecraft:crafting_shaped":
        if "key" in data:
            new_key = {}
            for k, v in data["key"].items():
                new_key[k] = ingredient_to_string(v)
            data["key"] = new_key

    # ── Shapeless crafting ───────────────────────────────────────────────────
    elif recipe_type == "minecraft:crafting_shapeless":
        if "ingredients" in data:
            data["ingredients"] = [ingredient_to_string(i) for i in data["ingredients"]]

    # ── Smelting / Blasting / Smoking / Campfire ─────────────────────────────
    elif recipe_type in (
        "minecraft:smelting",
        "minecraft:blasting",
        "minecraft:smoking",
        "minecraft:campfire_cooking",
    ):
        if "ingredient" in data:
            data["ingredient"] = ingredient_to_string(data["ingredient"])

    # ── Stonecutting ─────────────────────────────────────────────────────────
    elif recipe_type == "minecraft:stonecutting":
        if "ingredient" in data:
            data["ingredient"] = ingredient_to_string(data["ingredient"])

    # ── Smithing Transform ───────────────────────────────────────────────────
    elif recipe_type == "minecraft:smithing_transform":
        for field in ("template", "base", "addition"):
            if field in data:
                data[field] = ingredient_to_string(data[field])

    return data


def fix_file(path: Path):
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
    except json.JSONDecodeError as e:
        print(f"  SKIP (invalid JSON): {path} — {e}")
        return False

    original = json.dumps(data, sort_keys=True)
    fixed = fix_recipe(data)
    result = json.dumps(fixed, sort_keys=True)

    if original == result:
        print(f"  unchanged: {path.name}")
        return False

    with open(path, "w", encoding="utf-8") as f:
        json.dump(fixed, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"  FIXED:     {path.name}")
    return True


def main():
    targets = []

    if len(sys.argv) == 1:
        # No arguments — fix all JSON in current directory
        targets = list(Path(".").glob("*.json"))
    else:
        for arg in sys.argv[1:]:
            p = Path(arg)
            if p.is_dir():
                targets.extend(p.rglob("*.json"))
            elif p.is_file():
                targets.append(p)
            else:
                print(f"WARNING: '{arg}' is not a file or directory, skipping.")

    if not targets:
        print("No JSON files found.")
        return

    fixed_count = 0
    for path in sorted(targets):
        changed = fix_file(path)
        if changed:
            fixed_count += 1

    print(f"\nDone. {fixed_count}/{len(targets)} file(s) modified.")


if __name__ == "__main__":
    main()