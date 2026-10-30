package com.progressionmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.progressionmod.ModConfig;
import com.progressionmod.items.ModItems;
import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import com.progressionmod.quest.QuestNetworking;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
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
import java.util.Locale;

/**
 * The quest book. Left: the ages and HUD settings. Right: the selected age's header
 * and unlocks, a filterable quest list, and details of the selected quest.
 */
@Environment(EnvType.CLIENT)
public class QuestScreen extends Screen {

    private static final int SIDEBAR_W = 124;
    private static final int TITLE_H = 20;
    private static final int QUEST_ROW_H = 27;
    private static final int DETAIL_H = 100;
    private static final int BUTTON_H = 18;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int LIGHT = 0xFFD0D0D8;
    private static final int GRAY = 0xFF9A9AA4;
    private static final int DARK_GRAY = 0xFF5E5E66;
    private static final int SUBTLE = 0xFF85858F;
    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int GOLD = 0xFFFFC94D;

    private enum Filter { ALL("All"), ACTIVE("Active"), DONE("Done");
        final String label;
        Filter(String label) { this.label = label; }
    }

    private int left, top, panelW, panelH;
    private int contentX, contentW, headerTop, toolbarY, listTop, listBottom, detailTop;
    private int ageRowsTop, ageRowH;

    private int selectedAge;
    private QuestDefinition selected;
    private Filter filter = Filter.ALL;
    private String search = "";
    private double scroll;
    private boolean draggingScrollbar;

    private QuestButton trackButton;
    private EditBox searchBox;

    public QuestScreen() {
        super(Component.literal("Quest Book"));
        selectedAge = Math.min(ClientQuestState.age, Age.COUNT - 1);
        selected = firstOpenQuest(selectedAge);
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        panelW = Math.min(480, width - 10);
        panelH = Math.min(296, height - 10);
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
        contentX = left + SIDEBAR_W + 8;
        contentW = panelW - SIDEBAR_W - 14;
        headerTop = top + TITLE_H + 5;
        toolbarY = headerTop + 42;
        listTop = toolbarY + BUTTON_H + 4;
        detailTop = top + panelH - DETAIL_H - 5;
        listBottom = detailTop - 4;

        // Sidebar buttons, bottom up.
        int bx = left + 5, bw = SIDEBAR_W - 8;
        int doneY = top + panelH - 5 - BUTTON_H;
        int positionY = doneY - BUTTON_H - 3;
        int hudY = positionY - BUTTON_H - 3;
        addRenderableWidget(new QuestButton(bx, doneY, bw, BUTTON_H, Component.translatable("gui.done"), 0xFF8A8A9A,
                b -> onClose()));
        addRenderableWidget(new QuestButton(bx, positionY, bw, BUTTON_H, positionLabel(), 0xFF6FA8FF, b -> {
            ModConfig config = ModConfig.get();
            config.questHudPosition = config.questHudPosition.next();
            saveConfig();
            b.setMessage(positionLabel());
        }));
        addRenderableWidget(new QuestButton(bx, hudY, bw, BUTTON_H, hudLabel(), 0xFF6FA8FF, b -> {
            ModConfig config = ModConfig.get();
            config.showQuestHud = !config.showQuestHud;
            saveConfig();
            b.setMessage(hudLabel());
        }));

        // Age list fills the space between the title bar and the buttons.
        ageRowsTop = top + TITLE_H + 6;
        ageRowH = Math.max(14, Math.min(21, (hudY - 6 - ageRowsTop) / Age.COUNT));

        // Toolbar: filter tabs and search.
        int tx = contentX;
        for (Filter f : Filter.values()) {
            int w = font.width(f.label) + 14;
            addRenderableWidget(new QuestButton(tx, toolbarY, w, BUTTON_H, Component.literal(f.label), 0xFFFFC94D, b -> {
                filter = f;
                scroll = 0;
                keepSelectionVisible();
            }).selectedWhen(() -> filter == f));
            tx += w + 3;
        }
        int searchW = Math.min(130, contentX + contentW - tx - 6);
        searchBox = new EditBox(font, contentX + contentW - searchW, toolbarY + 1, searchW, BUTTON_H - 2, Component.literal("Search"));
        searchBox.setHint(Component.literal("Search quests...").withColor(DARK_GRAY));
        searchBox.setMaxLength(48);
        searchBox.setValue(search);
        searchBox.setResponder(text -> {
            search = text;
            scroll = 0;
            keepSelectionVisible();
            tick();
        });
        addRenderableWidget(searchBox);

        trackButton = addRenderableWidget(new QuestButton(left + panelW - 84, top + panelH - 5 - BUTTON_H - 2, 78, BUTTON_H + 2,
                Component.literal("Track"), GOLD, b -> toggleTrack()));
        clampScroll();
        tick();
    }

