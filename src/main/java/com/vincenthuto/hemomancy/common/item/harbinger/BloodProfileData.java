package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import java.util.*;

/** Server-owned definitions, mirrored to clients for sample presentation and interaction prediction. */
public final class BloodProfileData extends SimplePreparableReloadListener<BloodProfileData.Snapshot> {
    public static final String DIRECTORY = "blood_profiles";
    public static final int MAX_DEFINITIONS = 4096, MAX_JSON_LENGTH = 4096, MAX_SYNC_BYTES = 900_000;
    private static volatile Snapshot server = new Snapshot(Map.of());
    private static volatile Snapshot client = new Snapshot(Map.of());

    public static final class Snapshot {
        private final Map<ResourceLocation, String> json;
        private final Map<ResourceLocation, EntityBloodProfile> profiles;

        public Snapshot(Map<ResourceLocation, String> definitions) {
            if (definitions.size() > MAX_DEFINITIONS) throw new IllegalArgumentException("Too many blood profiles");
            var parsed = new HashMap<ResourceLocation, EntityBloodProfile>();
            int bytes = 0;
            for (var entry : definitions.entrySet()) {
                bytes += syncBytes(entry.getKey(), entry.getValue());
                if (entry.getValue().length() > MAX_JSON_LENGTH || bytes > MAX_SYNC_BYTES)
                    throw new IllegalArgumentException("Blood profile snapshot exceeds sync limits");
                parsed.put(entry.getKey(), EntityBloodProfile.parse(JsonParser.parseString(entry.getValue()).getAsJsonObject()));
            }
            json = Map.copyOf(definitions);
            profiles = Map.copyOf(parsed);
        }
        public Map<ResourceLocation, String> json() { return json; }
        public EntityBloodProfile get(ResourceLocation id) { return profiles.getOrDefault(id, EntityBloodProfile.EMPTY); }
    }

    private static int syncBytes(ResourceLocation id, String json) {
        return id.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                + json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + 10;
    }
    public static Snapshot snapshot(boolean clientSide) { return clientSide ? client : server; }
    public static EntityBloodProfile profile(EntityType<?> type, boolean clientSide) {
        return type == null ? EntityBloodProfile.EMPTY : snapshot(clientSide).get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }
    public static void receive(Map<ResourceLocation, String> definitions) { client = new Snapshot(definitions); }
    public static void clearClient() { client = new Snapshot(Map.of()); }

    @Override protected Snapshot prepare(ResourceManager manager, ProfilerFiller profiler) {
        var valid = new LinkedHashMap<ResourceLocation, String>();
        var resources = new TreeMap<ResourceLocation, Resource>(Comparator.comparing(ResourceLocation::toString));
        resources.putAll(manager.listResources(DIRECTORY, id -> id.getPath().endsWith(".json")));
        int bytes = 0;
        for (var entry : resources.entrySet()) {
            var resourceId = entry.getKey();
            try (var reader = entry.getValue().openAsReader()) {
                var object = JsonParser.parseReader(reader).getAsJsonObject();
                EntityBloodProfile.parse(object);
                var id = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(),
                        resourceId.getPath().substring(DIRECTORY.length() + 1, resourceId.getPath().length() - 5));
                String json = object.toString();
                int size = syncBytes(id, json);
                if (json.length() > MAX_JSON_LENGTH || valid.size() >= MAX_DEFINITIONS || bytes + size > MAX_SYNC_BYTES)
                    throw new IllegalArgumentException("Blood profile snapshot exceeds sync limits");
                valid.put(id, json);
                bytes += size;
            } catch (Exception exception) {
                Hemomancy.LOGGER.error("Invalid blood profile {}: {}", resourceId, exception.getMessage());
            }
        }
        return new Snapshot(valid);
    }
    @Override protected void apply(Snapshot prepared, ResourceManager manager, ProfilerFiller profiler) {
        server = prepared;
        Hemomancy.LOGGER.info("Loaded {} entity blood profiles", prepared.json().size());
    }
}
