package com.progressionmod.client;

import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;

/**
 * The quest book's tree view: a pannable, zoomable canvas where each quest is a card and
 * arrows run from a quest to the quests it unlocks. Quests that are linked are laid out as
 * left-to-right trees; quests with no links share a grid underneath.
 *
 * <p>Clicking a card opens an info panel on the right with tasks, rewards and clickable
 * "Requires" / "Unlocks" links.
 */
@Environment(EnvType.CLIENT)
final class QuestTreeView {

    private static final int NODE_W = 108;
    private static final int NODE_H = 30;
    private static final int COL_W = 138;
    private static final int ROW_H = 40;
    private static final int GROUP_GAP = 24;
    private static final int GRID_COLUMNS = 4;
    private static final float MIN_ZOOM = 0.45f;
    private static final float MAX_ZOOM = 1.6f;

    private static final int GREEN = 0xFF55FF55;
    private static final int DONE_EDGE = 0xFF3FAF3F;
    private static final int GRAY = 0xFF9A9AA4;
    private static final int DARK_GRAY = 0xFF5E5E66;
    private static final int LIGHT = 0xFFD0D0D8;
    private static final int GOLD = 0xFFFFC94D;

    /** Where a quest's card sits, in canvas ("world") coordinates. */
    private record Node(QuestDefinition quest, int x, int y) {}
    private record Edge(Node from, Node to) {}
    private record Label(String text, int x, int y) {}
    /** A clickable line in the info panel that jumps to another quest. */
    private record Link(int x, int y, int w, int h, QuestDefinition quest) {}

    private final QuestScreen screen;
    private final Font font;

    private int x, y, w, h;
    private int age = -1;
    private int layoutKey;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private int minX, minY, maxX, maxY;

    private float camX, camY, zoom = 1f;
    private float targetCamX, targetCamY;
    private boolean animating;

    private boolean pressed, dragging;
    private double pressX, pressY;
    private float pressCamX, pressCamY;

    private boolean cardOpen;
    private final List<Link> links = new ArrayList<>();
    private Node hovered;

