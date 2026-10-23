package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Stream;

/** JSON schema helpers that retain the caller's registry lookup for nested codecs. */
final class RecipeCodecJson {
    private RecipeCodecJson() {}

    /** Reads a recipe from its JSON object, with its id or the placeholder id. */
    interface JsonReader<R> {
        R read(ResourceLocation id, JsonObject json, DynamicOps<JsonElement> jsonOps);
    }

    /** Writes a recipe's fields; a generic static method reference fits. */
    interface FieldWriter<R> {
        <T> RecordBuilder<T> write(R recipe, DynamicOps<T> ops, RecordBuilder<T> prefix);
    }

    /**
     * L-8: the one hand-written recipe codec. It turns the map into a JSON object (keeping the registry lookup), so a
     * recipe parses the same from datapack JSON and from NBT, and reports any failure as a {@link DataResult} error.
     * A recipe without an {@code id} gets {@code hemomancy:<unknownId>}.
     */
    static <R> MapCodec<R> bridge(String recipeName, String unknownId, JsonReader<R> reader, FieldWriter<R> writer,
            String... keys) {
        List<String> fieldNames = List.of(keys);
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return fieldNames.stream().map(ops::createString);
            }

            @Override
            public <T> DataResult<R> decode(DynamicOps<T> ops, MapLike<T> input) {
                try {
                    JsonObject json = toJsonObject(ops, input);
                    ResourceLocation id = json.has("id")
                            ? ResourceLocation.parse(json.get("id").getAsString())
                            : Hemomancy.rloc(unknownId);
                    return DataResult.success(reader.read(id, json, jsonOps(ops)));
                } catch (Exception e) {
                    return DataResult.error(() -> "Failed to decode " + recipeName + ": " + e.getMessage());
                }
            }

            @Override
            public <T> RecordBuilder<T> encode(R recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return writer.write(recipe, ops, prefix);
            }
        };
    }

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
