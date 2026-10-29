package com.progressionmod.quest;

import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class QuestNetworking {

    private QuestNetworking() {}

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String name) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, name));
    }

    /** S2C: every quest definition, as the JSON array produced by {@link QuestRegistry#toJson()}. */
    public record DefinitionsPayload(String json) implements CustomPacketPayload {
        public static final Type<DefinitionsPayload> TYPE = payloadType("quest_definitions");
        public static final StreamCodec<RegistryFriendlyByteBuf, DefinitionsPayload> CODEC = StreamCodec.ofMember(
                (p, buf) -> buf.writeUtf(p.json, Integer.MAX_VALUE / 4),
                buf -> new DefinitionsPayload(buf.readUtf(Integer.MAX_VALUE / 4)));
        @Override public Type<DefinitionsPayload> type() { return TYPE; }
    }

    /**
     * S2C: the player's age, completed quests, and per-task progress of every quest
     * that is visible but not yet complete.
     */
    public record ProgressPayload(int age, Set<String> completed, Map<String, int[]> progress, String tracked)
            implements CustomPacketPayload {
        public static final Type<ProgressPayload> TYPE = payloadType("quest_progress");
        public static final StreamCodec<RegistryFriendlyByteBuf, ProgressPayload> CODEC =
                StreamCodec.ofMember(ProgressPayload::write, ProgressPayload::read);

        private void write(FriendlyByteBuf buf) {
            buf.writeVarInt(age);
            buf.writeVarInt(completed.size());
            for (String id : completed) buf.writeUtf(id);
            buf.writeVarInt(progress.size());
            for (var entry : progress.entrySet()) {
                buf.writeUtf(entry.getKey());
                buf.writeVarInt(entry.getValue().length);
                for (int v : entry.getValue()) buf.writeVarInt(v);
            }
            buf.writeUtf(tracked);
        }

        private static ProgressPayload read(FriendlyByteBuf buf) {
            int age = buf.readVarInt();
            int n = buf.readVarInt();
            Set<String> completed = new HashSet<>();
            for (int i = 0; i < n; i++) completed.add(buf.readUtf());
            int m = buf.readVarInt();
            Map<String, int[]> progress = new HashMap<>();
            for (int i = 0; i < m; i++) {
                String id = buf.readUtf();
                int[] values = new int[buf.readVarInt()];
                for (int j = 0; j < values.length; j++) values[j] = buf.readVarInt();
                progress.put(id, values);
            }
            return new ProgressPayload(age, completed, progress, buf.readUtf());
        }

        @Override public Type<ProgressPayload> type() { return TYPE; }
    }

    /** S2C: a quest was completed, or (when {@code ageUp}) a new age was reached. */
    public record EventPayload(boolean ageUp, String questId, int age) implements CustomPacketPayload {
        public static final Type<EventPayload> TYPE = payloadType("quest_event");
        public static final StreamCodec<RegistryFriendlyByteBuf, EventPayload> CODEC = StreamCodec.ofMember(
                (p, buf) -> { buf.writeBoolean(p.ageUp); buf.writeUtf(p.questId); buf.writeVarInt(p.age); },
                buf -> new EventPayload(buf.readBoolean(), buf.readUtf(), buf.readVarInt()));
        @Override public Type<EventPayload> type() { return TYPE; }
    }

    /** S2C: open the quest book screen (sent by {@code /quests}). */
    public record OpenBookPayload() implements CustomPacketPayload {
        public static final Type<OpenBookPayload> TYPE = payloadType("open_quest_book");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenBookPayload> CODEC =
                StreamCodec.of((buf, p) -> {}, buf -> new OpenBookPayload());
        @Override public Type<OpenBookPayload> type() { return TYPE; }
    }

    /** C2S: pin a quest to the HUD. An empty id unpins. */
    public record TrackPayload(String questId) implements CustomPacketPayload {
        public static final Type<TrackPayload> TYPE = payloadType("track_quest");
        public static final StreamCodec<RegistryFriendlyByteBuf, TrackPayload> CODEC = StreamCodec.ofMember(
                (p, buf) -> buf.writeUtf(p.questId),
                buf -> new TrackPayload(buf.readUtf()));
        @Override public Type<TrackPayload> type() { return TYPE; }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().registerLarge(DefinitionsPayload.TYPE, DefinitionsPayload.CODEC, 16 * 1024 * 1024);
        PayloadTypeRegistry.clientboundPlay().register(ProgressPayload.TYPE, ProgressPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EventPayload.TYPE, EventPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenBookPayload.TYPE, OpenBookPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TrackPayload.TYPE, TrackPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TrackPayload.TYPE, (payload, context) ->
                QuestManager.setTracked(context.player(), payload.questId()));
    }
}
