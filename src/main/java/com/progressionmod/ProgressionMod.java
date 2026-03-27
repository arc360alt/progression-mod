package com.progressionmod;

import com.progressionmod.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.progressionmod.items.ModItemGroup;
import com.progressionmod.GrassStringLoot;
import com.progressionmod.ModConfig;
import com.progressionmod.RecipeUnlocker;

public class ProgressionMod implements ModInitializer {

    public static final String MOD_ID = "progressionmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Progression Overhaul loading...");
        ModConfig.register();
        RecipeUnlocker.register();
        ModItems.registerItems();
        ModItemGroup.registerItemGroup();
        GrassStringLoot.register();
        LOGGER.info("Progression Overhaul loaded!");
    }
}
