package com.progressionmod.client;

import com.progressionmod.ModConfig;
import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Top-left HUD panel: current age, age progress, and up to three active quests.
 * Also shows a banner when a quest is completed.
 */
@Environment(EnvType.CLIENT)
public final class QuestHud {

    private static final int WIDTH = 150;
    private static final int MAX_QUESTS = 3;
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
        boolean showPanel = ClientQuestState.synced && ModConfig.get().showQuestHud
                && !mc.getDebugOverlay().showDebugScreen() && !(mc.gui.screen() instanceof QuestScreen);
        int bannerY = showPanel ? drawPanel(g, mc.font) + 4 : 4;
        drawBanner(g, mc.font, bannerY);
    }

    /** Draws the panel and returns its bottom edge. */
    private static int drawPanel(GuiGraphicsExtractor g, Font font) {
        int age = ClientQuestState.age;
        Age current = Age.byIndex(age);
        List<QuestDefinition> quests = ClientQuestState.hudQuests(MAX_QUESTS);

        int x = 4, y = 4;
        int height = 24 + quests.size() * 22 + (quests.isEmpty() ? 0 : 2);
        g.fill(x, y, x + WIDTH, y + height, 0x90000000);
        g.outline(x, y, WIDTH, height, 0x40FFFFFF);

        if (current == null) {
            g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(Age.ENDIUM.icon)), x + 3, y + 3);
            g.text(font, Component.literal("Ascended"), x + 22, y + 8, 0xFFFF55FF, true);
            return y + height;
        }

        int total = ClientQuestState.QUESTS.forAge(age).size();
        int done = ClientQuestState.completedInAge(age);
        g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(current.icon)), x + 3, y + 3);
        g.text(font, current.displayName, x + 22, y + 4, 0xFF000000 | current.color, true);
        String count = done + "/" + total;
        g.text(font, count, x + WIDTH - 4 - font.width(count), y + 4, 0xFFCCCCCC, true);
        bar(g, x + 22, y + 15, WIDTH - 26, 3, total == 0 ? 1 : (double) done / total, 0xFF000000 | current.color);

        int rowY = y + 24;
        for (QuestDefinition quest : quests) {
            g.item(stackOf(quest), x + 3, rowY + 1);
            boolean pinned = quest.id().equals(ClientQuestState.tracked);
            String progress = ClientQuestState.questProgress(quest) + "/" + quest.totalCount();
            int titleWidth = WIDTH - 26 - font.width(progress) - 6;
            String title = (pinned ? "★ " : "") + quest.title();
            g.text(font, font.plainSubstrByWidth(title, titleWidth), x + 22, rowY + 2, pinned ? 0xFFFFFF55 : 0xFFFFFFFF, true);
            g.text(font, progress, x + WIDTH - 4 - font.width(progress), rowY + 2, 0xFFAAAAAA, true);
            bar(g, x + 22, rowY + 13, WIDTH - 26, 2, ClientQuestState.fraction(quest), 0xFF55FF55);
            rowY += 22;
        }
        return y + height;
    }

    /** Draws the current banner at the left edge, sliding in from off-screen. */
    private static void drawBanner(GuiGraphicsExtractor g, Font font, int y) {
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
        int w = Math.max(WIDTH, Math.max(font.width(banner.title()), font.width(banner.subtitle())) + 36);
        int h = 30;
        int x = (int) (-w + (w + 4) * slide);
        g.fill(x, y, x + w, y + h, 0xE0101010);
        g.outline(x, y, w, h, banner.color());
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
