package com.vincenthuto.hemomancy.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

final class JsonResourceReloadListenerTest {
    @Test
    void reloadPublishesOnlyAfterPreparationBarrier() {
        var published = new AtomicReference<Map<ResourceLocation, String>>(Map.of(id("custom:old"), "old"));
        var ready = new CompletableFuture<Void>();
        var barrier = new PreparableReloadListener.PreparationBarrier() {
            @Override public <T> CompletableFuture<T> wait(T prepared) {
                return ready.thenApply(ignored -> prepared);
            }
        };
        var manager = new Resources(Map.of(id("custom:definitions/new.json"),
                resource("{\"name\":\"new\"}", new AtomicInteger())));
        var completion = loader(published).reload(barrier, manager, null, null, Runnable::run, Runnable::run);
        assertFalse(completion.isDone());
        assertEquals(Map.of(id("custom:old"), "old"), published.get());
        ready.complete(null);
        completion.join();
        assertEquals(Map.of(id("custom:new"), "new"), published.get());
    }

    @Test
    void scansNestedNamespacedJsonAsUtf8AndClosesEveryOpenedResource() {
        var closed = new AtomicInteger();
        var manager = new Resources(Map.of(
                id("custom:definitions/nested/first.json"), resource("{\"name\":\"Mémoire 血\"}", closed),
                id("other:definitions/first.json"), resource("{\"name\":\"Other\"}", closed),
                id("custom:definitions/ignored.txt"), resource("ignored", closed),
                id("custom:unrelated/file.json"), resource("{}", closed)));
        var published = new AtomicReference<Map<ResourceLocation, String>>(Map.of());
        var loader = loader(published);
        var prepared = loader.prepare(manager, null);
        assertEquals(Map.of(id("custom:nested/first"), "Mémoire 血", id("other:first"), "Other"), prepared);
        assertEquals(2, closed.get());
        assertTrue(published.get().isEmpty(), "Preparation must not publish");
        assertThrows(UnsupportedOperationException.class, prepared::clear);
    }

    @Test
    void badJsonSchemaAndIoFailuresDoNotDiscardOtherDefinitions() {
        var closed = new AtomicInteger();
        var files = new LinkedHashMap<ResourceLocation, Resource>();
        files.put(id("custom:definitions/broken.json"), resource("{", closed));
        files.put(id("custom:definitions/schema.json"), resource("{}", closed));
        files.put(id("custom:definitions/null.json"), resource("null", closed));
        files.put(id("custom:definitions/array.json"), resource("[]", closed));
        files.put(id("custom:definitions/io.json"), new Resource(null, () -> { throw new IOException("unreadable"); }));
        files.put(id("custom:definitions/good.json"), resource("{\"name\":\"survivor\"}", closed));
        var prepared = loader(new AtomicReference<>()).prepare(new Resources(files), null);
        assertEquals(Map.of(id("custom:good"), "survivor"), prepared);
        assertEquals(5, closed.get(), "Readers must close after parse failures too");
    }

    @Test
    void publicationReplacesSnapshotAndEmptyReloadRemovesStaleEntries() {
        var published = new AtomicReference<Map<ResourceLocation, String>>(Map.of(id("custom:old"), "old"));
        var loader = loader(published);
        var previous = published.get();
        var incoming = new LinkedHashMap<>(Map.of(id("custom:new"), "new"));
        loader.apply(incoming, ResourceManager.Empty.INSTANCE, null);
        incoming.clear();
        var snapshot = published.get();
        assertEquals(Map.of(id("custom:new"), "new"), snapshot);
        assertEquals(Map.of(id("custom:old"), "old"), previous);
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        loader.apply(loader.prepare(ResourceManager.Empty.INSTANCE, null), ResourceManager.Empty.INSTANCE, null);
        assertTrue(published.get().isEmpty());
        assertEquals(1, snapshot.size(), "Previously returned snapshots must remain stable");
    }

    private static JsonResourceReloadListener<String> loader(AtomicReference<Map<ResourceLocation, String>> published) {
        return new JsonResourceReloadListener<>("definitions", "test", LogManager.getLogger(),
                (id, json) -> json.get("name").getAsString(), published::set);
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    private static Resource resource(String json, AtomicInteger closed) {
        return new Resource(null, () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                closed.incrementAndGet();
                super.close();
            }
        });
    }

    private record Resources(Map<ResourceLocation, Resource> files) implements ResourceManager {
        @Override public Set<String> getNamespaces() { return Set.of("custom", "other"); }
        @Override public Optional<Resource> getResource(ResourceLocation id) { return Optional.ofNullable(files.get(id)); }
        @Override public List<Resource> getResourceStack(ResourceLocation id) { return getResource(id).stream().toList(); }
        @Override public Stream<PackResources> listPacks() { return Stream.empty(); }
        @Override public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
            return files.entrySet().stream()
                    .filter(entry -> entry.getKey().getPath().startsWith(path + "/") && filter.test(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        @Override public Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter) {
            throw new AssertionError("Scanning must use resolved resources, preserving resource-pack priority");
        }
    }
}
