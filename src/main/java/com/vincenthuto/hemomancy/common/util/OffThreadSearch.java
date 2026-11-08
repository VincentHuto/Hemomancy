package com.vincenthuto.hemomancy.common.util;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A slow world search run on the background worker pool, with its answer handed back on the server thread. One search
 * runs per player at a time, and answers can be cached by key. An owner keeps one as a static field and clears it, and
 * forgets a player on logout, through {@code TransientServerStateEvents}. The search itself must not load a chunk or
 * read state that only the server thread may touch.
 */
public final class OffThreadSearch<K, V> {
	private final Map<K, V> cache = new ConcurrentHashMap<>();
	private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
	@Nullable
	private volatile Thread lastSearchThread;

	/** The cached answer for {@code key} at once, or {@code search} off-thread, cached and completed on the server thread. */
	public CompletableFuture<V> cached(MinecraftServer server, K key, Supplier<V> search) {
		V hit = cache.get(key);
		if (hit != null) return CompletableFuture.completedFuture(hit);
		return run(search).thenApplyAsync(found -> {
			cache.put(key, found);
			return found;
		}, server);
	}

	/** {@code search} on the background worker pool, uncached; the future completes on that worker. */
	public CompletableFuture<V> run(Supplier<V> search) {
		return CompletableFuture.supplyAsync(() -> {
			lastSearchThread = Thread.currentThread();
			return search.get();
		}, Util.backgroundExecutor());
	}

	/** {@code search} off-thread, uncached, completed on the server thread. */
	public CompletableFuture<V> runAndReturn(MinecraftServer server, Supplier<V> search) {
		return run(search).thenApplyAsync(found -> found, server);
	}

	/** Starts a search for {@code player}; false while that player's last search is still running. */
	public boolean begin(UUID player) {
		return pending.add(player);
	}

	public void finish(UUID player) {
		pending.remove(player);
	}

	public boolean pending(UUID player) {
		return pending.contains(player);
	}

	/** The thread that ran the last uncached search, for the off-thread proof. */
	@Nullable
	public Thread lastSearchThread() {
		return lastSearchThread;
	}

	public void logout(UUID player) {
		pending.remove(player);
	}

	public void clear() {
		cache.clear();
		pending.clear();
		lastSearchThread = null;
	}
}
