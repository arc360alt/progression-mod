package com.progressionmod.entities;

import com.progressionmod.ProgressionMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final ResourceKey<EntityType<?>> ENDIUM_MARKER_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "endium_marker"));

    public static final EntityType<EndiumMarkerEntity> ENDIUM_MARKER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ENDIUM_MARKER_KEY,
            EntityType.Builder.<EndiumMarkerEntity>of(EndiumMarkerEntity::new, MobCategory.MISC)
                    .sized(0.99f, 0.99f)
                    .noSave()
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(ENDIUM_MARKER_KEY)
    );

    public static void register() {
        ProgressionMod.LOGGER.info("Registering Endium Marker entity...");
    }
}
