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

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean stickDropFromLeaves = false;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean improvedFlintFromGravel = false;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean ageItemLocking = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean showQuestHud = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public HudPosition questHudPosition = HudPosition.TOP_LEFT;

    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
    public int questHudQuestCount = 3;

    /** Where the quest HUD sits on screen. */
    public enum HudPosition {
        TOP_LEFT("Top Left"),
        TOP_CENTER("Top Center"),
        TOP_RIGHT("Top Right"),
        MIDDLE_LEFT("Middle Left"),
        MIDDLE_RIGHT("Middle Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right");

        public final String displayName;

        HudPosition(String displayName) {
            this.displayName = displayName;
        }

        public boolean isLeft() { return this == TOP_LEFT || this == MIDDLE_LEFT || this == BOTTOM_LEFT; }
        public boolean isRight() { return this == TOP_RIGHT || this == MIDDLE_RIGHT || this == BOTTOM_RIGHT; }
        public boolean isTop() { return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT; }
        public boolean isBottom() { return this == BOTTOM_LEFT || this == BOTTOM_RIGHT; }

        public HudPosition next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    @Override
    public void validatePostLoad() {
        if (questHudPosition == null) questHudPosition = HudPosition.TOP_LEFT;
        questHudQuestCount = Math.max(1, Math.min(5, questHudQuestCount));
    }

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}