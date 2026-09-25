package com.vincenthuto.hemomancy.common.enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public final class ScriptoriumAffinities extends SimplePreparableReloadListener<Map<ResourceLocation, ScriptoriumAffinities.Affinity>> {
    private static final String DIRECTORY = "enchantment_affinity";
    private static volatile Map<ResourceLocation, Affinity> current = Map.of();

    public record Affinity(EnumBloodTendency primary, EnumBloodTendency secondary) {}

    public static Affinity get(ResourceLocation enchantment) { return current.get(enchantment); }

    @Override protected Map<ResourceLocation, Affinity> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Affinity> result = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(DIRECTORY,
                id -> id.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation file = entry.getKey();
            try (var reader = entry.getValue().openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                ResourceLocation enchantment = ResourceLocation.parse(json.get("enchantment").getAsString());
                String path = file.getPath().substring(DIRECTORY.length() + 1, file.getPath().length() - 5);
                if (!path.equals(enchantment.getNamespace() + "/" + enchantment.getPath()))
                    throw new IllegalArgumentException("Affinity path does not match enchantment id");
                EnumBloodTendency primary = tendency(json.get("primary").getAsString());
                EnumBloodTendency secondary = json.has("secondary")
                        ? tendency(json.get("secondary").getAsString()) : null;
                result.put(enchantment, new Affinity(primary, secondary));
            } catch (Exception exception) {
                Hemomancy.LOGGER.error("Invalid Scriptorium affinity {}: {}", file, exception.getMessage());
            }
        }
        return Map.copyOf(result);
    }

    @Override protected void apply(Map<ResourceLocation, Affinity> prepared, ResourceManager manager, ProfilerFiller profiler) {
        current = prepared;
    }

    private static EnumBloodTendency tendency(String id) {
        return EnumBloodTendency.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(java.util.Locale.ROOT));
    }
}
