package com.progressionmod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-player quest progress, stored as a persistent Fabric data attachment that
 * survives death. Stat-based quests read the vanilla stats directly; this only holds
 * what vanilla doesn't already track.
 */
public class QuestData {

    public int age;
    public final Set<String> completed;
    public final Set<String> visitedBiomes;
    public final Set<String> visitedDimensions;
    public int lowestY;
    public int highestY;
    public boolean bookGiven;
    public String tracked;

    public QuestData() {
        this(0, List.of(), List.of(), List.of(), Integer.MAX_VALUE, Integer.MIN_VALUE, false, "");
    }

    private QuestData(int age, List<String> completed, List<String> visitedBiomes, List<String> visitedDimensions,
                      int lowestY, int highestY, boolean bookGiven, String tracked) {
        this.age = age;
        this.completed = new HashSet<>(completed);
        this.visitedBiomes = new HashSet<>(visitedBiomes);
        this.visitedDimensions = new HashSet<>(visitedDimensions);
        this.lowestY = lowestY;
        this.highestY = highestY;
        this.bookGiven = bookGiven;
        this.tracked = tracked;
    }

    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("age", 0).forGetter(d -> d.age),
            Codec.STRING.listOf().optionalFieldOf("completed", List.of()).forGetter(d -> List.copyOf(d.completed)),
            Codec.STRING.listOf().optionalFieldOf("visited_biomes", List.of()).forGetter(d -> List.copyOf(d.visitedBiomes)),
            Codec.STRING.listOf().optionalFieldOf("visited_dimensions", List.of()).forGetter(d -> List.copyOf(d.visitedDimensions)),
            Codec.INT.optionalFieldOf("lowest_y", Integer.MAX_VALUE).forGetter(d -> d.lowestY),
            Codec.INT.optionalFieldOf("highest_y", Integer.MIN_VALUE).forGetter(d -> d.highestY),
            Codec.BOOL.optionalFieldOf("book_given", false).forGetter(d -> d.bookGiven),
            Codec.STRING.optionalFieldOf("tracked", "").forGetter(d -> d.tracked)
    ).apply(i, QuestData::new));

    public static final AttachmentType<QuestData> ATTACHMENT = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "quest_data"),
            builder -> builder.persistent(CODEC).copyOnDeath().initializer(QuestData::new));

    public static QuestData get(ServerPlayer player) {
        return player.getAttachedOrCreate(ATTACHMENT);
    }

    public static void register() {
        // Class load registers the attachment.
    }
}