    private static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    private Component hudLabel() {
        return Component.literal("HUD: " + (ModConfig.get().showQuestHud ? "Shown" : "Hidden"));
    }

    private Component positionLabel() {
        return Component.literal("HUD: " + ModConfig.get().questHudPosition.displayName);
    }

    private void toggleTrack() {
        if (selected == null) return;
        String id = selected.id().equals(ClientQuestState.tracked) ? "" : selected.id();
        ClientQuestState.tracked = id;
        ClientPlayNetworking.send(new QuestNetworking.TrackPayload(id));
        tick();
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
        boolean tracked = selected != null && selected.id().equals(ClientQuestState.tracked);
        trackButton.active = trackable;
        trackButton.setMessage(Component.literal(tracked ? "✖ Untrack" : "★ Track"));
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private List<QuestDefinition> visibleQuests() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        List<QuestDefinition> out = new ArrayList<>();
        for (QuestDefinition quest : ClientQuestState.QUESTS.forAge(selectedAge)) {
            boolean done = ClientQuestState.isComplete(quest);
            if (filter == Filter.ACTIVE && done) continue;
            if (filter == Filter.DONE && !done) continue;
            if (!query.isEmpty() && !quest.title().toLowerCase(Locale.ROOT).contains(query)
                    && !quest.description().toLowerCase(Locale.ROOT).contains(query)) continue;
            out.add(quest);
        }
        return out;
    }

    private Age age() {
        return Age.values()[selectedAge];
    }

    private int accent() {
        return 0xFF000000 | age().color;
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int accent = accent();
        // Frame
        g.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF050507);
        g.fill(left, top, left + panelW, top + panelH, 0xF4141418);
        g.outline(left, top, panelW, panelH, mix(accent, 0xFF000000, 0.45f));
        g.fill(left + 1, top + 1, left + SIDEBAR_W, top + panelH - 1, 0xFF1B1B21);
        g.fill(left + SIDEBAR_W, top + TITLE_H, left + SIDEBAR_W + 1, top + panelH - 1, 0xFF2A2A32);

        drawTitleBar(g, accent);
        drawSidebar(g, mouseX, mouseY);
        drawHeader(g, mouseX, mouseY);
        drawQuestList(g, mouseX, mouseY);
        drawDetails(g, mouseX, mouseY);

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void drawTitleBar(GuiGraphicsExtractor g, int accent) {
        g.fillGradient(left + 1, top + 1, left + panelW - 1, top + TITLE_H, 0xFF26262E, 0xFF1B1B21);
        g.fill(left + 1, top + TITLE_H, left + panelW - 1, top + TITLE_H + 1, accent);
        g.item(new ItemStack(ModItems.QUEST_BOOK), left + 4, top + 2);
        g.text(font, title, left + 23, top + 6, GOLD, true);

        int total = ClientQuestState.QUESTS.size();
        int done = ClientQuestState.completed.size();
        int percent = total == 0 ? 0 : done * 100 / total;
        String summary = done + "/" + total + " quests  " + percent + "%";
        int sx = left + panelW - 6 - font.width(summary);
        g.text(font, summary, sx, top + 6, LIGHT, true);
        int barW = Math.min(120, sx - (left + SIDEBAR_W + 8) - 8);
        if (barW > 20) QuestHud.bar(g, sx - 8 - barW, top + 9, barW, 3, total == 0 ? 0 : (double) done / total, GOLD);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        for (int i = 0; i < Age.COUNT; i++) {
            Age age = Age.values()[i];
            int y = ageRowsTop + i * ageRowH;
            int x = left + 4, w = SIDEBAR_W - 7, h = ageRowH - 2;
            int color = 0xFF000000 | age.color;
            boolean reached = i <= ClientQuestState.age;
            boolean hovered = isIn(mouseX, mouseY, x, y, w, h);

            if (i == selectedAge) g.fill(x, y, x + w, y + h, 0xFF30303A);
            else if (hovered) g.fill(x, y, x + w, y + h, 0xFF26262E);
            if (reached) g.fill(x, y, x + 2, y + h, i == ClientQuestState.age ? color : mix(color, 0xFF000000, 0.4f));

            int iconY = y + (h - 16) / 2;
            g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(age.icon)), x + 4, iconY);
            int textY = ageRowH >= 18 ? y + 2 : y + (h - 8) / 2;
            g.text(font, age.displayName, x + 23, textY, reached ? color : DARK_GRAY, true);

