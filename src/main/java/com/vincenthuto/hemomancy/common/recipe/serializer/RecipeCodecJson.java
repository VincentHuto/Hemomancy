package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import net.minecraft.resources.RegistryOps;

/** JSON schema helpers that retain the caller's registry lookup for nested codecs. */
final class RecipeCodecJson {
    private RecipeCodecJson() {}

    static DynamicOps<JsonElement> jsonOps(DynamicOps<?> ops) {
        return ops instanceof RegistryOps<?> registries ? registries.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE;
    }

    /**
     * A boolean field that also reads the 0/1 bytes NBT stores booleans as: a recipe decoded through NBT reaches
     * {@link #toJsonObject} with numbers where its JSON had {@code true}/{@code false}.
     */
    static boolean booleanField(JsonObject json, String name, boolean fallback) {
        if (!json.has(name) || !json.get(name).isJsonPrimitive()) return fallback;
        var value = json.getAsJsonPrimitive(name);
        return value.isNumber() ? value.getAsInt() != 0 : value.getAsBoolean();
    }

    static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
        var jsonOps = jsonOps(ops);
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
                ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new),
                ops.convertTo(jsonOps, pair.getSecond())));
        return json;
    }
}
