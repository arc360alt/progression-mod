package com.progressionmod;

import com.progressionmod.client.EndiumMarkerRenderer;
import com.progressionmod.entities.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ProgressionModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ENDIUM_MARKER, EndiumMarkerRenderer::new);
    }
}
