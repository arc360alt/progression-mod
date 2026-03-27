package com.progressionmod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "progressionmod")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean showMiningTierMessages = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean showHandBreakWarning = true;

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}