package com.progressionmod.client;

import com.progressionmod.ModConfig;
import com.progressionmod.ModConfig.HudPosition;
import com.progressionmod.client.compat.NeoCraftHudCompat;
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
import net.minecraft.world.item.Items;

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
    private static final int BANNER_H = 30;
    private static final long BANNER_MS = 3500;

    /**
     * Set by {@link NeoCraftHudCompat} when NeoCraft UI's HUD API registers our elements.
     * While false nothing NeoCraft-related is touched, so the mod works without it.
     */
    public static boolean neoCraftActive = false;

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
        boolean editor = neoCraftActive && NeoCraftHudCompat.editorOpen();
        // Hide only while the F3 text overlay is open. showDebugScreen() is also true when any
        // debug entry is set to "always on" (e.g. hitboxes via F3+B), which would hide the HUD for good.
        // In NeoCraft's HUD editor the panel is always shown so it can be placed.
        boolean showPanel = ClientQuestState.synced && (config.showQuestHud || editor)
                && !mc.debugEntries.isOverlayVisible() && !(mc.gui.screen() instanceof QuestScreen);

        List<QuestDefinition> quests = ClientQuestState.age < Age.COUNT
                ? ClientQuestState.hudQuests(config.questHudQuestCount) : List.of();
        int height = HEADER_H + quests.size() * ROW_H + (quests.isEmpty() ? 0 : 2);

        if (neoCraftActive) {
            extractWithNeoCraft(g, mc.font, position, showPanel, editor, quests, height);
            return;
        }

        int x = anchorX(g.guiWidth(), position, WIDTH);
        int y = anchorY(g.guiHeight(), mc, position, height);

        if (showPanel) {
            drawPanel(g, mc.font, x, y, height, quests);
            drawBanner(g, mc.font, position, x, position.isBottom() ? y - 34 : y + height + 4);
        } else {
            int bannerY = anchorY(g.guiHeight(), mc, position, BANNER_H);
            drawBanner(g, mc.font, position, x, bannerY);
        }
    }

    // ── NeoCraft UI integration ──────────────────────────────────────────────

    /**
     * With NeoCraft UI the panel and banner are separate HUD-editor elements. Each draws at
     * its default box ({@link #panelBox}, {@link #bannerBox}) and NeoCraft moves and scales it.
     */
    private static void extractWithNeoCraft(GuiGraphicsExtractor g, Font font, HudPosition position, boolean showPanel,
                                            boolean editor, List<QuestDefinition> quests, int height) {
        if (showPanel) {
            int[] box = panelBox(g.guiWidth(), g.guiHeight());
            if (NeoCraftHudCompat.pushPanel(g)) drawPanel(g, font, box[0], box[1], height, quests);
            NeoCraftHudCompat.popPanel(g);
        }

        int[] box = bannerBox(g.guiWidth(), g.guiHeight());
        if (NeoCraftHudCompat.pushBanner(g)) {
            if (editor && banners.isEmpty()) {
                drawBannerBox(g, font, box[0], box[1], Component.literal("Quest Complete!"),
                        Component.literal("Example Quest"), new ItemStack(Items.BOOK), 0xFF55FF55);
            } else {
                drawBanner(g, font, position, box[0], box[1]);
            }
        }
        NeoCraftHudCompat.popBanner(g);
    }

    /** Height of the panel with every quest slot filled; the editor box uses it so it doesn't jump around. */
    private static int fullPanelHeight() {
        return HEADER_H + ModConfig.get().questHudQuestCount * ROW_H + 2;
    }

    /** Where the panel is drawn before NeoCraft moves it: {x, y, width, height} in GUI pixels. */
    public static int[] panelBox(int screenWidth, int screenHeight) {
        HudPosition position = ModConfig.get().questHudPosition;
        int h = fullPanelHeight();
        return new int[] { anchorX(screenWidth, position, WIDTH), anchorY(screenHeight, Minecraft.getInstance(), position, h), WIDTH, h };
    }

    /** Where the banner is drawn before NeoCraft moves it: next to the panel's default box. */
    public static int[] bannerBox(int screenWidth, int screenHeight) {
        HudPosition position = ModConfig.get().questHudPosition;
        int[] panel = panelBox(screenWidth, screenHeight);
        int y = position.isBottom() ? panel[1] - BANNER_H - 4 : panel[1] + panel[3] + 4;
        return new int[] { panel[0], y, WIDTH, BANNER_H };
    }

    // ── Placement ────────────────────────────────────────────────────────────

    static int anchorX(int screenWidth, HudPosition position, int width) {
        if (position.isLeft()) return MARGIN;
        if (position.isRight()) return screenWidth - width - MARGIN;
        return (screenWidth - width) / 2;
    }

    static int anchorY(int screenHeight, Minecraft mc, HudPosition position, int height) {
        if (position.isTop()) {
            // Top right shares the corner with status effect icons and toasts; move below them.
            // (No player when NeoCraft's editor asks from the title screen.)
            if (position == HudPosition.TOP_RIGHT && mc.player != null) {
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
        if (position.isBottom()) return screenHeight - height - 52;
        return (screenHeight - height) / 2;
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

        // Slide in for 250ms, hold, slide out for the last 250ms.
        double slide = Math.min(1, Math.min(elapsed, BANNER_MS - elapsed) / 250.0);
        int hidden = (int) ((1 - slide) * (WIDTH + MARGIN * 2));
        if (position.isLeft()) x -= hidden;
        else if (position.isRight()) x += hidden;
        else y -= (int) ((1 - slide) * (BANNER_H + y + 2));

        drawBannerBox(g, font, x, y, banner.title(), banner.subtitle(), banner.icon(), banner.color());
    }

    private static void drawBannerBox(GuiGraphicsExtractor g, Font font, int x, int y, Component title,
                                      Component subtitle, ItemStack icon, int color) {
        g.fill(x, y, x + WIDTH, y + BANNER_H, 0xE0101010);
        g.outline(x, y, WIDTH, BANNER_H, color);
        g.fill(x, y, x + 2, y + BANNER_H, color);
        g.item(icon, x + 7, y + 7);
        g.text(font, title, x + 28, y + 6, color, true);
        g.text(font, font.plainSubstrByWidth(subtitle.getString(), WIDTH - 32), x + 28, y + 17, 0xFFFFFFFF, true);
    }

    static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, double fraction, int color) {
        g.fill(x, y, x + w, y + h, 0xFF303030);
        int filled = (int) Math.round(w * Math.max(0, Math.min(1, fraction)));
        if (filled > 0) g.fill(x, y, x + filled, y + h, color);
    }
}
