package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		private final String phase;
		private final int instability;
		private final int currentWave;
		private final int totalWaves;
		private final int completedRings;
		private final int totalRings;
		private final int committedBloodMl;
		private final int upfrontBloodMl;
		private final int carriedIchorMl;
		private final int allyCount;
		private final int sharedBloodMl;
		private final String cue;
		private final float footprintRadius;
		private final List<String> checklist;
		private final List<CardinalRiteBoundaryProgress.Segment> boundarySegments;
		private final List<SigilSegment> sigilSegments;
		private final List<SanguineBlob> sanguineBlobs;

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained) {
			this(center, riteSize, progress, recipeId, unstained, "LEGACY", 0, 0, 0,
					Math.max(1, (riteSize - 1) / 2), Math.max(1, (riteSize - 1) / 2),
					0, 0, 0, 0, -1, "", List.of(), List.of(), List.of());
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs) {
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, 0.0F, List.of(), boundarySegments, sigilSegments, sanguineBlobs);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs) {
			this.center = center;
			this.riteSize = riteSize;
			this.progress = progress;
			this.recipeId = recipeId;
			this.unstained = unstained;
			this.phase = phase;
			this.instability = instability;
			this.currentWave = currentWave;
			this.totalWaves = totalWaves;
			this.completedRings = completedRings;
			this.totalRings = totalRings;
			this.committedBloodMl = committedBloodMl;
			this.upfrontBloodMl = upfrontBloodMl;
			this.carriedIchorMl = carriedIchorMl;
			this.allyCount = allyCount;
			this.sharedBloodMl = sharedBloodMl;
			this.cue = cue;
			this.footprintRadius = footprintRadius;
			this.checklist = List.copyOf(checklist);
			this.boundarySegments = List.copyOf(boundarySegments);
			this.sigilSegments = List.copyOf(sigilSegments);
			this.sanguineBlobs = List.copyOf(sanguineBlobs);
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
		public String getPhase() { return phase; }
		public int getInstability() { return instability; }
		public int getCurrentWave() { return currentWave; }
		public int getTotalWaves() { return totalWaves; }
		public int getCompletedRings() { return completedRings; }
		public int getTotalRings() { return totalRings; }
		public int getCommittedBloodMl() { return committedBloodMl; }
		public int getUpfrontBloodMl() { return upfrontBloodMl; }
		public int getCarriedIchorMl() { return carriedIchorMl; }
		public int getAllyCount() { return allyCount; }
		public int getSharedBloodMl() { return sharedBloodMl; }
		public String getCue() { return cue; }
		public float getFootprintRadius() { return footprintRadius; }
		public List<String> getChecklist() { return checklist; }
		public List<CardinalRiteBoundaryProgress.Segment> getBoundarySegments() { return boundarySegments; }
		public List<SigilSegment> getSigilSegments() { return sigilSegments; }
		public List<SanguineBlob> getSanguineBlobs() { return sanguineBlobs; }
	}

	public record SigilSegment(
			double startX, double startY, double startZ,
			double endX, double endY, double endZ,
			int color) {
	}

	public static final class SanguineBlob {
		private static final float GROWTH_EASING = 0.22F;

		private final double x;
		private final double y;
		private final double z;
		private final float radius;
		private final int color;
		private final long seed;
		private final float integrity;
		private float previousRenderRadius;
		private float currentRenderRadius;

		public SanguineBlob(double x, double y, double z, float radius, int color, long seed) {
			this(x, y, z, radius, color, seed, 1.0F);
		}

		public SanguineBlob(double x, double y, double z, float radius,
				int color, long seed, float integrity) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.radius = Math.max(0.0F, radius);
			this.color = color;
			this.seed = seed;
			this.integrity = integrity;
			this.previousRenderRadius = this.radius;
			this.currentRenderRadius = this.radius;
		}

		public double x() { return x; }
		public double y() { return y; }
		public double z() { return z; }
		public float radius() { return radius; }
		public int color() { return color; }
		public long seed() { return seed; }
		public float integrity() { return integrity; }

		public float renderRadius(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousRenderRadius
					+ (currentRenderRadius - previousRenderRadius) * clampedPartialTick;
		}

		private void beginGrowth() {
			previousRenderRadius = 0.0F;
			currentRenderRadius = 0.0F;
		}

		private void continueGrowthFrom(SanguineBlob previous) {
			float visibleRadius = previous.currentRenderRadius;
			previousRenderRadius = visibleRadius;
			currentRenderRadius = visibleRadius;
		}

		private void tickGrowth() {
			previousRenderRadius = currentRenderRadius;
			currentRenderRadius += (radius - currentRenderRadius) * GROWTH_EASING;
			if (Math.abs(radius - currentRenderRadius) < 0.0001F) {
				currentRenderRadius = radius;
			}
		}
	}

	private static List<RiteEntry> activeRites = Collections.emptyList();

	public static void set(List<RiteEntry> rites) {
		Map<BlobKey, SanguineBlob> previousBlobs = new HashMap<>();
		for (RiteEntry entry : activeRites) {
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				previousBlobs.put(new BlobKey(entry.getCenter(), blob.seed()), blob);
			}
		}
		for (RiteEntry entry : rites) {
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				SanguineBlob previous = previousBlobs.get(new BlobKey(entry.getCenter(), blob.seed()));
				if (previous == null) {
					blob.beginGrowth();
				} else {
					blob.continueGrowthFrom(previous);
				}
			}
		}
		activeRites = Collections.unmodifiableList(new ArrayList<>(rites));
	}

	public static void tick() {
		for (RiteEntry entry : activeRites) {
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
	}

	private record BlobKey(BlockPos center, long seed) {
	}
}
