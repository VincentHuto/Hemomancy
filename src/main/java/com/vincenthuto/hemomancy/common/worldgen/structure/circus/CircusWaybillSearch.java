package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.util.OffThreadSearch;
import com.vincenthuto.hemomancy.common.worldgen.structure.StructureSitePrediction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The Scarlet Waybill's search, kept off the server thread like the Relay Chart's. {@link StructureSitePrediction}
 * finds the nearest predicted camp or pavilion on the background worker pool, and for pavilions every one within
 * {@link #UNFINISHED_RADIUS} as well; the answer comes back on the server thread, where {@link #pick} skips pavilions
 * whose performance is over. One search runs per player at a time; that state is session state, cleared through
 * {@code TransientServerStateEvents}.
 */
public final class CircusWaybillSearch {
	/** Rings of spacing cells searched: about 24,000 blocks each way for pavilions, 12,000 for camps. */
	public static final int MAX_RINGS = 32;
	/** How far from the asker an unfinished pavilion may be when the nearest one is finished. */
	public static final int UNFINISHED_RADIUS = 2560;
	public static final TagKey<Structure> PAVILIONS = TagKey.create(Registries.STRUCTURE,
			Hemomancy.rloc("circus_waybill_targets"));

	/** The nearest predicted site, and for pavilions every site within {@link #UNFINISHED_RADIUS}, nearest first. */
	public record Found(Optional<BlockPos> nearest, List<BlockPos> nearby) {
	}

	private static final OffThreadSearch<Void, Found> SEARCH = new OffThreadSearch<>();

	private CircusWaybillSearch() {
	}

	/** Searches for {@code tag}'s sites around {@code from} off the server thread; completes on the server thread. */
	public static CompletableFuture<Found> search(ServerLevel level, TagKey<Structure> tag, BlockPos from) {
		boolean pavilions = tag.equals(PAVILIONS);
		return SEARCH.runAndReturn(level.getServer(), () -> {
			Optional<BlockPos> nearest = StructureSitePrediction.nearest(level, tag, from, MAX_RINGS);
			List<BlockPos> nearby = pavilions && nearest.isPresent()
					? StructureSitePrediction.within(level, tag, from, UNFINISHED_RADIUS) : List.of();
			return new Found(nearest, nearby);
		});
	}

	/**
	 * The Waybill's target, on the server thread: the nearest site, except that a finished pavilion gives way to the
	 * nearest unfinished one within {@link #UNFINISHED_RADIUS}, or to nothing.
	 */
	@Nullable
	public static BlockPos pick(ServerLevel level, TagKey<Structure> tag, Found found) {
		if (found.nearest().isEmpty()) return null;
		BlockPos nearest = found.nearest().get();
		if (!tag.equals(PAVILIONS)) return nearest;
		var sites = CircusPavilionSavedData.get(level);
		if (!sites.isCompletedNear(level, nearest)) return nearest;
		for (BlockPos next : found.nearby()) {
			if (!sites.isCompletedNear(level, next)) return next;
		}
		return null;
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

	/** The thread that ran the last search, for the off-thread proof. */
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
}
