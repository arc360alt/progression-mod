package com.progressionmod.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A single quest, parsed from {@code data/<namespace>/progression/quests/*.json}.
 * See {@code QUESTS.md} in the repository root for the file format.
 *
 * The raw JSON is kept so the server can send definitions to clients verbatim and
 * both sides parse them with the same code.
 */
public record QuestDefinition(
        String id,
        int age,
        String title,
        String description,
        Identifier icon,
        boolean optional,
        List<String> requires,
        List<Task> tasks,
        List<RewardItem> rewardItems,
        int rewardXp,
        JsonObject json
) {

    /** One objective of a quest. {@code args} holds the type-specific fields. */
    public record Task(String type, int count, String label, JsonObject args) {

        /** Reads a string or array of strings ("target", "item", ...) as a list. */
        public List<String> strings(String key) {
            List<String> out = new ArrayList<>();
            JsonElement el = args.get(key);
            if (el == null) return out;
            if (el.isJsonArray()) for (JsonElement e : el.getAsJsonArray()) out.add(e.getAsString());
            else out.add(el.getAsString());
            return out;
        }

        public String string(String key, String fallback) {
            JsonElement el = args.get(key);
            return el != null && el.isJsonPrimitive() ? el.getAsString() : fallback;
        }

        public int integer(String key, int fallback) {
            JsonElement el = args.get(key);
            return el != null && el.isJsonPrimitive() ? el.getAsInt() : fallback;
        }

        public boolean has(String key) {
            return args.has(key);
        }
    }

    public record RewardItem(Identifier item, int count) {}

    public static QuestDefinition parse(JsonObject json, Age defaultAge) {
        String id = req(json, "id");
        Age age = json.has("age") ? Age.byId(json.get("age").getAsString()) : defaultAge;
        if (age == null) throw new JsonParseException("Quest '" + id + "' has an unknown age");

        String title = req(json, "title");
        String description = json.has("description") ? json.get("description").getAsString() : "";
        Identifier icon = Identifier.parse(json.has("icon") ? json.get("icon").getAsString() : "minecraft:book");
        boolean optional = json.has("optional") && json.get("optional").getAsBoolean();

        List<String> requires = new ArrayList<>();
        if (json.has("requires")) for (JsonElement e : json.getAsJsonArray("requires")) requires.add(e.getAsString());

        List<Task> tasks = new ArrayList<>();
        if (json.has("task")) tasks.add(parseTask(json.getAsJsonObject("task"), id));
        if (json.has("tasks")) for (JsonElement e : json.getAsJsonArray("tasks")) tasks.add(parseTask(e.getAsJsonObject(), id));
        if (tasks.isEmpty()) throw new JsonParseException("Quest '" + id + "' has no task");

        List<RewardItem> rewardItems = new ArrayList<>();
        int rewardXp = 0;
        if (json.has("rewards")) {
            JsonObject rewards = json.getAsJsonObject("rewards");
            if (rewards.has("xp")) rewardXp = rewards.get("xp").getAsInt();
            if (rewards.has("items")) {
                JsonArray items = rewards.getAsJsonArray("items");
                for (JsonElement e : items) {
                    JsonObject o = e.getAsJsonObject();
                    rewardItems.add(new RewardItem(Identifier.parse(req(o, "item")),
                            o.has("count") ? o.get("count").getAsInt() : 1));
                }
            }
        }

        JsonObject normalized = json.deepCopy();
        normalized.addProperty("age", age.id);
        return new QuestDefinition(id, age.ordinal(), title, description, icon, optional,
                List.copyOf(requires), List.copyOf(tasks), List.copyOf(rewardItems), rewardXp, normalized);
    }

    private static Task parseTask(JsonObject json, String questId) {
        if (!json.has("type")) throw new JsonParseException("A task in quest '" + questId + "' has no type");
        String type = json.get("type").getAsString();
        if (!TaskTypes.isKnown(type)) throw new JsonParseException("Quest '" + questId + "' uses unknown task type '" + type + "'");
        int count = json.has("count") ? Math.max(1, json.get("count").getAsInt()) : 1;
        String label = json.has("label") ? json.get("label").getAsString() : "";
        return new Task(type, count, label, json);
    }

    private static String req(JsonObject json, String key) {
        if (!json.has(key)) throw new JsonParseException("Missing required field '" + key + "'");
        return json.get(key).getAsString();
    }

    public int totalCount() {
        int total = 0;
        for (Task t : tasks) total += t.count();
        return total;
    }
}
