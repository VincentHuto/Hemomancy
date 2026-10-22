package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;

/** A persistent Qliphoth Bloom as {@code PacketSyncQliphothBlooms} carries it to clients. */
public final class QliphothBloomData {
	private QliphothBloomData() {
	}

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
}
