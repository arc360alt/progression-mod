package com.progressionmod.client;

import com.progressionmod.quest.Age;
import com.progressionmod.quest.QuestDefinition;
import com.progressionmod.quest.QuestManager;
import com.progressionmod.quest.QuestNetworking;
import com.progressionmod.quest.QuestRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;

/** The client's copy of quest definitions and this player's progress, as last sent by the server. */
@Environment(EnvType.CLIENT)
public final class ClientQuestState {

    public static final QuestRegistry QUESTS = new QuestRegistry();

    /** Until the server says otherwise nothing is locked, so a server without the mod behaves normally. */
    public static int age = Age.COUNT;
    public static boolean synced = false;
    public static Set<String> completed = Set.of();
    public static Map<String, int[]> progress = Map.of();
    public static String tracked = "";

    private ClientQuestState() {}

    public static void apply(QuestNetworking.ProgressPayload payload) {
        age = payload.age();
        completed = payload.completed();
        progress = payload.progress();
        tracked = payload.tracked();
        synced = true;
    }

    public static void reset() {
        QUESTS.replaceAll(List.of());
        age = Age.COUNT;
        synced = false;
        completed = Set.of();
        progress = Map.of();
        tracked = "";
    }

    public static boolean isComplete(QuestDefinition quest) {
        return completed.contains(quest.id());
    }

    public static boolean isAvailable(QuestDefinition quest) {
        return QuestManager.isVisible(quest, age) && QuestManager.requirementsMet(quest, completed, QUESTS);
    }

    /** Capped progress of each task, or zeros if the server hasn't sent any. */
    public static int[] taskProgress(QuestDefinition quest) {
        int[] values = progress.get(quest.id());
        if (values != null && values.length == quest.tasks().size()) return values;
        int[] out = new int[quest.tasks().size()];
        if (isComplete(quest)) for (int i = 0; i < out.length; i++) out[i] = quest.tasks().get(i).count();
        return out;
    }

    /** Index of the first task that isn't finished yet, or -1 if every task is done. */
    public static int currentTask(QuestDefinition quest) {
        int[] values = taskProgress(quest);
        for (int i = 0; i < values.length; i++) {
            if (values[i] < quest.tasks().get(i).count()) return i;
        }
        return -1;
    }

    public static int completedInAge(int ageIndex) {
        int n = 0;
        for (QuestDefinition quest : QUESTS.forAge(ageIndex)) if (isComplete(quest)) n++;
        return n;
    }

    /** Quests to show on the HUD: the tracked one first, then the next available ones in the current age. */
    public static List<QuestDefinition> hudQuests(int max) {
        List<QuestDefinition> out = new ArrayList<>();
        QuestDefinition pinned = QUESTS.get(tracked);
        if (pinned != null && !isComplete(pinned)) out.add(pinned);

        // Nearly-finished quests first; the sort is stable so ties keep file order.
        List<QuestDefinition> candidates = new ArrayList<>();
        for (QuestDefinition quest : QUESTS.forAge(age)) {
            if (quest != pinned && !isComplete(quest) && isAvailable(quest)) candidates.add(quest);
        }
        candidates.sort(Comparator.comparingDouble(q -> -fraction(q)));
        for (QuestDefinition quest : candidates) {
            if (out.size() >= max) break;
            out.add(quest);
        }
        return out;
    }

    /**
     * Overall progress from 0 to 1: the average of each task's own progress, so a big
     * count in one task (like "collect 20") can't make a two-task quest look nearly done.
     */
    public static double fraction(QuestDefinition quest) {
        int[] values = taskProgress(quest);
        if (values.length == 0) return 0;
        double sum = 0;
        for (int i = 0; i < values.length; i++) sum += Math.min(1.0, values[i] / (double) quest.tasks().get(i).count());
        return sum / values.length;
    }

    /** "3/10" for a single-task quest, or completed tasks out of total ("1/2") when there are several. */
    public static String progressText(QuestDefinition quest) {
        if (quest.tasks().size() == 1) return taskProgress(quest)[0] + "/" + quest.tasks().getFirst().count();
        int[] values = taskProgress(quest);
        int done = 0;
        for (int i = 0; i < values.length; i++) if (values[i] >= quest.tasks().get(i).count()) done++;
        return done + "/" + values.length;
    }
}
