package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.*;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Independent server/client snapshots also keep integrated-server disconnects from clearing gameplay data. */
public final class BloodInjectionData extends SimplePreparableReloadListener<BloodInjectionData.Snapshot> {
    public static final int MAX_DEFINITIONS = 128, MAX_JSON_LENGTH = 2048;
    private static volatile Snapshot server = new Snapshot(Map.of());
    private static volatile Snapshot client = new Snapshot(Map.of());

    public record Snapshot(Map<ResourceLocation, String> json, List<BloodInjectionRules.Response> responses) {
        public Snapshot(Map<ResourceLocation, String> json) { this(Map.copyOf(json), decode(json)); }
        public Snapshot { json = Map.copyOf(json); responses = List.copyOf(responses); }
        public List<ResourceLocation> properties() {
            return responses.stream().map(BloodInjectionRules.Response::property).filter(Objects::nonNull).toList();
        }
        public BloodInjectionRules.Result resolve(BloodSampleData.Profile profile) {
            return BloodInjectionRules.select(responses.stream().filter(r -> r.tendency() != null
                ? profile.tendencies().contains(r.tendency()) : profile.properties().contains(r.property())).toList());
        }
    }
    public static Snapshot snapshot(boolean clientSide) { return clientSide ? client : server; }
    public static void receive(Map<ResourceLocation, String> json) { client = new Snapshot(json); }
    public static void clearClient() { client = new Snapshot(Map.of()); }

    private static BloodInjectionRules.Response parse(String json) {
        return BloodInjectionRules.parse(JsonParser.parseString(json).getAsJsonObject(), BuiltInRegistries.MOB_EFFECT::containsKey);
    }
    private static List<BloodInjectionRules.Response> decode(Map<ResourceLocation, String> json) {
        return json.values().stream().map(BloodInjectionData::parse)
            .sorted(Comparator.comparingInt((BloodInjectionRules.Response r) -> r.tendency() == null ? 8 : r.tendency().ordinal())
                .thenComparing(BloodInjectionRules.Response::origin)).toList();
    }
    @Override protected Snapshot prepare(ResourceManager manager, ProfilerFiller profiler) {
        var valid = new LinkedHashMap<ResourceLocation, String>();
        var origins = new HashSet<String>();
        var conflicts = new HashSet<String>();
        var resources = new TreeMap<ResourceLocation, Resource>(Comparator.comparing(ResourceLocation::toString));
        resources.putAll(manager.listResources("blood_injection", id -> id.getPath().endsWith(".json")));
        resources.forEach((id, resource) -> {
            try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                var object = JsonParser.parseReader(reader).getAsJsonObject();
                String json = object.toString();
                if (json.length() > MAX_JSON_LENGTH || id.toString().length() > 256 || valid.size() >= MAX_DEFINITIONS)
                    throw new IllegalArgumentException("Response snapshot limit exceeded");
                var response = parse(json);
                if (!origins.add(response.origin())) {
                    conflicts.add(response.origin());
                    throw new IllegalArgumentException("Conflicting origin " + response.origin() + "; all definitions for this origin discarded");
                }
                valid.put(id, json);
            } catch (Exception exception) {
                Hemomancy.LOGGER.error("Invalid blood injection response {}: {}", id, exception.getMessage());
            }
        });
        valid.entrySet().removeIf(entry -> conflicts.contains(parse(entry.getValue()).origin()));
        return new Snapshot(valid);
    }
    @Override protected void apply(Snapshot prepared, ResourceManager manager, ProfilerFiller profiler) {
        server = prepared;
        Hemomancy.LOGGER.info("Loaded {} blood injection responses", prepared.responses().size());
    }
}