    QuestTreeView(QuestScreen screen, Font font) {
        this.screen = screen;
        this.font = font;
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    void setBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    void setAge(int age, QuestDefinition selected) {
        if (this.age != age) {
            this.age = age;
            layoutKey = 0;
            ensureLayout();
            zoom = 1f;
        }
        cardOpen = selected != null;
        focus(selected, false);
    }

    boolean isCardOpen() {
        return cardOpen && screen.selected() != null;
    }

    /** Info panel width: up to 170px, but never more than 45% of the canvas. */
    private int cardW() {
        return Math.max(130, Math.min(170, (int) (w * 0.45f)));
    }

    /** Screen rectangle of the info panel: {x, y, w, h}. */
    int[] cardBounds() {
        int cw = cardW();
        return new int[] { x + w - cw - 4, y + 4, cw, h - 8 };
    }

    /** Rebuilds the layout if the quests for this age changed (e.g. after /reload). */
    private void ensureLayout() {
        List<QuestDefinition> quests = ClientQuestState.QUESTS.forAge(age);
        int key = 1;
        for (QuestDefinition quest : quests) key = 31 * key + quest.id().hashCode() + quest.requires().hashCode();
        if (key == layoutKey && !nodes.isEmpty()) return;
        layoutKey = key;
        layout(quests);
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    private void layout(List<QuestDefinition> quests) {
        nodes.clear();
        edges.clear();
        labels.clear();

        Map<String, QuestDefinition> byId = new LinkedHashMap<>();
        for (QuestDefinition quest : quests) byId.put(quest.id(), quest);

        // Links within this age (requirements from earlier ages are shown in the info panel).
        Map<String, List<String>> parents = new HashMap<>();
        Map<String, List<String>> children = new HashMap<>();
        for (QuestDefinition quest : quests) {
            for (String req : quest.requires()) {
                if (!byId.containsKey(req) || req.equals(quest.id())) continue;
                parents.computeIfAbsent(quest.id(), k -> new ArrayList<>()).add(req);
                children.computeIfAbsent(req, k -> new ArrayList<>()).add(quest.id());
            }
        }

        // Group linked quests into connected components, in file order.
        List<List<String>> groups = new ArrayList<>();
        List<String> loners = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String id : byId.keySet()) {
            if (seen.contains(id)) continue;
            List<String> group = new ArrayList<>();
            Deque<String> stack = new ArrayDeque<>(List.of(id));
            while (!stack.isEmpty()) {
                String cur = stack.pop();
                if (!seen.add(cur)) continue;
                group.add(cur);
                stack.addAll(parents.getOrDefault(cur, List.of()));
                stack.addAll(children.getOrDefault(cur, List.of()));
            }
            if (group.size() == 1) loners.add(id);
            else groups.add(group);
        }

        Map<String, Integer> depth = new HashMap<>();
        int top = 0;
        for (List<String> group : groups) {
            // Column = longest chain of requirements leading to the quest.
            int maxDepth = 0;
            for (String id : group) maxDepth = Math.max(maxDepth, depthOf(id, parents, depth, new HashSet<>()));
            List<List<String>> columns = new ArrayList<>();
            for (int i = 0; i <= maxDepth; i++) columns.add(new ArrayList<>());
            List<String> ordered = new ArrayList<>(byId.keySet());
            ordered.retainAll(group);
            for (String id : ordered) columns.get(depth.get(id)).add(id);

            // Rows: each quest sits level with its parents where possible, without overlapping.
            Map<String, Float> row = new HashMap<>();
            int rows = 0;
            for (List<String> column : columns) {
                List<String> sorted = new ArrayList<>(column);
                sorted.sort(Comparator.comparingDouble(id -> desiredRow(id, parents, row)));
                float next = 0;
                for (String id : sorted) {
                    float r = Math.max(next, (float) desiredRow(id, parents, row));
                    row.put(id, r);
                    next = r + 1;
                }
                rows = Math.max(rows, (int) Math.ceil(next));
            }
            for (String id : group) {
                nodes.put(id, new Node(byId.get(id), depth.get(id) * COL_W, top + Math.round(row.get(id) * ROW_H)));
            }
            top += rows * ROW_H + GROUP_GAP;
        }

        if (!loners.isEmpty()) {
            if (!groups.isEmpty()) labels.add(new Label("Independent quests", 0, top - 4));
            int gridTop = top + (groups.isEmpty() ? 0 : 10);
            for (int i = 0; i < loners.size(); i++) {
                String id = loners.get(i);
                nodes.put(id, new Node(byId.get(id), (i % GRID_COLUMNS) * COL_W, gridTop + (i / GRID_COLUMNS) * ROW_H));
            }
        }

        for (var entry : parents.entrySet()) {
            Node to = nodes.get(entry.getKey());
            for (String parent : entry.getValue()) edges.add(new Edge(nodes.get(parent), to));
        }

        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
        for (Node node : nodes.values()) {
            minX = Math.min(minX, node.x());
            minY = Math.min(minY, node.y());
            maxX = Math.max(maxX, node.x() + NODE_W);
            maxY = Math.max(maxY, node.y() + NODE_H);
        }
        if (nodes.isEmpty()) minX = minY = maxX = maxY = 0;
    }

    private static int depthOf(String id, Map<String, List<String>> parents, Map<String, Integer> memo, Set<String> visiting) {
        Integer known = memo.get(id);
        if (known != null) return known;
        if (!visiting.add(id)) return 0; // cycle guard
        int d = 0;
        for (String parent : parents.getOrDefault(id, List.of())) d = Math.max(d, depthOf(parent, parents, memo, visiting) + 1);
        memo.put(id, d);
        return d;
    }

    private static double desiredRow(String id, Map<String, List<String>> parents, Map<String, Float> row) {
        double sum = 0;
        int n = 0;
        for (String parent : parents.getOrDefault(id, List.of())) {
            Float r = row.get(parent);
            if (r != null) { sum += r; n++; }
        }
        return n == 0 ? 0 : sum / n;
    }

    // ── Camera ───────────────────────────────────────────────────────────────

    /** Width of the canvas not covered by the info panel. */
    private int viewW() {
        return isCardOpen() ? w - cardW() - 8 : w;
    }

    /** Moves the camera so the quest's card is in the middle of the visible canvas. */
    void focus(QuestDefinition quest, boolean animate) {
        ensureLayout();
        Node node = quest != null ? nodes.get(quest.id()) : null;
        float cx, cy;
        if (node != null) {
            cx = node.x() + NODE_W / 2f;
            cy = node.y() + NODE_H / 2f;
        } else {
            cx = (minX + maxX) / 2f;
            cy = (minY + maxY) / 2f;
        }
        targetCamX = cx - viewW() / 2f / zoom;
        targetCamY = cy - h / 2f / zoom;
        if (animate) {
            animating = true;
        } else {
            camX = targetCamX;
            camY = targetCamY;
            animating = false;
        }
        clampCamera();
    }

    private void clampCamera() {
        float visW = viewW() / zoom, visH = h / zoom;
        camX = Math.max(minX - visW + 60, Math.min(maxX - 60, camX));
        camY = Math.max(minY - visH + 40, Math.min(maxY - 40, camY));
    }

    private float worldX(double screenX) {
        return camX + (float) (screenX - x) / zoom;
    }

    private float worldY(double screenY) {
        return camY + (float) (screenY - y) / zoom;
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    void render(GuiGraphicsExtractor g, int mouseX, int mouseY, Predicate<QuestDefinition> matches) {
        ensureLayout();
        if (animating) {
            camX += (targetCamX - camX) * 0.25f;
            camY += (targetCamY - camY) * 0.25f;
            if (Math.abs(targetCamX - camX) < 0.5f && Math.abs(targetCamY - camY) < 0.5f) animating = false;
        }

        g.fill(x, y, x + w, y + h, 0xFF0C0C10);
        boolean overCard = isCardOpen() && inCard(mouseX, mouseY);
        boolean overCanvas = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h && !overCard;
        hovered = overCanvas && !dragging ? nodeAt(worldX(mouseX), worldY(mouseY)) : null;

        g.enableScissor(x, y, x + w, y + h);
        drawDots(g);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(zoom, zoom);
        g.pose().translate(-camX, -camY);

        QuestDefinition selected = screen.selected();
        for (Edge edge : edges) drawEdge(g, edge, selected);
        for (Label label : labels) g.text(font, label.text(), label.x(), label.y() - 8, GRAY, false);
        for (Node node : nodes.values()) drawNode(g, node, matches.test(node.quest()), selected);

        g.pose().popMatrix();
        g.disableScissor();

        if (nodes.isEmpty()) g.centeredText(font, "No quests in this age", x + w / 2, y + h / 2 - 4, DARK_GRAY);
        drawOverlay(g);

        if (hovered != null) {
            g.setComponentTooltipForNextFrame(font, tooltip(hovered.quest()), mouseX, mouseY);
        }
        links.clear();
        if (isCardOpen()) drawCard(g, mouseX, mouseY);
    }

    /** Faint dot grid that moves with the canvas, so panning feels physical. */
    private void drawDots(GuiGraphicsExtractor g) {
        float spacing = 20 * zoom;
        if (spacing < 6) return;
        float offX = ((-camX * zoom) % spacing + spacing) % spacing;
        float offY = ((-camY * zoom) % spacing + spacing) % spacing;
        for (float dx = offX; dx < w; dx += spacing) {
            for (float dy = offY; dy < h; dy += spacing) {
                int px = x + (int) dx, py = y + (int) dy;
                g.fill(px, py, px + 1, py + 1, 0xFF22222A);
            }
        }
    }

    private void drawEdge(GuiGraphicsExtractor g, Edge edge, QuestDefinition selected) {
        Node a = edge.from(), b = edge.to();
        boolean done = ClientQuestState.isComplete(a.quest());
        boolean lit = selected != null && (a.quest() == selected || b.quest() == selected)
                || hovered != null && (a == hovered || b == hovered);
        int color = lit ? accent() : done ? DONE_EDGE : 0xFF4A4A56;

        int sx = a.x() + NODE_W, sy = a.y() + NODE_H / 2;
        int ex = b.x() - 4, ey = b.y() + NODE_H / 2;
        int mid = sx + (COL_W - NODE_W) / 2;
        if (ex < mid) mid = (sx + ex) / 2;
        // Elbow: right, then up/down, then right into the child.
        g.fill(sx, sy - 1, mid + 1, sy + 1, color);
        g.fill(mid - 1, Math.min(sy, ey) - 1, mid + 1, Math.max(sy, ey) + 1, color);
        g.fill(mid - 1, ey - 1, ex, ey + 1, color);
        // Arrow head
        for (int i = 0; i < 4; i++) g.fill(ex + i, ey - 3 + i, ex + i + 1, ey + 4 - i, color);
    }

    private void drawNode(GuiGraphicsExtractor g, Node node, boolean matches, QuestDefinition selected) {
        QuestDefinition quest = node.quest();
        int nx = node.x(), ny = node.y();
        boolean complete = ClientQuestState.isComplete(quest);
        boolean futureAge = quest.age() > ClientQuestState.age;
        boolean available = !complete && !futureAge && ClientQuestState.isAvailable(quest);
        boolean isSelected = quest == selected;
        boolean isHovered = node == hovered;

        int border;
        if (complete) border = DONE_EDGE;
        else if (available) {
            // Gentle pulse on quests you can work on right now.
            float pulse = (float) (0.5 + 0.5 * Math.sin(Util.getMillis() / 300.0));
            border = QuestScreen.mix(accent(), 0xFF000000, 0.15f + 0.3f * pulse);
        } else border = 0xFF3A3A44;
        if (isSelected || isHovered) border = 0xFFFFFFFF;

        int bg = isSelected ? 0xFF2C2C3A : isHovered ? 0xFF24242E : 0xFF18181F;
        g.fill(nx - 1, ny - 1, nx + NODE_W + 1, ny + NODE_H + 1, 0xFF050507);
        g.fill(nx, ny, nx + NODE_W, ny + NODE_H, bg);
        g.outline(nx, ny, NODE_W, NODE_H, border);
        g.fill(nx + 1, ny + 1, nx + 3, ny + NODE_H - 1, complete ? DONE_EDGE : available ? accent() : 0xFF3A3A44);

        g.fill(nx + 5, ny + 5, nx + 25, ny + 25, 0xFF101015);
        g.item(QuestHud.stackOf(quest), nx + 7, ny + 7);

        boolean pinned = quest.id().equals(ClientQuestState.tracked);
        String title = (pinned ? "★ " : "") + quest.title();
        int titleColor = complete ? 0xFFA6E8A6 : available ? 0xFFFFFFFF : 0xFF7A7A84;
        g.text(font, font.plainSubstrByWidth(title, NODE_W - 32), nx + 29, ny + 5, titleColor, false);

        String status;
        int statusColor;
        if (complete) { status = "✔ Done"; statusColor = GREEN; }
        else if (futureAge) { status = "Future age"; statusColor = DARK_GRAY; }
        else if (!available) { status = "✖ Locked"; statusColor = DARK_GRAY; }
        else { status = ClientQuestState.progressText(quest) + (quest.tasks().size() > 1 ? " tasks" : ""); statusColor = GRAY; }
        if (quest.optional() && !complete) status += "  · opt";
        g.text(font, font.plainSubstrByWidth(status, NODE_W - 32), nx + 29, ny + 16, statusColor, false);

        if (available) QuestHud.bar(g, nx + 29, ny + NODE_H - 4, NODE_W - 34, 1, ClientQuestState.fraction(quest), GREEN);

        // Dim cards hidden by the filter or search, so the tree keeps its shape.
        if (!matches) g.fill(nx, ny, nx + NODE_W, ny + NODE_H, 0xB00C0C10);
    }

    /** Zoom level, legend and controls hint along the bottom of the canvas. */
    private void drawOverlay(GuiGraphicsExtractor g) {
        int by = y + h - 12;
        int lx = x + 5;
        lx = legend(g, lx, by, DONE_EDGE, "Done");
        lx = legend(g, lx, by, accent(), "Available");
        lx = legend(g, lx, by, 0xFF3A3A44, "Locked");
        String zoomText = Math.round(zoom * 100) + "%";
        String hint = "Drag to move · Scroll to zoom · " + zoomText;
        // Fall back to just the zoom level when the hint would run into the legend.
        if (x + viewW() - 5 - font.width(hint) < lx + 4) hint = zoomText;
        int hx = x + viewW() - 5 - font.width(hint);
        if (hx < lx + 4) return;
        g.fill(hx - 3, by - 2, hx + font.width(hint) + 2, by + 9, 0xA00C0C10);
        g.text(font, hint, hx, by, DARK_GRAY, false);
    }

    private int legend(GuiGraphicsExtractor g, int lx, int ly, int color, String text) {
        // Skip entries that would run under the info panel.
        if (lx + 12 + font.width(text) > x + viewW() - 4) return lx;
        g.fill(lx - 2, ly - 2, lx + 11 + font.width(text) + 2, ly + 9, 0xA00C0C10);
        g.fill(lx, ly + 1, lx + 7, ly + 7, color);
        g.text(font, text, lx + 10, ly, GRAY, false);
        return lx + 18 + font.width(text);
    }

    private List<Component> tooltip(QuestDefinition quest) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(quest.title()).withColor(GOLD));
        if (!quest.description().isEmpty()) {
            for (FormattedCharSequence seq : font.split(Component.literal(quest.description()), 180)) {
                StringBuilder sb = new StringBuilder();
                seq.accept((index, style, cp) -> { sb.appendCodePoint(cp); return true; });
                lines.add(Component.literal(sb.toString()).withColor(LIGHT));
            }
        }
        int task = ClientQuestState.currentTask(quest);
        if (ClientQuestState.isComplete(quest)) lines.add(Component.literal("✔ Completed").withColor(GREEN));
        else if (task >= 0) lines.add(Component.literal("▸ " + QuestText.describe(quest.tasks().get(task)).getString()).withColor(GRAY));
        lines.add(Component.literal("Click for details").withColor(DARK_GRAY));
        return lines;
    }

