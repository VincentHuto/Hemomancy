package com.vincenthuto.hemomancy.client.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

		public BloomEntry(BlockPos center, int chunkRadius) {
			this.center = center;
			this.chunkRadius = chunkRadius;
		}

		public BlockPos getCenter() {
			return center;
		}

		public int getChunkRadius() {
			return chunkRadius;
		}
	}

	private static List<BloomEntry> activeBlooms = Collections.emptyList();

	public static void set(List<BloomEntry> blooms) {
		activeBlooms = Collections.unmodifiableList(new ArrayList<>(blooms));
	}

	public static List<BloomEntry> getActiveBlooms() {
		return activeBlooms;
	}

	public static void clear() {
		activeBlooms = Collections.emptyList();
	}
}
