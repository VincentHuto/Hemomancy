package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData.RiteEntry;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData.SanguineBlob;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Client-side cache for active cardinal rite boundaries.
 * Updated by PacketSyncActiveRites from the server.
 */
public class ActiveRiteClientData {
	/** How long a rite that left the sync list can still be found for its ending. */
	private static final int RECENTLY_ENDED_TICKS = 20;

	private static List<RiteEntry> activeRites = Collections.emptyList();
	private static final Map<BlockPos, EndedRite> RECENTLY_ENDED = new HashMap<>();
	private static long clientTicks;

	public static void set(List<RiteEntry> rites) {
		Map<BlobKey, SanguineBlob> previousBlobs = new HashMap<>();
		Map<RiteKey, RiteEntry> previousRites = new HashMap<>();
		for (RiteEntry entry : activeRites) {
			previousRites.put(new RiteKey(entry.getCenter(), entry.getRecipeId()), entry);
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				previousBlobs.put(new BlobKey(entry.getCenter(), blob.seed()), blob);
			}
		}
		for (RiteEntry entry : rites) {
			RiteEntry previousRite = previousRites.remove(
					new RiteKey(entry.getCenter(), entry.getRecipeId()));
			if (previousRite != null) {
				entry.continueBoundaryGrowthFrom(previousRite);
			} else {
				entry.showExistingProgress();
			}
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				SanguineBlob previous = previousBlobs.get(new BlobKey(entry.getCenter(), blob.seed()));
				if (previous == null) {
					blob.beginGrowth();
				} else {
					blob.continueGrowthFrom(previous);
				}
			}
		}
		for (RiteEntry ended : previousRites.values()) {
			RECENTLY_ENDED.put(ended.getCenter(), new EndedRite(ended, clientTicks));
		}
		activeRites = Collections.unmodifiableList(new ArrayList<>(rites));
	}

	/**
	 * A rite that left the sync list within the last second at this centre, or
	 * {@code null}. Lets a terminal event that arrives just after the snapshot
	 * that removed its rite still find the rite's last state for its ending.
	 */
	public static RiteEntry recentlyEnded(BlockPos center) {
		EndedRite ended = RECENTLY_ENDED.get(center);
		return ended == null ? null : ended.entry();
	}

	public static void tick() {
		clientTicks++;
		RECENTLY_ENDED.values().removeIf(ended -> clientTicks - ended.endedTick() > RECENTLY_ENDED_TICKS);
		for (RiteEntry entry : activeRites) {
			entry.tickBoundaryGrowth();
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				blob.tickGrowth();
			}
		}
	}

	public static List<RiteEntry> getActiveRites() {
		return activeRites;
	}

	public static void clear() {
		activeRites = Collections.emptyList();
		RECENTLY_ENDED.clear();
	}

	public static boolean isStaffPlanted(UUID playerId) {
		return playerId != null && activeRites.stream()
				.anyMatch(rite -> rite.hasPlantedStaff() && playerId.equals(rite.getOwner()));
	}

	private record BlobKey(BlockPos center, long seed) {
	}

	private record RiteKey(BlockPos center, ResourceLocation recipeId) {
	}

	private record EndedRite(RiteEntry entry, long endedTick) {
	}

}
