package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record EntityBloodProfile(List<EnumBloodTendency> tendencies, List<ResourceLocation> properties,
                                 boolean requiresLivingSyringe) {
    public static final EntityBloodProfile EMPTY = new EntityBloodProfile(List.of(), List.of(), false);
    public static final ResourceLocation FUNGAL = ResourceLocation.parse("hemomancy:fungal");

    public EntityBloodProfile {
        tendencies = tendencies.stream().distinct().sorted().toList();
        properties = properties.stream().distinct().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
    }

    public static EntityBloodProfile parse(JsonObject json) {
        var tendencies = new ArrayList<EnumBloodTendency>();
        for (String name : strings(json, "tendencies")) {
            if (!name.equals(name.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Tendency must be lowercase: " + name);
            tendencies.add(EnumBloodTendency.valueOf(name.toUpperCase(Locale.ROOT)));
        }
        var properties = new ArrayList<ResourceLocation>();
        for (String id : strings(json, "properties")) {
            if (!id.contains(":")) throw new IllegalArgumentException("Property must be namespaced: " + id);
            properties.add(ResourceLocation.parse(id));
        }
        boolean requires = false;
        if (json.has("requires_living_syringe")) {
            var value = json.get("requires_living_syringe");
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
                throw new IllegalArgumentException("requires_living_syringe must be a boolean");
            requires = value.getAsBoolean();
        }
        return new EntityBloodProfile(tendencies, properties, requires);
    }

    private static List<String> strings(JsonObject json, String field) {
        if (!json.has(field)) return List.of();
        var values = json.get(field);
        if (!values.isJsonArray()) throw new IllegalArgumentException(field + " must be an array");
        var result = new ArrayList<String>();
        for (JsonElement value : values.getAsJsonArray()) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
                throw new IllegalArgumentException(field + " must contain strings");
            result.add(value.getAsString());
        }
        return result;
    }
}
