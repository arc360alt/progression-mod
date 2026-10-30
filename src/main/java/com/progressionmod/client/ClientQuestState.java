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

    public static int questProgress(QuestDefinition quest) {
        int sum = 0;
        for (int v : taskProgress(quest)) sum += v;
        return sum;
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

    public static double fraction(QuestDefinition quest) {
        int total = quest.totalCount();
        return total == 0 ? 0 : (double) questProgress(quest) / total;
    }
}
