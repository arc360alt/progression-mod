package com.progressionmod.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.progressionmod.ProgressionMod;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.*;

/**
 * Holds every loaded quest. On the server this is filled from datapacks; on the
 * client a separate instance is filled from the definitions the server sends.
 *
 * Quest files live at {@code data/<namespace>/progression/quests/<name>.json}:
 * <pre>{ "age": "iron", "quests": [ { "id": "...", ... }, ... ] }</pre>
 * A quest whose id is already loaded replaces the earlier one, so datapacks can
 * override built-in quests by reusing their ids.
 */
public class QuestRegistry {

    public static final String DIRECTORY = "progression/quests";

    /** Server-side quests, loaded from datapacks. */
    public static final QuestRegistry SERVER = new QuestRegistry();

    private final Map<String, QuestDefinition> byId = new LinkedHashMap<>();
    private final List<List<QuestDefinition>> byAge = new ArrayList<>();

    public QuestRegistry() {
        for (int i = 0; i < Age.COUNT; i++) byAge.add(new ArrayList<>());
    }

    public QuestDefinition get(String id) {
        return byId.get(id);
    }

    public Collection<QuestDefinition> all() {
        return byId.values();
    }

    public List<QuestDefinition> forAge(int age) {
        return age >= 0 && age < Age.COUNT ? byAge.get(age) : List.of();
    }

    public int size() {
        return byId.size();
    }

    public void replaceAll(Collection<QuestDefinition> quests) {
        byId.clear();
        byAge.forEach(List::clear);
        for (QuestDefinition quest : quests) byId.put(quest.id(), quest);
        for (QuestDefinition quest : byId.values()) byAge.get(quest.age()).add(quest);
    }

    // ── Client sync ──────────────────────────────────────────────────────────

    public String toJson() {
        JsonArray array = new JsonArray();
        for (QuestDefinition quest : byId.values()) array.add(quest.json());
        return array.toString();
    }

    public void loadFromJson(String json) {
        List<QuestDefinition> quests = new ArrayList<>();
        for (JsonElement el : JsonParser.parseString(json).getAsJsonArray()) {
            try {
                quests.add(QuestDefinition.parse(el.getAsJsonObject(), Age.FLINT));
            } catch (RuntimeException e) {
                ProgressionMod.LOGGER.error("Failed to read synced quest: {}", e.getMessage());
            }
        }
        replaceAll(quests);
    }

    // ── Datapack loading ─────────────────────────────────────────────────────

    public static void register() {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
                Identifier.fromNamespaceAndPath(ProgressionMod.MOD_ID, "quests"), new Loader());
    }

    private static class Loader extends SimplePreparableReloadListener<List<QuestDefinition>> {

        @Override
        protected List<QuestDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
            List<QuestDefinition> quests = new ArrayList<>();
            // Built-in files first so other namespaces can override quests by id; then by file name.
            TreeMap<Identifier, Resource> files = new TreeMap<>(
                    Comparator.comparing((Identifier id) -> !id.getNamespace().equals(ProgressionMod.MOD_ID))
                            .thenComparing(Identifier::getPath)
                            .thenComparing(Identifier::getNamespace));
            files.putAll(manager.listResources(DIRECTORY, id -> id.getPath().endsWith(".json")));

            for (var entry : files.entrySet()) {
                Identifier file = entry.getKey();
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    Age age = root.has("age") ? Age.byId(root.get("age").getAsString()) : null;
                    if (age == null) {
                        ProgressionMod.LOGGER.error("Quest file {} is missing a valid \"age\"", file);
                        continue;
                    }
                    for (JsonElement el : root.getAsJsonArray("quests")) {
                        try {
                            quests.add(QuestDefinition.parse(el.getAsJsonObject(), age));
                        } catch (RuntimeException e) {
                            ProgressionMod.LOGGER.error("Skipping invalid quest in {}: {}", file, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    ProgressionMod.LOGGER.error("Failed to load quest file {}", file, e);
                }
            }
            return quests;
        }

        @Override
        protected void apply(List<QuestDefinition> quests, ResourceManager manager, ProfilerFiller profiler) {
            SERVER.replaceAll(quests);
            for (QuestDefinition quest : SERVER.all()) {
                for (String req : quest.requires()) {
                    if (SERVER.get(req) == null)
                        ProgressionMod.LOGGER.warn("Quest '{}' requires unknown quest '{}'", quest.id(), req);
                }
            }
            ProgressionMod.LOGGER.info("Loaded {} progression quests", SERVER.size());
        }
    }
}
