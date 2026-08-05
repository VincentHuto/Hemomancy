package com.vincenthuto.hemomancy.client.data;

import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Client-side cache for persistent Qliphoth Bloom locations.
 * Updated by PacketSyncQliphothBlooms from the server.
 * Used by QliphothBloomRenderer to draw the tree and pulsing rings.
 */
public class QliphothBloomClientData {

	public static class BloomEntry {
		private final BlockPos center;
		private final int chunkRadius;
		/** Number of Qliphoth Pomes already dropped (0–9). Controls the growth stage. */
		private final int pomesDropped;
		private final int severedState;

		public BloomEntry(BlockPos center, int chunkRadius) {
			this(center, chunkRadius, 0, 0);
		}

		public BloomEntry(BlockPos center, int chunkRadius, int pomesDropped) {
			this(center, chunkRadius, pomesDropped, 0);
		}

		public BloomEntry(BlockPos center, int chunkRadius, int pomesDropped, int severedState) {
			this.center = center;
			this.chunkRadius = chunkRadius;
			this.pomesDropped = pomesDropped;
			this.severedState = severedState;
		}

		public BlockPos getCenter() {
			return center;
		}

		public int getChunkRadius() {
			return chunkRadius;
		}

		/** Returns the number of pomes dropped so far (0–9), which drives the visual growth stage. */
		public int getPomesDropped() {
			return pomesDropped;
		}

		public int getSeveredState() { return severedState; }
		public boolean isPortalOpen() { return severedState == 1; }
		public boolean isSealedTrophy() { return severedState == 2; }
	}

	private static List<BloomEntry> activeBlooms = Collections.emptyList();
	private static Set<BlockPos> activeCenters = Collections.emptySet();

	public static void set(List<BloomEntry> blooms) {
		List<BloomEntry> bloomCopy = Collections.unmodifiableList(new ArrayList<>(blooms));
		Set<BlockPos> centers = new HashSet<>(bloomCopy.size());
		for (BloomEntry bloom : bloomCopy) {
			centers.add(bloom.getCenter());
		}
		activeBlooms = bloomCopy;
		activeCenters = Collections.unmodifiableSet(centers);
	}

	public static List<BloomEntry> getActiveBlooms() {
		return activeBlooms;
	}

	public static void clear() {
		activeBlooms = Collections.emptyList();
		activeCenters = Collections.emptySet();
	}

	public static boolean containsCenter(BlockPos pos) {
		return activeCenters.contains(pos);
	}
}
