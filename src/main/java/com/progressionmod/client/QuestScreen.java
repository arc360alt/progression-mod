package com.progressionmod.client;

import com.progressionmod.ModConfig;
import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import com.progressionmod.quest.QuestNetworking;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The quest book. Left: the ages. Right: the selected age's unlocks, its quest list,
 * and details of the selected quest.
 */
@Environment(EnvType.CLIENT)
public class QuestScreen extends Screen {

    private static final int SIDEBAR_W = 116;
    private static final int AGE_ROW_H = 19;
    private static final int QUEST_ROW_H = 24;
    private static final int HEADER_H = 46;
    private static final int DETAIL_H = 96;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF9A9A9A;
    private static final int DARK_GRAY = 0xFF5E5E5E;
    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFFF55;

    private int left, top, panelW, panelH;
    private int contentX, contentW, listTop, listBottom, detailTop;

    private int selectedAge;
    private QuestDefinition selected;
    private double scroll;

    private Button trackButton;
    private Button hudButton;

    public QuestScreen() {
        super(Component.literal("Quest Book"));
        selectedAge = Math.min(ClientQuestState.age, Age.COUNT - 1);
        selected = firstOpenQuest(selectedAge);
    }

    @Override
    protected void init() {
        panelW = Math.min(460, width - 12);
        panelH = Math.min(270, height - 12);
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
        contentX = left + SIDEBAR_W + 8;
        contentW = panelW - SIDEBAR_W - 14;
        listTop = top + 6 + HEADER_H;
        detailTop = top + panelH - DETAIL_H - 6;
        listBottom = detailTop - 4;

        int buttonY = top + panelH - 26;
        hudButton = addRenderableWidget(Button.builder(hudLabel(), b -> {
            ModConfig config = ModConfig.get();
            config.showQuestHud = !config.showQuestHud;
            AutoConfig.getConfigHolder(ModConfig.class).save();
            b.setMessage(hudLabel());
        }).bounds(left + 6, buttonY - 22, SIDEBAR_W - 8, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + 6, buttonY, SIDEBAR_W - 8, 20).build());
        trackButton = addRenderableWidget(Button.builder(Component.literal("Track"), b -> toggleTrack())
                .bounds(left + panelW - 78, top + panelH - 28, 70, 20).build());
        clampScroll();
    }

    private Component hudLabel() {
        return Component.literal("HUD: " + (ModConfig.get().showQuestHud ? "Shown" : "Hidden"));
    }

    private void toggleTrack() {
        if (selected == null) return;
        String id = selected.id().equals(ClientQuestState.tracked) ? "" : selected.id();
        ClientQuestState.tracked = id;
        ClientPlayNetworking.send(new QuestNetworking.TrackPayload(id));
    }

    @Override
    public boolean isPauseScreen() {
        // Keep the integrated server running so progress updates live while the book is open.
        return false;
    }

    @Override
    public void tick() {
        // Definitions may be replaced by a /reload while the screen is open.
        if (selected != null) selected = ClientQuestState.QUESTS.get(selected.id());
        boolean trackable = selected != null && !ClientQuestState.isComplete(selected)
                && selected.age() <= ClientQuestState.age;
        trackButton.active = trackable;
        trackButton.setMessage(Component.literal(selected != null && selected.id().equals(ClientQuestState.tracked)
                ? "Untrack" : "Track"));
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(left, top, left + panelW, top + panelH, 0xF0181818);
        g.outline(left, top, panelW, panelH, 0xFF505050);
        g.fill(left + 2, top + 2, left + SIDEBAR_W, top + panelH - 2, 0xFF222222);

        drawSidebar(g, mouseX, mouseY);
        drawHeader(g, mouseX, mouseY);
        drawQuestList(g, mouseX, mouseY);
        drawDetails(g, mouseX, mouseY);

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, left + 8, top + 7, YELLOW, true);
        int total = ClientQuestState.QUESTS.size();
        String overall = ClientQuestState.completed.size() + "/" + total;
        g.text(font, overall, left + SIDEBAR_W - 4 - font.width(overall), top + 7, GRAY, true);

