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

    static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
        var jsonOps = jsonOps(ops);
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> json.add(
                ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new),
                ops.convertTo(jsonOps, pair.getSecond())));
        return json;
    }
}
