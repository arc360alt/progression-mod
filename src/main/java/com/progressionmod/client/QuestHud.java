package com.progressionmod.client;

import com.progressionmod.ModConfig;
import com.progressionmod.ModConfig.HudPosition;
import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Quest HUD: current age, age progress, and the active quests with the task each one
 * is waiting on. Its corner is set in the config. Quest completions show a banner
 * next to it.
 */
@Environment(EnvType.CLIENT)
public final class QuestHud {

    private static final int WIDTH = 170;
    private static final int MARGIN = 4;
    private static final int HEADER_H = 22;
    private static final int ROW_H = 31;
    private static final long BANNER_MS = 3500;

    private record Banner(Component title, Component subtitle, ItemStack icon, int color) {}

    private static final Deque<Banner> banners = new ArrayDeque<>();
    private static long bannerStart = 0;

    private QuestHud() {}

    public static void showQuestComplete(QuestDefinition quest) {
        banners.add(new Banner(Component.literal("Quest Complete!"), Component.literal(quest.title()),
                stackOf(quest), 0xFF55FF55));
    }

    static ItemStack stackOf(QuestDefinition quest) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(quest.icon()));
    }

    public static void extract(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ModConfig config = ModConfig.get();
        HudPosition position = config.questHudPosition;
        // Hide only while the F3 text overlay is open. showDebugScreen() is also true when any
        // debug entry is set to "always on" (e.g. hitboxes via F3+B), which would hide the HUD for good.
        boolean showPanel = ClientQuestState.synced && config.showQuestHud
                && !mc.debugEntries.isOverlayVisible() && !(mc.gui.screen() instanceof QuestScreen);

        List<QuestDefinition> quests = ClientQuestState.age < Age.COUNT
                ? ClientQuestState.hudQuests(config.questHudQuestCount) : List.of();
        int height = HEADER_H + quests.size() * ROW_H + (quests.isEmpty() ? 0 : 2);
        int x = anchorX(g, position, WIDTH);
        int y = anchorY(g, mc, position, height);

        if (showPanel) {
            drawPanel(g, mc.font, x, y, height, quests);
            drawBanner(g, mc.font, position, x, position.isBottom() ? y - 34 : y + height + 4);
        } else {
            int bannerY = anchorY(g, mc, position, 30);
            drawBanner(g, mc.font, position, x, bannerY);
        }
    }

    // ── Placement ────────────────────────────────────────────────────────────

    static int anchorX(GuiGraphicsExtractor g, HudPosition position, int width) {
        if (position.isLeft()) return MARGIN;
        if (position.isRight()) return g.guiWidth() - width - MARGIN;
        return (g.guiWidth() - width) / 2;
    }

    static int anchorY(GuiGraphicsExtractor g, Minecraft mc, HudPosition position, int height) {
        if (position.isTop()) {
            // Top right shares the corner with status effect icons and toasts; move below them.
            if (position == HudPosition.TOP_RIGHT) {
                int y = MARGIN;
                if (!mc.player.getActiveEffects().isEmpty()) {
                    boolean harmful = mc.player.getActiveEffects().stream().anyMatch(e -> !e.getEffect().value().isBeneficial());
                    y += harmful ? 52 : 26;
                }
                int toastSlots = mc.gui.toastManager().occupiedSlots.length();
                return Math.max(y, toastSlots * Toast.SLOT_HEIGHT + MARGIN);
            }
            return MARGIN;
        }
        // Keep clear of the hotbar, health and food bars at the bottom.
        if (position.isBottom()) return g.guiHeight() - height - 52;
        return (g.guiHeight() - height) / 2;
    }

    // ── Panel ────────────────────────────────────────────────────────────────

    private static void drawPanel(GuiGraphicsExtractor g, Font font, int x, int y, int height, List<QuestDefinition> quests) {
        Age current = Age.byIndex(ClientQuestState.age);
        int accent = current != null ? 0xFF000000 | current.color : 0xFFFF55FF;

        g.fill(x, y, x + WIDTH, y + height, 0xA0101014);
        g.outline(x, y, WIDTH, height, 0x50FFFFFF);
        g.fill(x, y, x + 2, y + height, accent);

        if (current == null) {
            g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(Age.ENDIUM.icon)), x + 5, y + 3);
            g.text(font, Component.literal("Ascended"), x + 24, y + 4, 0xFFFF55FF, true);
            g.text(font, Component.literal("Every age complete"), x + 24, y + 13, 0xFFAAAAAA, false);
            return;
        }

        int total = ClientQuestState.QUESTS.forAge(ClientQuestState.age).size();
        int done = ClientQuestState.completedInAge(ClientQuestState.age);
        g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(current.icon)), x + 5, y + 3);
        g.text(font, current.displayName, x + 24, y + 4, accent, true);
        String count = done + "/" + total;
        g.text(font, count, x + WIDTH - 5 - font.width(count), y + 4, 0xFFCCCCCC, true);
        bar(g, x + 24, y + 15, WIDTH - 29, 2, total == 0 ? 1 : (double) done / total, accent);

        int rowY = y + HEADER_H;
        for (QuestDefinition quest : quests) {
            drawQuestRow(g, font, quest, x, rowY);
            rowY += ROW_H;
        }
    }

    private static void drawQuestRow(GuiGraphicsExtractor g, Font font, QuestDefinition quest, int x, int y) {
        g.fill(x + 3, y, x + WIDTH - 3, y + 1, 0x30FFFFFF);
        g.item(stackOf(quest), x + 5, y + 4);

        boolean pinned = quest.id().equals(ClientQuestState.tracked);
        String progress = ClientQuestState.progressText(quest) + (quest.tasks().size() > 1 ? " tasks" : "");
        String title = (pinned ? "★ " : "") + quest.title();
        int titleWidth = WIDTH - 29 - font.width(progress) - 6;
        g.text(font, font.plainSubstrByWidth(title, titleWidth), x + 24, y + 3, pinned ? 0xFFFFFF55 : 0xFFFFFFFF, true);
        g.text(font, progress, x + WIDTH - 5 - font.width(progress), y + 3, 0xFFAAAAAA, true);

        // The task this quest is waiting on.
        int taskIndex = ClientQuestState.currentTask(quest);
        if (taskIndex >= 0) {
            QuestDefinition.Task task = quest.tasks().get(taskIndex);
            int value = ClientQuestState.taskProgress(quest)[taskIndex];
            String amount = task.count() > 1 ? " " + value + "/" + task.count() : "";
            String text = "▸ " + QuestText.describe(task).getString();
            int maxW = WIDTH - 29 - font.width(amount);
            g.text(font, font.plainSubstrByWidth(text, maxW) + amount, x + 24, y + 13, 0xFFB8B8C8, false);
        }
        bar(g, x + 24, y + 24, WIDTH - 29, 2, ClientQuestState.fraction(quest), 0xFF55FF55);
    }

    // ── Banner ───────────────────────────────────────────────────────────────

    /** Draws the current banner near (x, y), sliding in from the side the HUD is anchored to. */
    private static void drawBanner(GuiGraphicsExtractor g, Font font, HudPosition position, int x, int y) {
        if (banners.isEmpty()) return;
        long now = Util.getMillis();
        if (bannerStart == 0) bannerStart = now;
        long elapsed = now - bannerStart;
        if (elapsed > BANNER_MS) {
            banners.poll();
            bannerStart = banners.isEmpty() ? 0 : now;
            return;
        }
        Banner banner = banners.peek();
        int w = Math.max(WIDTH, Math.max(font.width(banner.title()), font.width(banner.subtitle())) + 36);
        int h = 30;
        x = anchorX(g, position, w);

        // Slide in for 250ms, hold, slide out for the last 250ms.
        double slide = Math.min(1, Math.min(elapsed, BANNER_MS - elapsed) / 250.0);
        int hidden = (int) ((1 - slide) * (w + MARGIN * 2));
        if (position.isLeft()) x -= hidden;
        else if (position.isRight()) x += hidden;
        else y -= (int) ((1 - slide) * (h + y + 2));

        g.fill(x, y, x + w, y + h, 0xE0101010);
        g.outline(x, y, w, h, banner.color());
        g.fill(x, y, x + 2, y + h, banner.color());
        g.item(banner.icon(), x + 7, y + 7);
        g.text(font, banner.title(), x + 28, y + 6, banner.color(), true);
        g.text(font, banner.subtitle(), x + 28, y + 17, 0xFFFFFFFF, true);
    }

    static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, double fraction, int color) {
        g.fill(x, y, x + w, y + h, 0xFF303030);
        int filled = (int) Math.round(w * Math.max(0, Math.min(1, fraction)));
        if (filled > 0) g.fill(x, y, x + filled, y + h, color);
    }
}
