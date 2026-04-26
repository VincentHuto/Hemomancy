package com.vincenthuto.hemomancy.client.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side cache for active cardinal rite boundaries.
 * Updated by PacketSyncActiveRites from the server.
 */
public class ActiveRiteClientData {

	public static class RiteEntry {
		private final BlockPos center;
		private final int riteSize;
		private final double progress;
		private final ResourceLocation recipeId;
		private final boolean unstained;

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained) {
			this.center = center;
			this.riteSize = riteSize;
			this.progress = progress;
			this.recipeId = recipeId;
			this.unstained = unstained;
		}

		public BlockPos getCenter() {
			return center;
		}

		public int getRiteSize() {
			return riteSize;
		}

		public double getProgress() {
			return progress;
		}

		public ResourceLocation getRecipeId() {
			return recipeId;
		}

		public boolean isUnstained() {
			return unstained;
		}
	}

	private static List<RiteEntry> activeRites = Collections.emptyList();

	public static void set(List<RiteEntry> rites) {
		activeRites = Collections.unmodifiableList(new ArrayList<>(rites));
	}

	public static List<RiteEntry> getActiveRites() {
		return activeRites;
	}

	public static void clear() {
		activeRites = Collections.emptyList();
	}
}
