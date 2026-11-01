package com.progressionmod.client.compat;

import com.progressionmod.client.QuestHud;
import net.alan.gui.api.NeoCraftHudApi;
import net.alan.gui.api.NeoCraftHudEntrypoint;
import net.alan.gui.api.WrappedHudElement;
import net.alan.gui.hud.screen.HudEditorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Puts the quest HUD and the quest-complete banner into NeoCraft UI's HUD editor, so they
 * can be moved, scaled and switched off from the NeoCraft Client menu.
 *
 * <p>This is the only class that touches NeoCraft. Fabric only loads it through the
 * {@code neocraft_ui:hud} entrypoint, which NeoCraft (with its HUD API) invokes. Without
 * NeoCraft, or with a version that has no HUD API, it never loads and the HUD draws on its own.
 * {@link QuestHud} only calls in here after {@link #registerHudElements()} has run.
 */
@Environment(EnvType.CLIENT)
public final class NeoCraftHudCompat implements NeoCraftHudEntrypoint {

    private static WrappedHudElement panel;
    private static WrappedHudElement banner;

    @Override
    public void registerHudElements() {
        panel = NeoCraftHudApi.wrap("progressionmod:quest_hud", "progressionmod.hud.quests", QuestHud::panelBox);
        banner = NeoCraftHudApi.wrap("progressionmod:quest_banner", "progressionmod.hud.banner", QuestHud::bannerBox);
        QuestHud.neoCraftActive = true;
    }

    /** Applies the player's position and scale. Returns false if the element was switched off; always call {@link #popPanel} after. */
    public static boolean pushPanel(GuiGraphicsExtractor g) {
        return panel.push(g);
    }

    public static void popPanel(GuiGraphicsExtractor g) {
        panel.pop(g);
    }

    public static boolean pushBanner(GuiGraphicsExtractor g) {
        return banner.push(g);
    }

    public static void popBanner(GuiGraphicsExtractor g) {
        banner.pop(g);
    }

    /** True while NeoCraft's HUD layout editor is open, so sample content can be shown to place. */
    public static boolean editorOpen() {
        return Minecraft.getInstance().gui.screen() instanceof HudEditorScreen;
    }
}
