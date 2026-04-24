package com.vincenthuto.hemomancy.client.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;

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

		public BloomEntry(BlockPos center, int chunkRadius) {
			this(center, chunkRadius, 0);
		}

		public BloomEntry(BlockPos center, int chunkRadius, int pomesDropped) {
			this.center = center;
			this.chunkRadius = chunkRadius;
			this.pomesDropped = pomesDropped;
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
