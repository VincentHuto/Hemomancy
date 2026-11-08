package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.util.OffThreadSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The Relay Chart's Seat search, kept off the server thread. {@link CourtSeatCandidates#nearest} runs on the
 * background worker pool against the biome source alone, and the result comes back on the server thread. Results are
 * cached per dimension and Seat cell of the asking position, and one search runs per player at a time, through the
 * shared {@link OffThreadSearch}. Both are session state, cleared through {@code TransientServerStateEvents}.
 */
public final class CourtSeatSearch {
	/** Rings of Seat cells searched around the asker: at the default spacing, about 6,700 blocks each way. */
	public static final int MAX_RINGS = 6;
	private static final int FALLBACK_CELL_BLOCKS = 1024;

	private record Key(ResourceKey<Level> dimension, int cellX, int cellZ) {
	}

	private static final OffThreadSearch<Key, Optional<BlockPos>> SEARCH = new OffThreadSearch<>();

	private CourtSeatSearch() {
	}

	/**
	 * The nearest Lighthouse Garden within {@link #MAX_RINGS} Seat cells of {@code from}, completed on the server
	 * thread. A cached answer completes at once.
	 */
	public static CompletableFuture<Optional<BlockPos>> nearest(ServerLevel level, BlockPos from) {
		return SEARCH.cached(level.getServer(), key(level, from), () -> CourtSeatCandidates.nearest(level, from, MAX_RINGS));
	}

	/** The uncached search alone, on the background worker pool; it never touches a chunk or the server thread. */
	public static CompletableFuture<Optional<BlockPos>> offThread(ServerLevel level, BlockPos from) {
		return SEARCH.run(() -> CourtSeatCandidates.nearest(level, from, MAX_RINGS));
	}

	/** Starts a search for {@code player}; false while that player's last search is still running. */
	public static boolean begin(UUID player) {
		return SEARCH.begin(player);
	}

	public static void finish(UUID player) {
		SEARCH.finish(player);
	}

	public static boolean pending(UUID player) {
		return SEARCH.pending(player);
	}

	/** The thread that ran the last uncached search, for the off-thread proof. */
	@Nullable
	public static Thread lastSearchThread() {
		return SEARCH.lastSearchThread();
	}

	public static void logout(UUID player) {
		SEARCH.logout(player);
	}

	public static void clearSessionState() {
		SEARCH.clear();
	}

	private static Key key(ServerLevel level, BlockPos from) {
		var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(CourtSeatCandidates.SEATS);
		int cellBlocks = set != null && set.placement() instanceof RandomSpreadStructurePlacement spread
				? spread.spacing() * 16 : FALLBACK_CELL_BLOCKS;
		return new Key(level.dimension(), Math.floorDiv(from.getX(), cellBlocks), Math.floorDiv(from.getZ(), cellBlocks));
	}
}
