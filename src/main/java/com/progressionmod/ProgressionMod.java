package com.progressionmod;

import com.progressionmod.blocks.ModBlocks;
import com.progressionmod.entities.ModEntities;
import com.progressionmod.items.EndiumLocatorItem;
import com.progressionmod.items.ModItems;
import com.progressionmod.items.ModItemGroup;
import com.progressionmod.worldgen.AmethystOreGeneration;
import com.progressionmod.worldgen.EndiumOreGeneration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.Level;
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
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModEntities.register();
        ModItemGroup.registerItemGroup();
        GrassStringLoot.register();
        PrimitiveLoot.register();
        AmethystOreLoot.register();
        SmithingTemplateLoot.register();
        AmethystOreGeneration.register();
        EndiumOreGeneration.register();

        // Endium Locator: expire glow markers every tick; stale markers from a previous
        // session are cleaned up on the first tick the End level is present.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var end = server.getLevel(Level.END);
            if (end != null) EndiumLocatorItem.tickEndLevel(end);
        });

        LOGGER.info("Progression Overhaul loaded!");
    }
}
