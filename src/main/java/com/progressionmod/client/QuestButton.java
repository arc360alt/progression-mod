package com.progressionmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Flat, dark button used throughout the quest book, with an accent color, optional icon and "selected" state. */
@Environment(EnvType.CLIENT)
public class QuestButton extends AbstractButton {

    private final Consumer<QuestButton> onPress;
    private int accent;
    private ItemStack icon = ItemStack.EMPTY;
    private Supplier<Boolean> selected = () -> false;

    public QuestButton(int x, int y, int width, int height, Component label, int accent, Consumer<QuestButton> onPress) {
        super(x, y, width, height, label);
        this.accent = accent;
        this.onPress = onPress;
    }

    public QuestButton icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    public QuestButton selectedWhen(Supplier<Boolean> selected) {
        this.selected = selected;
        return this;
    }

    public void setAccent(int accent) {
        this.accent = accent;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.accept(this);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hovered = active && isHoveredOrFocused();
        boolean isSelected = selected.get();

        int background = !active ? 0xFF1A1A1A : isSelected ? 0xFF33333F : hovered ? 0xFF2C2C34 : 0xFF222228;
        int border = !active ? 0xFF2E2E2E : isSelected || hovered ? accent : QuestScreen.mix(accent, 0xFF000000, 0.55f);
        g.fill(x, y, x + w, y + h, background);
        g.outline(x, y, w, h, border);
        // Accent strip along the bottom for the selected / hovered state.
        if (isSelected || hovered) g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, accent);

        Font font = Minecraft.getInstance().font;
        int textColor = !active ? 0xFF5A5A5A : isSelected ? 0xFFFFFFFF : hovered ? 0xFFFFFFFF : 0xFFC8C8C8;
        int contentW = font.width(getMessage()) + (icon.isEmpty() ? 0 : 18);
        int cx = x + (w - contentW) / 2;
        if (!icon.isEmpty()) {
            g.item(icon, cx, y + (h - 16) / 2);
            cx += 18;
        }
        g.text(font, getMessage(), cx, y + (h - 8) / 2, textColor, active);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
