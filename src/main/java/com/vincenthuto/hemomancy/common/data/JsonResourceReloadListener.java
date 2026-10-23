package com.vincenthuto.hemomancy.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * R-6: the base of Hemomancy's server data loaders. It reads every {@code <namespace>:<folder>/**.json} file, decodes
 * it with a {@link Codec} (or a parser when the definition needs its own id), logs and skips a bad file, and publishes
 * the whole map only after preparation completes. Register a loader in {@code Hemomancy.onAddReloadListeners}.
 */
public class JsonResourceReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
    private static final Gson GSON = new Gson();
    private final FileToIdConverter files;
    private final String label;
    private final Logger logger;
    private final BiFunction<ResourceLocation, JsonObject, T> parser;
    private final Consumer<Map<ResourceLocation, T>> publisher;

    public JsonResourceReloadListener(String folder, String label, Logger logger,
            BiFunction<ResourceLocation, JsonObject, T> parser, Consumer<Map<ResourceLocation, T>> publisher) {
        this.files = FileToIdConverter.json(folder);
        this.label = label;
        this.logger = logger;
        this.parser = parser;
        this.publisher = publisher;
    }

    public JsonResourceReloadListener(String folder, String label, Logger logger, Codec<T> codec,
            Consumer<Map<ResourceLocation, T>> publisher) {
        this(folder, label, logger, decoding(codec), publisher);
    }

    /** A parser that decodes the file with a codec; for loaders that check the decoded value against its id. */
    public static <T> BiFunction<ResourceLocation, JsonObject, T> decoding(Codec<T> codec) {
        return (id, root) -> codec.parse(JsonOps.INSTANCE, root).getOrThrow(JsonSyntaxException::new);
    }

    @Override
    protected final Map<ResourceLocation, T> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, T> loaded = new HashMap<>();
        files.listMatchingResources(manager).forEach((file, resource) -> {
            try (var reader = resource.openAsReader()) {
                ResourceLocation id = files.fileToId(file);
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) throw new JsonSyntaxException("Expected a JSON object");
                T definition = Objects.requireNonNull(parser.apply(id, root), "Parser returned no definition");
                if (loaded.putIfAbsent(id, definition) != null) {
                    throw new JsonSyntaxException("Duplicate definition " + id);
                }
            } catch (Exception exception) {
                logger.error("Failed to load {} {}: {}", label, file, exception.getMessage());
            }
        });
        return Map.copyOf(loaded);
    }

    @Override
    protected final void apply(Map<ResourceLocation, T> prepared, ResourceManager manager, ProfilerFiller profiler) {
        publisher.accept(Map.copyOf(prepared));
        logger.info("Loaded {} {} definitions", prepared.size(), label);
    }
}
