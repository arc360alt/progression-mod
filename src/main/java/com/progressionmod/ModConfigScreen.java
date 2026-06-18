package com.progressionmod;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ModConfigScreen extends Screen {

    private final Screen parent;

    public ModConfigScreen(Screen parent) {
        super(Component.translatable("text.autoconfig.progressionmod.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.get();
        int cx = this.width / 2;
        int bw = 260;
        int bh = 20;
        int x = cx - bw / 2;
        int y = this.height / 4;

        addRenderableWidget(new StringWidget(cx - 150, y - 30, 300, bh, this.title, this.font));

        addRenderableWidget(CycleButton.onOffBuilder(config.showMiningTierMessages)
            .create(x, y, bw, bh,
                Component.translatable("text.autoconfig.progressionmod.option.showMiningTierMessages"),
                (btn, val) -> config.showMiningTierMessages = val));

        addRenderableWidget(CycleButton.onOffBuilder(config.showHandBreakWarning)
            .create(x, y + 24, bw, bh,
                Component.translatable("text.autoconfig.progressionmod.option.showHandBreakWarning"),
                (btn, val) -> config.showHandBreakWarning = val));

        addRenderableWidget(CycleButton.onOffBuilder(config.stickDropFromLeaves)
            .create(x, y + 48, bw, bh,
                Component.translatable("text.autoconfig.progressionmod.option.stickDropFromLeaves"),
                (btn, val) -> config.stickDropFromLeaves = val));

        addRenderableWidget(CycleButton.onOffBuilder(config.improvedFlintFromGravel)
            .create(x, y + 72, bw, bh,
                Component.translatable("text.autoconfig.progressionmod.option.improvedFlintFromGravel"),
                (btn, val) -> config.improvedFlintFromGravel = val));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> onClose())
            .bounds(cx - 75, this.height - 28, 150, bh)
            .build());
    }

    @Override
    public void onClose() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
        this.minecraft.setScreenAndShow(parent);
    }
}
