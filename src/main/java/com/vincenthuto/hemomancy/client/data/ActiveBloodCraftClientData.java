package com.vincenthuto.hemomancy.client.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;

/**
 * Client-side cache for active blood craft ring animations.
 * Each entry represents a collapsing bloody ring that plays when
 * a blood structure recipe is activated.
 */
public class ActiveBloodCraftClientData {

	public static class CraftRingEntry {
		private final BlockPos center;
		private final float startRadius;
		private final float centerY;
		/** Total duration of the collapse animation in ticks. */
		private final int totalTicks;
		/** Remaining ticks until the ring is gone. */
		private int remainingTicks;

		public CraftRingEntry(BlockPos center, float startRadius, float centerY, int totalTicks) {
			this.center = center;
			this.startRadius = startRadius;
			this.centerY = centerY;
			this.totalTicks = totalTicks;
			this.remainingTicks = totalTicks;
		}

		public BlockPos getCenter() {
			return center;
		}

		public float getStartRadius() {
			return startRadius;
		}

		public float getCenterY() {
			return centerY;
		}

		public int getTotalTicks() {
			return totalTicks;
		}

		public int getRemainingTicks() {
			return remainingTicks;
		}

		/** Progress from 0.0 (just started) to 1.0 (fully collapsed). */
		public double getProgress() {
			return 1.0 - (double) remainingTicks / totalTicks;
		}

		/** Tick down; returns true if the animation is finished. */
		public boolean tick() {
			return --remainingTicks <= 0;
		}
	}

	private static final List<CraftRingEntry> activeRings = new ArrayList<>();

	public static void addRing(CraftRingEntry ring) {
		activeRings.add(ring);
	}

	public static List<CraftRingEntry> getActiveRings() {
		return activeRings;
	}

	/** Called each client tick to advance animations and prune finished ones. */
	public static void tick() {
		Iterator<CraftRingEntry> it = activeRings.iterator();
		while (it.hasNext()) {
			if (it.next().tick()) {
				it.remove();
			}
		}
	}

	public static void clear() {
		activeRings.clear();
	}
}
