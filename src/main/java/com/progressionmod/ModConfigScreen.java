package com.progressionmod;

import com.progressionmod.ModConfig.HudPosition;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ModConfigScreen extends Screen {

    private static final int ROW_H = 24;
    private static final int BUTTON_H = 20;

    private final Screen parent;
    private int leftX, rightX, columnW, rowsTop;

    public ModConfigScreen(Screen parent) {
        super(Component.translatable("text.autoconfig.progressionmod.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.get();
        columnW = Math.min(200, (width - 30) / 2);
        leftX = width / 2 - columnW - 5;
        rightX = width / 2 + 5;
        rowsTop = Math.max(44, height / 2 - 80);

        // ── Gameplay ──
        toggle(leftX, 0, "showMiningTierMessages", config.showMiningTierMessages, v -> config.showMiningTierMessages = v);
        toggle(leftX, 1, "showHandBreakWarning", config.showHandBreakWarning, v -> config.showHandBreakWarning = v);
        toggle(leftX, 2, "stickDropFromLeaves", config.stickDropFromLeaves, v -> config.stickDropFromLeaves = v);
        toggle(leftX, 3, "improvedFlintFromGravel", config.improvedFlintFromGravel, v -> config.improvedFlintFromGravel = v);
        toggle(leftX, 4, "ageItemLocking", config.ageItemLocking, v -> config.ageItemLocking = v);

        // ── Quest HUD ──
        toggle(rightX, 0, "showQuestHud", config.showQuestHud, v -> config.showQuestHud = v);
        add(CycleButton.<HudPosition>builder(p -> Component.literal(p.displayName), config.questHudPosition)
                .withValues(HudPosition.values())
                .create(rightX, rowY(1), columnW, BUTTON_H, option("questHudPosition"),
                        (b, v) -> config.questHudPosition = v), "questHudPosition");
        add(CycleButton.<Integer>builder(n -> Component.literal(String.valueOf(n)), config.questHudQuestCount)
                .withValues(1, 2, 3, 4, 5)
                .create(rightX, rowY(2), columnW, BUTTON_H, option("questHudQuestCount"),
                        (b, v) -> config.questHudQuestCount = v), "questHudQuestCount");

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> onClose())
            .bounds(width / 2 - 75, height - 28, 150, BUTTON_H)
            .build());
    }

    private int rowY(int row) {
        return rowsTop + row * ROW_H;
    }

    private static Component option(String key) {
        return Component.translatable("text.autoconfig.progressionmod.option." + key);
    }

    private void toggle(int x, int row, String key, boolean value, Consumer<Boolean> setter) {
        add(CycleButton.onOffBuilder(value)
                .create(x, rowY(row), columnW, BUTTON_H, option(key), (b, v) -> setter.accept(v)), key);
    }

    private void add(AbstractWidget widget, String key) {
        widget.setTooltip(Tooltip.create(Component.translatable("text.autoconfig.progressionmod.option." + key + ".@Tooltip")));
        addRenderableWidget(widget);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, 15, 0xFFFFFFFF);
        g.text(font, Component.literal("Gameplay"), leftX, rowsTop - 13, 0xFFFFC94D, true);
        g.text(font, Component.literal("Quest HUD"), rightX, rowsTop - 13, 0xFFFFC94D, true);
    }

    @Override
    public void onClose() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
        this.minecraft.setScreenAndShow(parent);
    }
}
