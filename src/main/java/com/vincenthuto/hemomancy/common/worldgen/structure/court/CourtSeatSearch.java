package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Relay Chart's Seat search, kept off the server thread. {@link CourtSeatCandidates#nearest} runs on the
 * background worker pool against the biome source alone, and the result comes back on the server thread. Results are
 * cached per dimension and Seat cell of the asking position, and one search runs per player at a time. Both maps are
 * session state, cleared through {@code TransientServerStateEvents}.
 */
public final class CourtSeatSearch {
	/** Rings of Seat cells searched around the asker: at the default spacing, about 6,700 blocks each way. */
	public static final int MAX_RINGS = 6;
	private static final int FALLBACK_CELL_BLOCKS = 1024;

	private record Key(ResourceKey<Level> dimension, int cellX, int cellZ) {
	}

	private static final Map<Key, Optional<BlockPos>> CACHE = new ConcurrentHashMap<>();
	private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();
	@Nullable
	private static volatile Thread lastSearchThread;

	private CourtSeatSearch() {
	}

	/**
	 * The nearest Lighthouse Garden within {@link #MAX_RINGS} Seat cells of {@code from}, completed on the server
	 * thread. A cached answer completes at once.
	 */
	public static CompletableFuture<Optional<BlockPos>> nearest(ServerLevel level, BlockPos from) {
		Key key = key(level, from);
		Optional<BlockPos> cached = CACHE.get(key);
		if (cached != null) return CompletableFuture.completedFuture(cached);
		return offThread(level, from).thenApplyAsync(found -> {
			CACHE.put(key, found);
			return found;
		}, level.getServer());
	}

	/** The uncached search alone, on the background worker pool; it never touches a chunk or the server thread. */
	public static CompletableFuture<Optional<BlockPos>> offThread(ServerLevel level, BlockPos from) {
		return CompletableFuture.supplyAsync(() -> {
			lastSearchThread = Thread.currentThread();
			return CourtSeatCandidates.nearest(level, from, MAX_RINGS);
		}, Util.backgroundExecutor());
	}

	/** Starts a search for {@code player}; false while that player's last search is still running. */
	public static boolean begin(UUID player) {
		return PENDING.add(player);
	}

	public static void finish(UUID player) {
		PENDING.remove(player);
	}

	public static boolean pending(UUID player) {
		return PENDING.contains(player);
	}

	/** The thread that ran the last uncached search, for the off-thread proof. */
	@Nullable
	public static Thread lastSearchThread() {
		return lastSearchThread;
	}

	public static void logout(UUID player) {
		PENDING.remove(player);
	}

	public static void clearSessionState() {
		CACHE.clear();
		PENDING.clear();
		lastSearchThread = null;
	}

	private static Key key(ServerLevel level, BlockPos from) {
		var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(CourtSeatCandidates.SEATS);
		int cellBlocks = set != null && set.placement() instanceof RandomSpreadStructurePlacement spread
				? spread.spacing() * 16 : FALLBACK_CELL_BLOCKS;
		return new Key(level.dimension(), Math.floorDiv(from.getX(), cellBlocks), Math.floorDiv(from.getZ(), cellBlocks));
	}
}
