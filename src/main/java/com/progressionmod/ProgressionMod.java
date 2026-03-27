package com.progressionmod;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.items.ModItems;
import com.progressionmod.items.ModItemGroup;
import com.progressionmod.worldgen.AmethystOreGeneration;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressionMod implements ModInitializer {

    public static final String MOD_ID = "progressionmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Progression Overhaul loading...");
        ModConfig.register();
        RecipeUnlocker.register();
        ModBlocks.registerBlocks();       // Blocks must be registered before items
        ModItems.registerItems();
        ModItemGroup.registerItemGroup();
        GrassStringLoot.register();
        AmethystOreGeneration.register(); // World gen last
        LOGGER.info("Progression Overhaul loaded!");
    }
}
