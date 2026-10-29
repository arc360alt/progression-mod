package com.progressionmod;

import com.progressionmod.client.ClientQuestState;
import com.progressionmod.client.EndiumMarkerRenderer;
import com.progressionmod.client.ProgressionModKeys;
import com.progressionmod.client.QuestHud;
import com.progressionmod.client.QuestScreen;
import com.progressionmod.entities.ModEntities;
import com.progressionmod.items.QuestBookItem;
import com.progressionmod.quest.Age;
import com.progressionmod.quest.AgeLocks;
import com.progressionmod.quest.QuestDefinition;
import com.progressionmod.quest.QuestNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class ProgressionModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ENDIUM_MARKER, EndiumMarkerRenderer::new);

        ProgressionModKeys.register();
        AgeLocks.clientAge = player -> ClientQuestState.age;
        QuestBookItem.openScreen = ProgressionModClient::openQuestBook;

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "quests"), QuestHud::extract);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ProgressionModKeys.OPEN_QUESTS.consumeClick()) {
                if (client.gui.screen() == null) openQuestBook();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(QuestNetworking.DefinitionsPayload.TYPE,
                (payload, context) -> ClientQuestState.QUESTS.loadFromJson(payload.json()));
        ClientPlayNetworking.registerGlobalReceiver(QuestNetworking.ProgressPayload.TYPE,
                (payload, context) -> ClientQuestState.apply(payload));
        ClientPlayNetworking.registerGlobalReceiver(QuestNetworking.OpenBookPayload.TYPE,
                (payload, context) -> openQuestBook());
        ClientPlayNetworking.registerGlobalReceiver(QuestNetworking.EventPayload.TYPE, (payload, context) -> {
            Minecraft mc = context.client();
            if (payload.ageUp()) {
                ClientQuestState.age = payload.age();
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f));
            } else {
                QuestDefinition quest = ClientQuestState.QUESTS.get(payload.questId());
                if (quest != null) QuestHud.showQuestComplete(quest);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.4f, 0.6f));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientQuestState.reset());

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            int required = AgeLocks.requiredAge(stack);
            if (required < 0) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && AgeLocks.isLocked(mc.player, stack)) {
                lines.add(Component.literal("✖ Locked until the " + Age.nameOf(required)).withStyle(ChatFormatting.RED));
            } else {
                lines.add(Component.literal(Age.nameOf(required)).withColor(Age.values()[required].color));
            }
        });
    }

    private static void openQuestBook() {
        Minecraft.getInstance().setScreenAndShow(new QuestScreen());
    }
}