        for (int i = 0; i < Age.COUNT; i++) {
            Age age = Age.values()[i];
            int y = ageRowY(i);
            boolean hovered = isIn(mouseX, mouseY, left + 4, y, SIDEBAR_W - 6, AGE_ROW_H - 1);
            if (i == selectedAge) g.fill(left + 4, y, left + SIDEBAR_W - 2, y + AGE_ROW_H - 1, 0xFF3A3A3A);
            else if (hovered) g.fill(left + 4, y, left + SIDEBAR_W - 2, y + AGE_ROW_H - 1, 0xFF2E2E2E);

            boolean reached = i <= ClientQuestState.age;
            g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(age.icon)), left + 6, y + 1);
            int color = reached ? 0xFF000000 | age.color : DARK_GRAY;
            g.text(font, age.displayName, left + 25, y + 5, color, true);

            String mark;
            int markColor;
            if (i < ClientQuestState.age) { mark = "✔"; markColor = GREEN; }
            else if (i == ClientQuestState.age) { mark = "▶"; markColor = YELLOW; }
            else { mark = "✖"; markColor = DARK_GRAY; }
            g.text(font, mark, left + SIDEBAR_W - 4 - font.width(mark), y + 5, markColor, true);
        }
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Age age = Age.values()[selectedAge];
        int x = contentX, y = top + 7;
        g.text(font, age.displayName, x, y, 0xFF000000 | age.color, true);

        List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(selectedAge);
        int done = ClientQuestState.completedInAge(selectedAge);
        String status;
        if (selectedAge < ClientQuestState.age) status = "Complete";
        else if (selectedAge == ClientQuestState.age) status = "Current age";
        else status = "Locked: finish the " + Age.nameOf(selectedAge - 1);
        g.text(font, status, x + font.width(age.displayName) + 8, y, GRAY, true);
        String count = done + "/" + quests.size() + " quests";
        g.text(font, count, contentX + contentW - font.width(count), y, WHITE, true);
        QuestHud.bar(g, x, y + 11, contentW, 3, quests.isEmpty() ? 1 : (double) done / quests.size(), 0xFF000000 | age.color);

        g.text(font, "Unlocks:", x, y + 23, GRAY, true);
        int ix = x + font.width("Unlocks:") + 4;
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(age.itemTag)) {
            if (ix + 16 > contentX + contentW) break;
            ItemStack stack = new ItemStack(holder);
            g.item(stack, ix, y + 18);
            if (isIn(mouseX, mouseY, ix, y + 18, 16, 16)) g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
            ix += 17;
        }
        if (ix == x + font.width("Unlocks:") + 4) g.text(font, "Nothing (starting age)", ix, y + 23, DARK_GRAY, true);
    }

    private void drawQuestList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(selectedAge);
        g.fill(contentX - 2, listTop - 2, contentX + contentW + 2, listBottom, 0xFF101010);
        if (quests.isEmpty()) {
            g.centeredText(font, "No quests in this age", contentX + contentW / 2, listTop + 10, GRAY);
            return;
        }

        g.enableScissor(contentX - 2, listTop - 1, contentX + contentW + 2, listBottom - 1);
        boolean futureAge = selectedAge > ClientQuestState.age;
        int y = listTop - (int) scroll;
        for (QuestDefinition quest : quests) {
            if (y + QUEST_ROW_H >= listTop && y <= listBottom) {
                boolean hovered = isIn(mouseX, mouseY, contentX, y, contentW, QUEST_ROW_H - 1)
                        && mouseY >= listTop && mouseY < listBottom;
                if (quest == selected) g.fill(contentX, y, contentX + contentW, y + QUEST_ROW_H - 1, 0xFF34343F);
                else if (hovered) g.fill(contentX, y, contentX + contentW, y + QUEST_ROW_H - 1, 0xFF262626);
                drawQuestRow(g, quest, y, futureAge);
            }
            y += QUEST_ROW_H;
        }
        g.disableScissor();

        // Scrollbar
        int contentH = quests.size() * QUEST_ROW_H;
        int viewH = listBottom - listTop;
        if (contentH > viewH) {
            int barH = Math.max(12, viewH * viewH / contentH);
            int barY = listTop + (int) ((viewH - barH) * (scroll / (contentH - viewH)));
            g.fill(contentX + contentW - 2, barY, contentX + contentW + 1, barY + barH, 0xFF808080);
        }
    }

    private void drawQuestRow(GuiGraphicsExtractor g, QuestDefinition quest, int y, boolean futureAge) {
        boolean complete = ClientQuestState.isComplete(quest);
        boolean available = ClientQuestState.isAvailable(quest);
        g.item(QuestHud.stackOf(quest), contentX + 3, y + 3);

        String right;
        int rightColor;
        if (complete) { right = "✔"; rightColor = GREEN; }
        else if (futureAge) { right = "Locked"; rightColor = DARK_GRAY; }
        else if (!available) { right = "Needs earlier quest"; rightColor = DARK_GRAY; }
        else { right = ClientQuestState.questProgress(quest) + "/" + quest.totalCount(); rightColor = GRAY; }

        int titleColor = complete ? 0xFFA0E0A0 : futureAge || !available ? DARK_GRAY : WHITE;
        String title = (quest.id().equals(ClientQuestState.tracked) ? "★ " : "") + quest.title()
                + (quest.optional() ? " (optional)" : "");
        int titleW = contentW - 30 - font.width(right) - 8;
        g.text(font, font.plainSubstrByWidth(title, titleW), contentX + 23, y + 4, titleColor, true);
        g.text(font, right, contentX + contentW - 6 - font.width(right), y + 4, rightColor, true);
        if (!futureAge) {
            QuestHud.bar(g, contentX + 23, y + 15, contentW - 30, 3,
                    complete ? 1 : ClientQuestState.fraction(quest), complete ? 0xFF3FAF3F : 0xFF55FF55);
        }
    }

    private void drawDetails(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = contentX, y = detailTop, w = contentW;
        g.fill(x - 2, y, x + w + 2, top + panelH - 4, 0xFF1F1F26);
        if (selected == null) {
            g.text(font, "Select a quest to see its details.", x + 4, y + 6, GRAY, true);
            return;
        }
        QuestDefinition quest = selected;
        boolean futureAge = quest.age() > ClientQuestState.age;
        boolean complete = ClientQuestState.isComplete(quest);

        g.enableScissor(x - 2, y, x + w + 2, top + panelH - 4);
        g.item(QuestHud.stackOf(quest), x + 2, y + 4);
        g.text(font, quest.title(), x + 22, y + 4, complete ? GREEN : YELLOW, true);
        String state = complete ? "Completed" : futureAge ? "Reach the " + Age.nameOf(quest.age()) + " first"
                : quest.optional() ? "Optional" : "Required for next age";
        g.text(font, state, x + 22, y + 14, GRAY, true);

        int ty = y + 26;
        if (!quest.description().isEmpty()) {
            List<FormattedCharSequence> lines = font.split(Component.literal(quest.description()), w - 8);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                g.text(font, lines.get(i), x + 4, ty, 0xFFD0D0D0, true);
                ty += 10;
            }
        }

        int[] progress = ClientQuestState.taskProgress(quest);
        List<QuestDefinition.Task> tasks = quest.tasks();
        int rewardsY = top + panelH - 24;
        // More than three tasks don't fit in one column, so split them into two.
        int columns = tasks.size() > 3 ? 2 : 1;
        int columnW = (w - 4) / columns;
        int rows = (tasks.size() + columns - 1) / columns;
        for (int i = 0; i < tasks.size(); i++) {
            int col = i / rows, row = i % rows;
            int cx = x + 4 + col * columnW;
            int cy = ty + row * 10;
            if (cy + 10 > rewardsY) continue;
            QuestDefinition.Task task = tasks.get(i);
            boolean done = complete || progress[i] >= task.count();
            String amount = (complete ? task.count() : progress[i]) + "/" + task.count();
            // Rows level with the Track button must stop short of it.
            boolean besideButton = col == columns - 1 && cy + 10 > trackButton.getY();
            int amountX = cx + columnW - 8 - font.width(amount) - (besideButton ? 80 : 0);
            Component label = Component.literal(done ? "✔ " : "• ").append(QuestText.describe(task));
            g.text(font, font.plainSubstrByWidth(label.getString(), amountX - cx - 4), cx, cy, done ? GREEN : WHITE, true);
            g.text(font, amount, amountX, cy, done ? GREEN : GRAY, true);
        }
        ty += Math.min(rows, Math.max(0, (rewardsY - ty) / 10)) * 10;

        List<String> missing = new ArrayList<>();
        for (String req : quest.requires()) {
            QuestDefinition r = ClientQuestState.QUESTS.get(req);
            if (r != null && !ClientQuestState.isComplete(r)) missing.add(r.title());
        }
        if (!missing.isEmpty() && ty + 10 <= rewardsY) {
            g.text(font, font.plainSubstrByWidth("Requires: " + String.join(", ", missing), w - 90),
                    x + 4, ty, 0xFFFF7777, true);
        }

        if (!quest.rewardItems().isEmpty() || quest.rewardXp() > 0) {
            g.text(font, "Rewards:", x + 4, rewardsY + 6, GRAY, true);
            int rx = x + 8 + font.width("Rewards:");
            for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(reward.item()), reward.count());
                g.item(stack, rx, rewardsY + 1);
                g.itemDecorations(font, stack, rx, rewardsY + 1);
                if (isIn(mouseX, mouseY, rx, rewardsY + 1, 16, 16)) g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                rx += 18;
            }
            if (quest.rewardXp() > 0) g.text(font, "+" + quest.rewardXp() + " XP", rx + 2, rewardsY + 6, 0xFF80FF20, true);
        }
        g.disableScissor();
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        double mx = event.x(), my = event.y();
        if (event.button() != 0) return false;

        for (int i = 0; i < Age.COUNT; i++) {
            if (isIn(mx, my, left + 4, ageRowY(i), SIDEBAR_W - 6, AGE_ROW_H - 1)) {
                if (selectedAge != i) {
                    selectedAge = i;
                    selected = firstOpenQuest(i);
                    scroll = 0;
                }
                click();
                return true;
            }
        }

        if (mx >= contentX && mx < contentX + contentW && my >= listTop && my < listBottom) {
            int index = (int) ((my - listTop + scroll) / QUEST_ROW_H);
            List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(selectedAge);
            if (index >= 0 && index < quests.size()) {
                selected = quests.get(index);
                click();
                if (doubleClick && trackButton.active) toggleTrack();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= contentX && mouseX < contentX + contentW && mouseY >= listTop && mouseY < listBottom) {
            scroll -= scrollY * QUEST_ROW_H;
            clampScroll();
            return true;
        }
        if (mouseX >= left && mouseX < left + SIDEBAR_W) {
            int next = selectedAge - (int) Math.signum(scrollY);
            if (next >= 0 && next < Age.COUNT && next != selectedAge) {
                selectedAge = next;
                selected = firstOpenQuest(next);
                scroll = 0;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (ProgressionModKeys.OPEN_QUESTS.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void clampScroll() {
        int contentH = ClientQuestState.QUESTS.forAge(selectedAge).size() * QUEST_ROW_H;
        int max = Math.max(0, contentH - (listBottom - listTop));
        scroll = Math.max(0, Math.min(max, scroll));
    }

    private int ageRowY(int index) {
        return top + 20 + index * AGE_ROW_H;
    }

    private static QuestDefinition firstOpenQuest(int age) {
        List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(age);
        for (QuestDefinition quest : quests) {
            if (!ClientQuestState.isComplete(quest) && ClientQuestState.isAvailable(quest)) return quest;
        }
        return quests.isEmpty() ? null : quests.getFirst();
    }

    private void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private static boolean isIn(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