    // ── Info panel ───────────────────────────────────────────────────────────

    private boolean inCard(double mx, double my) {
        int[] c = cardBounds();
        return mx >= c[0] && mx < c[0] + c[2] && my >= c[1] && my < c[1] + c[3];
    }

    private void drawCard(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        QuestDefinition quest = screen.selected();
        int[] c = cardBounds();
        int cx = c[0], cy = c[1], cw = c[2], ch = c[3];
        boolean complete = ClientQuestState.isComplete(quest);
        boolean futureAge = quest.age() > ClientQuestState.age;

        g.fill(cx - 1, cy - 1, cx + cw + 1, cy + ch + 1, 0xFF050507);
        g.fill(cx, cy, cx + cw, cy + ch, 0xF01A1A22);
        g.outline(cx, cy, cw, ch, QuestScreen.mix(accent(), 0xFF000000, 0.35f));
        g.fill(cx, cy, cx + cw, cy + 2, accent());

        // Close button
        boolean closeHover = mouseX >= cx + cw - 13 && mouseX < cx + cw - 2 && mouseY >= cy + 3 && mouseY < cy + 14;
        g.text(font, "✖", cx + cw - 11, cy + 5, closeHover ? 0xFFFFFFFF : GRAY, false);

        int bottom = cy + ch - 26; // room for the Track button
        g.enableScissor(cx, cy, cx + cw, bottom);
        g.fill(cx + 5, cy + 6, cx + 25, cy + 26, 0xFF101015);
        g.item(QuestHud.stackOf(quest), cx + 7, cy + 8);
        g.text(font, font.plainSubstrByWidth(quest.title(), cw - 44), cx + 29, cy + 7, complete ? GREEN : GOLD, true);
        String state = complete ? "Completed" : futureAge ? "Reach the " + Age.nameOf(quest.age())
                : !ClientQuestState.isAvailable(quest) ? "Locked" : quest.optional() ? "Optional" : "Required";
        g.text(font, state, cx + 29, cy + 17, GRAY, false);

        int ty = cy + 31;
        if (!quest.description().isEmpty()) {
            for (FormattedCharSequence line : font.split(Component.literal(quest.description()), cw - 12)) {
                g.text(font, line, cx + 6, ty, LIGHT, false);
                ty += 10;
            }
            ty += 3;
        }

        ty = heading(g, cx, ty, cw, "Tasks");
        int[] progress = ClientQuestState.taskProgress(quest);
        for (int i = 0; i < quest.tasks().size(); i++) {
            QuestDefinition.Task task = quest.tasks().get(i);
            int value = complete ? task.count() : progress[i];
            boolean done = value >= task.count();
            String amount = value + "/" + task.count();
            String label = (done ? "✔ " : "• ") + QuestText.describe(task).getString();
            g.text(font, font.plainSubstrByWidth(label, cw - 18 - font.width(amount)), cx + 6, ty, done ? GREEN : 0xFFFFFFFF, false);
            g.text(font, amount, cx + cw - 6 - font.width(amount), ty, done ? GREEN : GRAY, false);
            if (task.count() > 1) QuestHud.bar(g, cx + 14, ty + 9, cw - 20, 1, (double) value / task.count(), done ? DONE_EDGE : GREEN);
            ty += task.count() > 1 ? 13 : 11;
        }

        List<QuestDefinition> requires = new ArrayList<>();
        for (String id : quest.requires()) {
            QuestDefinition r = ClientQuestState.QUESTS.get(id);
            if (r != null) requires.add(r);
        }
        if (!requires.isEmpty()) {
            ty = heading(g, cx, ty + 3, cw, "Requires");
            for (QuestDefinition r : requires) ty = link(g, cx, ty, cw, r, mouseX, mouseY, "◂ ");
        }

        List<QuestDefinition> unlocks = new ArrayList<>();
        for (QuestDefinition other : ClientQuestState.QUESTS.all()) {
            if (other.requires().contains(quest.id())) unlocks.add(other);
        }
        if (!unlocks.isEmpty()) {
            ty = heading(g, cx, ty + 3, cw, "Unlocks");
            for (QuestDefinition u : unlocks) ty = link(g, cx, ty, cw, u, mouseX, mouseY, "▸ ");
        }

        if (!quest.rewardItems().isEmpty() || quest.rewardXp() > 0) {
            ty = heading(g, cx, ty + 3, cw, "Rewards");
            int rx = cx + 6;
            for (QuestDefinition.RewardItem reward : quest.rewardItems()) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(reward.item()), reward.count());
                g.fill(rx - 1, ty - 1, rx + 17, ty + 17, 0xFF101015);
                g.item(stack, rx, ty);
                g.itemDecorations(font, stack, rx, ty);
                if (mouseX >= rx && mouseX < rx + 16 && mouseY >= ty && mouseY < ty + 16 && mouseY < bottom) {
                    g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                }
                rx += 20;
            }
            if (quest.rewardXp() > 0) g.text(font, "+" + quest.rewardXp() + " XP", rx + 2, ty + 4, 0xFF80FF20, true);
        }
        g.disableScissor();
    }

    private int heading(GuiGraphicsExtractor g, int cx, int ty, int cw, String text) {
        g.text(font, text, cx + 6, ty, GOLD, false);
        g.fill(cx + 9 + font.width(text), ty + 4, cx + cw - 6, ty + 5, 0xFF33333D);
        return ty + 11;
    }

    private int link(GuiGraphicsExtractor g, int cx, int ty, int cw, QuestDefinition target, int mouseX, int mouseY, String arrow) {
        boolean done = ClientQuestState.isComplete(target);
        String text = arrow + target.title() + (target.age() != age ? " (" + Age.nameOf(target.age()) + ")" : "");
        int lw = Math.min(cw - 12, font.width(text));
        boolean hover = mouseX >= cx + 6 && mouseX < cx + 6 + lw && mouseY >= ty - 1 && mouseY < ty + 9;
        int color = hover ? 0xFFFFFFFF : done ? 0xFF8FD88F : 0xFFE0A060;
        g.text(font, font.plainSubstrByWidth(text, cw - 12), cx + 6, ty, color, false);
        if (hover) g.fill(cx + 6, ty + 9, cx + 6 + lw, ty + 10, color);
        links.add(new Link(cx + 6, ty - 1, lw, 10, target));
        return ty + 11;
    }

    private int accent() {
        Age a = Age.byIndex(age);
        return a != null ? 0xFF000000 | a.color : GOLD;
    }

    // ── Input ────────────────────────────────────────────────────────────────

    private Node nodeAt(float wx, float wy) {
        for (Node node : nodes.values()) {
            if (wx >= node.x() && wx < node.x() + NODE_W && wy >= node.y() && wy < node.y() + NODE_H) return node;
        }
        return null;
    }

    boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    boolean mouseClicked(double mx, double my, boolean doubleClick) {
        if (!contains(mx, my)) return false;
        if (isCardOpen() && inCard(mx, my)) {
            int[] c = cardBounds();
            if (mx >= c[0] + c[2] - 13 && my < c[1] + 14) {
                cardOpen = false;
                screen.playClick();
                return true;
            }
            for (Link link : links) {
                if (mx >= link.x() && mx < link.x() + link.w() && my >= link.y() && my < link.y() + link.h()) {
                    screen.selectQuest(link.quest());
                    screen.playClick();
                    return true;
                }
            }
            return true;
        }
        Node node = nodeAt(worldX(mx), worldY(my));
        if (node != null) {
            screen.selectQuest(node.quest());
            cardOpen = true;
            screen.playClick();
            if (doubleClick) screen.toggleTrackFromTree();
            return true;
        }
        pressed = true;
        dragging = false;
        pressX = mx;
        pressY = my;
        pressCamX = camX;
        pressCamY = camY;
        return true;
    }

    boolean mouseDragged(double mx, double my) {
        if (!pressed) return false;
        if (!dragging && Math.hypot(mx - pressX, my - pressY) > 3) dragging = true;
        if (dragging) {
            animating = false;
            camX = pressCamX - (float) (mx - pressX) / zoom;
            camY = pressCamY - (float) (my - pressY) / zoom;
            clampCamera();
        }
        return true;
    }

    boolean mouseReleased() {
        boolean was = pressed;
        pressed = false;
        dragging = false;
        return was;
    }

    boolean mouseScrolled(double mx, double my, double amount) {
        if (!contains(mx, my)) return false;
        if (isCardOpen() && inCard(mx, my)) return true;
        float before = zoom;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * (amount > 0 ? 1.12f : 1 / 1.12f)));
        if (zoom != before) {
            // Keep the point under the cursor in place while zooming.
            float wx = camX + (float) (mx - x) / before;
            float wy = camY + (float) (my - y) / before;
            camX = wx - (float) (mx - x) / zoom;
            camY = wy - (float) (my - y) / zoom;
            animating = false;
            clampCamera();
        }
        return true;
    }

    /** Opens the info panel for a quest selected from elsewhere (keyboard, links) and glides to it. */
    void reveal(QuestDefinition quest) {
        if (quest == null) return;
        if (quest.age() != age) return;
        cardOpen = true;
        focus(quest, true);
    }
}