            String mark;
            int markColor;
            if (i < ClientQuestState.age) { mark = "✔"; markColor = GREEN; }
            else if (i == ClientQuestState.age) { mark = "▶"; markColor = YELLOW; }
            else { mark = "✖"; markColor = DARK_GRAY; }
            g.text(font, mark, x + w - 3 - font.width(mark), textY, markColor, true);

            if (ageRowH >= 18) {
                int total = ClientQuestState.QUESTS.forAge(i).size();
                int done = ClientQuestState.completedInAge(i);
                QuestHud.bar(g, x + 23, y + h - 5, w - 28, 2, total == 0 ? 0 : (double) done / total,
                        reached ? color : 0xFF404048);
            }
        }
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Age age = age();
        int accent = accent();
        int x = contentX, y = headerTop;
        g.text(font, age.displayName, x, y, accent, true);

        List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(selectedAge);
        int done = ClientQuestState.completedInAge(selectedAge);
        String status;
        int statusColor;
        if (selectedAge < ClientQuestState.age) { status = "Complete"; statusColor = GREEN; }
        else if (selectedAge == ClientQuestState.age) { status = "Current age"; statusColor = YELLOW; }
        else { status = "Locked"; statusColor = DARK_GRAY; }
        pill(g, x + font.width(age.displayName) + 6, y - 1, status, statusColor);

        String count = done + "/" + quests.size();
        g.text(font, count, contentX + contentW - font.width(count), y, WHITE, true);
        QuestHud.bar(g, x, y + 11, contentW, 3, quests.isEmpty() ? 1 : (double) done / quests.size(), accent);

        int unlockY = y + 19;
        g.text(font, "Unlocks", x, unlockY + 5, GRAY, true);
        int ix = x + font.width("Unlocks") + 6;
        int startX = ix;
        List<ItemStack> unlocks = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(age.itemTag)) unlocks.add(new ItemStack(holder));
        int fits = Math.max(0, (contentX + contentW - ix + 3) / 19);
        // If they don't all fit, the last slot becomes a "+N" counter listing the rest.
        int shown = unlocks.size() > fits ? Math.max(0, fits - 1) : unlocks.size();
        for (int i = 0; i < shown; i++) {
            ItemStack stack = unlocks.get(i);
            g.fill(ix - 1, unlockY - 1, ix + 17, unlockY + 17, 0xFF1E1E26);
            g.item(stack, ix, unlockY);
            if (isIn(mouseX, mouseY, ix, unlockY, 16, 16)) g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
            ix += 19;
        }
        if (shown < unlocks.size()) {
            String more = "+" + (unlocks.size() - shown);
            g.fill(ix - 1, unlockY - 1, ix + 17, unlockY + 17, 0xFF1E1E26);
            g.centeredText(font, more, ix + 8, unlockY + 5, LIGHT);
            if (isIn(mouseX, mouseY, ix, unlockY, 16, 16)) {
                List<Component> names = new ArrayList<>();
                for (ItemStack stack : unlocks.subList(shown, unlocks.size())) names.add(stack.getHoverName());
                g.setComponentTooltipForNextFrame(font, names, mouseX, mouseY);
            }
        }
        if (unlocks.isEmpty()) g.text(font, "Nothing new: this is where it all begins", startX, unlockY + 5, DARK_GRAY, true);
    }

    private void drawQuestList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        List<QuestDefinition> quests = visibleQuests();
        g.fill(contentX - 2, listTop - 2, contentX + contentW + 2, listBottom, 0xFF0E0E12);
        if (quests.isEmpty()) {
            String message = ClientQuestState.QUESTS.forAge(selectedAge).isEmpty() ? "No quests in this age"
                    : "No quests match";
            g.centeredText(font, message, contentX + contentW / 2, (listTop + listBottom) / 2 - 4, DARK_GRAY);
            return;
        }

        boolean futureAge = selectedAge > ClientQuestState.age;
        int scrollbarW = needsScrollbar(quests) ? 5 : 0;
        g.enableScissor(contentX - 2, listTop - 1, contentX + contentW + 2, listBottom - 1);
        int y = listTop - (int) scroll;
        for (QuestDefinition quest : quests) {
            if (y + QUEST_ROW_H >= listTop && y <= listBottom) {
                boolean hovered = isIn(mouseX, mouseY, contentX, y, contentW - scrollbarW, QUEST_ROW_H - 1)
                        && mouseY >= listTop && mouseY < listBottom;
                drawQuestRow(g, quest, contentX, y, contentW - scrollbarW, futureAge, hovered);
            }
            y += QUEST_ROW_H;
        }
        g.disableScissor();

        if (scrollbarW > 0) {
            int[] bar = scrollbar(quests);
            int sx = contentX + contentW - 3;
            g.fill(sx, listTop, sx + 4, listBottom - 1, 0xFF1C1C22);
            boolean hover = isIn(mouseX, mouseY, sx - 1, bar[0], 6, bar[1]) || draggingScrollbar;
            g.fill(sx, bar[0], sx + 4, bar[0] + bar[1], hover ? 0xFFB0B0BC : 0xFF6A6A76);
        }
    }

    private void drawQuestRow(GuiGraphicsExtractor g, QuestDefinition quest, int x, int y, int w,
                              boolean futureAge, boolean hovered) {
        boolean complete = ClientQuestState.isComplete(quest);
        boolean available = ClientQuestState.isAvailable(quest);
        boolean isSelected = quest == selected;
        int h = QUEST_ROW_H - 2;

        int background = isSelected ? 0xFF2C2C38 : hovered ? 0xFF22222A : 0xFF18181E;
        g.fill(x, y, x + w, y + h, background);
        int stripe = complete ? 0xFF3FAF3F : futureAge || !available ? 0xFF3A3A42 : accent();
        g.fill(x, y, x + 2, y + h, stripe);
        if (isSelected) g.outline(x, y, w, h, mix(accent(), 0xFF000000, 0.3f));

        g.item(QuestHud.stackOf(quest), x + 5, y + (h - 16) / 2);

        // Right-hand status
        String pillText = null;
        int pillColor = 0;
        if (complete) { pillText = "DONE"; pillColor = GREEN; }
        else if (futureAge) { pillText = "LOCKED"; pillColor = DARK_GRAY; }
        else if (!available) { pillText = "NEEDS PREVIOUS"; pillColor = DARK_GRAY; }
        int rightX = x + w - 5;
        if (pillText != null) {
            rightX -= font.width(pillText) + 6;
            pill(g, rightX, y + 3, pillText, pillColor);
        } else {
            String progress = ClientQuestState.questProgress(quest) + "/" + quest.totalCount();
            rightX -= font.width(progress);
            g.text(font, progress, rightX, y + 4, GRAY, true);
        }
        if (quest.optional() && !complete) {
            rightX -= font.width("OPTIONAL") + 10;
            pill(g, rightX, y + 3, "OPTIONAL", 0xFF6FA8FF);
        }

        int titleColor = complete ? 0xFFA6E8A6 : futureAge || !available ? DARK_GRAY : WHITE;
        String title = (quest.id().equals(ClientQuestState.tracked) ? "★ " : "") + quest.title();
        g.text(font, font.plainSubstrByWidth(title, rightX - x - 30), x + 25, y + 4, titleColor, true);

        // Second line: what to do next.
        String sub;
        if (complete) sub = "Completed";
        else if (futureAge) sub = "Reach the " + Age.nameOf(quest.age()) + " to start";
        else {
            int task = ClientQuestState.currentTask(quest);
            sub = task < 0 ? "Finishing..." : "▸ " + QuestText.describe(quest.tasks().get(task)).getString();
        }
        g.text(font, font.plainSubstrByWidth(sub, w - 32), x + 25, y + 14, complete ? 0xFF6E9E6E : SUBTLE, false);

        if (!futureAge && !complete) {
            QuestHud.bar(g, x + 25, y + h - 2, w - 30, 1, ClientQuestState.fraction(quest), 0xFF55FF55);
        }
    }

    private void drawDetails(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = contentX, y = detailTop, w = contentW;
        int bottom = top + panelH - 5;
        g.fill(x - 2, y, x + w + 2, bottom, 0xFF1C1C24);
        g.fill(x - 2, y, x + w + 2, y + 1, mix(accent(), 0xFF000000, 0.3f));
        if (selected == null) {
            g.centeredText(font, "Select a quest to see its details", x + w / 2, y + DETAIL_H / 2 - 4, GRAY);
            return;
        }
        QuestDefinition quest = selected;
        boolean futureAge = quest.age() > ClientQuestState.age;
        boolean complete = ClientQuestState.isComplete(quest);

        g.enableScissor(x - 2, y, x + w + 2, bottom);
        g.fill(x + 1, y + 4, x + 21, y + 24, 0xFF14141A);
        g.item(QuestHud.stackOf(quest), x + 3, y + 6);
        g.text(font, quest.title(), x + 26, y + 5, complete ? GREEN : GOLD, true);
        String state = complete ? "Completed" : futureAge ? "Reach the " + Age.nameOf(quest.age()) + " first"
                : quest.optional() ? "Optional: not needed for the next age" : "Required for the next age";
        g.text(font, state, x + 26, y + 15, GRAY, false);

        int ty = y + 28;
        if (!quest.description().isEmpty()) {
            List<FormattedCharSequence> lines = font.split(Component.literal(quest.description()), w - 8);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                g.text(font, lines.get(i), x + 3, ty, LIGHT, false);
                ty += 10;
            }
        }
        ty += 2;

        int rewardsY = bottom - 20;
        int[] progress = ClientQuestState.taskProgress(quest);
        List<QuestDefinition.Task> tasks = quest.tasks();
        // More than three tasks don't fit in one column, so split them into two.
        int columns = tasks.size() > 3 ? 2 : 1;
        int columnW = (w - 4) / columns;
        int rows = (tasks.size() + columns - 1) / columns;
        int rowH = rows * 12 <= rewardsY - ty ? 12 : 10;
        for (int i = 0; i < tasks.size(); i++) {
            int col = i / rows, row = i % rows;
            int cx = x + 3 + col * columnW;
            int cy = ty + row * rowH;
            if (cy + 9 > rewardsY) continue;
            QuestDefinition.Task task = tasks.get(i);
            int value = complete ? task.count() : progress[i];
            boolean done = value >= task.count();
            String amount = value + "/" + task.count();
            boolean besideButton = col == columns - 1 && cy + 10 > trackButton.getY();
            int amountX = cx + columnW - 8 - font.width(amount) - (besideButton ? 86 : 0);
            String label = (done ? "✔ " : "• ") + QuestText.describe(task).getString();
            g.text(font, font.plainSubstrByWidth(label, amountX - cx - 4), cx, cy, done ? GREEN : WHITE, true);
            g.text(font, amount, amountX, cy, done ? GREEN : GRAY, true);
            if (rowH == 12 && task.count() > 1 && !done) {
                QuestHud.bar(g, cx + 8, cy + 9, amountX - cx - 12, 1, (double) value / task.count(), 0xFF55FF55);
            }
        }

        List<String> missing = new ArrayList<>();
        for (String req : quest.requires()) {
            QuestDefinition r = ClientQuestState.QUESTS.get(req);
            if (r != null && !ClientQuestState.isComplete(r)) missing.add(r.title());
        }

        int rx = x + 3;
        if (!missing.isEmpty()) {
            g.text(font, font.plainSubstrByWidth("Requires: " + String.join(", ", missing), w - 96),
                    rx, rewardsY + 6, 0xFFFF7777, true);
        } else if (!quest.rewardItems().isEmpty() || quest.rewardXp() > 0) {
            g.text(font, "Rewards", rx, rewardsY + 6, GRAY, true);
            rx += font.width("Rewards") + 6;
            for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(reward.item()), reward.count());
                g.fill(rx - 1, rewardsY + 1, rx + 17, rewardsY + 19, 0xFF14141A);
                g.item(stack, rx, rewardsY + 2);
                g.itemDecorations(font, stack, rx, rewardsY + 2);
                if (isIn(mouseX, mouseY, rx, rewardsY + 2, 16, 16)) g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                rx += 20;
            }
            if (quest.rewardXp() > 0) g.text(font, "+" + quest.rewardXp() + " XP", rx + 2, rewardsY + 6, 0xFF80FF20, true);
        }
        g.disableScissor();
    }

    /** A small rounded-looking label with a tinted background. */
    private void pill(GuiGraphicsExtractor g, int x, int y, String text, int color) {
        int w = font.width(text) + 6;
        g.fill(x, y, x + w, y + 10, mix(color, 0xFF000000, 0.75f));
        g.outline(x, y, w, 10, mix(color, 0xFF000000, 0.35f));
        g.text(font, text, x + 3, y + 1, color, false);
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        double mx = event.x(), my = event.y();
        if (event.button() != 0) return false;

        for (int i = 0; i < Age.COUNT; i++) {
            if (isIn(mx, my, left + 4, ageRowsTop + i * ageRowH, SIDEBAR_W - 7, ageRowH - 2)) {
                selectAge(i);
                click();
                return true;
            }
        }

        List<QuestDefinition> quests = visibleQuests();
        if (needsScrollbar(quests) && isIn(mx, my, contentX + contentW - 5, listTop, 8, listBottom - listTop)) {
            draggingScrollbar = true;
            dragScrollbar(my, quests);
            return true;
        }
        if (mx >= contentX && mx < contentX + contentW && my >= listTop && my < listBottom) {
            int index = (int) ((my - listTop + scroll) / QUEST_ROW_H);
            if (index >= 0 && index < quests.size()) {
                selected = quests.get(index);
                tick();
                click();
                if (doubleClick && trackButton.active) toggleTrack();
                return true;
            }
        }
        searchBox.setFocused(false);
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar) {
            dragScrollbar(event.y(), visibleQuests());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        return super.mouseReleased(event);
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
            if (next >= 0 && next < Age.COUNT) selectAge(next);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!searchBox.isFocused()) {
            if (ProgressionModKeys.OPEN_QUESTS.matches(event)) {
                onClose();
                return true;
            }
            int key = event.key();
            if (key == InputConstants.KEY_UP || key == InputConstants.KEY_DOWN) {
                moveSelection(key == InputConstants.KEY_DOWN ? 1 : -1);
                return true;
            }
            if (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_RIGHT) {
                int next = selectedAge + (key == InputConstants.KEY_RIGHT ? 1 : -1);
                if (next >= 0 && next < Age.COUNT) selectAge(next);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void selectAge(int index) {
        if (index == selectedAge) return;
        selectedAge = index;
        selected = firstOpenQuest(index);
        scroll = 0;
        keepSelectionVisible();
        tick();
    }

    private void moveSelection(int delta) {
        List<QuestDefinition> quests = visibleQuests();
        if (quests.isEmpty()) return;
        int index = quests.indexOf(selected);
        index = index < 0 ? 0 : Math.max(0, Math.min(quests.size() - 1, index + delta));
        selected = quests.get(index);
        keepSelectionVisible();
        tick();
        click();
    }

    private void keepSelectionVisible() {
        List<QuestDefinition> quests = visibleQuests();
        int index = quests.indexOf(selected);
        if (index < 0) {
            selected = quests.isEmpty() ? selected : quests.getFirst();
            index = quests.indexOf(selected);
        }
        if (index >= 0) {
            int rowTop = index * QUEST_ROW_H;
            int viewH = listBottom - listTop;
            if (rowTop < scroll) scroll = rowTop;
            else if (rowTop + QUEST_ROW_H > scroll + viewH) scroll = rowTop + QUEST_ROW_H - viewH;
        }
        clampScroll();
    }

    private boolean needsScrollbar(List<QuestDefinition> quests) {
        return quests.size() * QUEST_ROW_H > listBottom - listTop;
    }

    /** Top and height of the scrollbar thumb. */
    private int[] scrollbar(List<QuestDefinition> quests) {
        int contentH = quests.size() * QUEST_ROW_H;
        int viewH = listBottom - listTop;
        int barH = Math.max(14, viewH * viewH / contentH);
        int barY = listTop + (int) ((viewH - barH) * (scroll / (contentH - viewH)));
        return new int[] { barY, barH };
    }

    private void dragScrollbar(double mouseY, List<QuestDefinition> quests) {
        int contentH = quests.size() * QUEST_ROW_H;
        int viewH = listBottom - listTop;
        int barH = scrollbar(quests)[1];
        double fraction = (mouseY - listTop - barH / 2.0) / (viewH - barH);
        scroll = fraction * (contentH - viewH);
        clampScroll();
    }

    private void clampScroll() {
        int contentH = visibleQuests().size() * QUEST_ROW_H;
        int max = Math.max(0, contentH - (listBottom - listTop));
        scroll = Math.max(0, Math.min(max, scroll));
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

    /** Blends two ARGB colors; {@code t = 0} gives {@code a}, {@code t = 1} gives {@code b}. */
    static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int gr = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return 0xFF000000 | r << 16 | gr << 8 | bl;
    }
}
