package com.progressionmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.progressionmod.ProgressionMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class ProgressionModKeys {

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "progression"));

    public static final KeyMapping OPEN_QUESTS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.progressionmod.open_quests", InputConstants.KEY_K, CATEGORY));

    private ProgressionModKeys() {}

    public static void register() {
        // Class load registers the key mappings.
    }
}
