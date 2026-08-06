package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side cache for active cardinal rite boundaries.
 * Updated by PacketSyncActiveRites from the server.
 */
public class ActiveRiteClientData {
	public enum NodeKind {
		BOUNDARY_ANCHOR,
		SIGIL_NODE
	}

	public static class RiteEntry {
		private final BlockPos center;
		private final int riteSize;
		private final double progress;
		private final ResourceLocation recipeId;
		private final boolean unstained;
		private final String phase;
		private final int phaseTicks;
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
		private final boolean plantedStaff;
		private final UUID owner;
		private final int cancellationTicks;
		private final int staffPlantingTicks;
		private final String fogProfile;
		private final boolean fogLightning;
		private final boolean boundaryDome;
		private final List<String> checklist;
		private final List<CardinalRiteBoundaryProgress.Segment> boundarySegments;
		private final List<SigilSegment> sigilSegments;
		private final List<SanguineBlob> sanguineBlobs;
		private final Map<BoundarySegmentKey, BoundaryGrowth> boundaryGrowth;
		private float previousStainFade;
		private float currentStainFade;
		private float previousCancellationTicks;
		private float currentCancellationTicks;
		private float previousStaffPlantingTicks;
		private float currentStaffPlantingTicks;
		private float previousPhaseTicks;
		private float currentPhaseTicks;

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
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, footprintRadius, checklist, boundarySegments, sigilSegments,
					sanguineBlobs, false, null);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs, boolean plantedStaff,
				UUID owner) {
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, footprintRadius, checklist, boundarySegments, sigilSegments,
					sanguineBlobs, plantedStaff, owner, 0);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs, boolean plantedStaff,
				UUID owner, int cancellationTicks) {
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, footprintRadius, checklist, boundarySegments, sigilSegments,
					sanguineBlobs, plantedStaff, owner, cancellationTicks, -1);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs, boolean plantedStaff,
				UUID owner, int cancellationTicks, int staffPlantingTicks) {
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, footprintRadius, checklist, boundarySegments, sigilSegments,
					sanguineBlobs, plantedStaff, owner, cancellationTicks, staffPlantingTicks,
					unstained ? "none" : "storm", !unstained, !unstained);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs, boolean plantedStaff,
				UUID owner, int cancellationTicks, int staffPlantingTicks,
				String fogProfile, boolean fogLightning, boolean boundaryDome) {
			this(center, riteSize, progress, recipeId, unstained, phase, instability, currentWave, totalWaves,
					completedRings, totalRings, committedBloodMl, upfrontBloodMl, carriedIchorMl, allyCount,
					sharedBloodMl, cue, footprintRadius, checklist, boundarySegments, sigilSegments,
					sanguineBlobs, plantedStaff, owner, cancellationTicks, staffPlantingTicks,
					fogProfile, fogLightning, boundaryDome, 0);
		}

		public RiteEntry(BlockPos center, int riteSize, double progress, ResourceLocation recipeId, boolean unstained,
				String phase, int instability, int currentWave, int totalWaves, int completedRings, int totalRings,
				int committedBloodMl, int upfrontBloodMl, int carriedIchorMl, int allyCount, int sharedBloodMl,
				String cue, float footprintRadius, List<String> checklist,
				List<CardinalRiteBoundaryProgress.Segment> boundarySegments,
				List<SigilSegment> sigilSegments, List<SanguineBlob> sanguineBlobs, boolean plantedStaff,
				UUID owner, int cancellationTicks, int staffPlantingTicks,
				String fogProfile, boolean fogLightning, boolean boundaryDome, int phaseTicks) {
			this.center = center;
			this.riteSize = riteSize;
			this.progress = progress;
			this.recipeId = recipeId;
			this.unstained = unstained;
			this.phase = phase;
			this.phaseTicks = Math.max(0, phaseTicks);
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
			this.plantedStaff = plantedStaff;
			this.owner = owner;
			this.cancellationTicks = Math.max(0, cancellationTicks);
			this.staffPlantingTicks = staffPlantingTicks;
			this.fogProfile = normalizeFogProfile(fogProfile);
			this.fogLightning = fogLightning && !"none".equals(this.fogProfile);
			this.boundaryDome = boundaryDome;
			this.previousCancellationTicks = this.cancellationTicks;
			this.currentCancellationTicks = this.cancellationTicks;
			this.previousStaffPlantingTicks = this.staffPlantingTicks;
			this.currentStaffPlantingTicks = this.staffPlantingTicks;
			this.previousPhaseTicks = this.phaseTicks;
			this.currentPhaseTicks = this.phaseTicks;
			this.checklist = List.copyOf(checklist);
			this.boundarySegments = List.copyOf(boundarySegments);
			this.sigilSegments = List.copyOf(sigilSegments);
			this.sanguineBlobs = List.copyOf(sanguineBlobs);
			this.boundaryGrowth = new HashMap<>();
			for (CardinalRiteBoundaryProgress.Segment segment : boundarySegments) {
				this.boundaryGrowth.put(BoundarySegmentKey.of(segment), new BoundaryGrowth());
			}
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

		private static String normalizeFogProfile(String profile) {
			return switch (profile == null ? "none" : profile.toLowerCase(java.util.Locale.ROOT)) {
				case "faint", "dense", "storm" -> profile.toLowerCase(java.util.Locale.ROOT);
				default -> "none";
			};
		}
		public String getPhase() { return phase; }
		public int getPhaseTicks() { return phaseTicks; }
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
		public boolean hasPlantedStaff() { return plantedStaff; }
		public UUID getOwner() { return owner; }
		public int getCancellationTicks() { return cancellationTicks; }
		public int getStaffPlantingTicks() { return staffPlantingTicks; }
		public String getFogProfile() { return fogProfile; }
		public boolean hasFog() { return !"none".equals(fogProfile); }
		public boolean hasFogLightning() { return fogLightning; }
		public boolean hasBoundaryDome() { return boundaryDome; }
		public List<String> getChecklist() { return checklist; }
		public List<CardinalRiteBoundaryProgress.Segment> getBoundarySegments() { return boundarySegments; }
		public List<SigilSegment> getSigilSegments() { return sigilSegments; }
		public List<SanguineBlob> getSanguineBlobs() { return sanguineBlobs; }

		public float boundaryGrowth(CardinalRiteBoundaryProgress.Segment segment, float partialTick) {
			BoundaryGrowth growth = boundaryGrowth.get(BoundarySegmentKey.of(segment));
			return growth == null ? 1.0F : growth.render(partialTick);
		}

		public float boundaryEffectAge(CardinalRiteBoundaryProgress.Segment segment, float partialTick) {
			BoundaryGrowth growth = boundaryGrowth.get(BoundarySegmentKey.of(segment));
			return growth == null ? -1.0F : growth.renderEffectAge(partialTick);
		}

		public float stainFadeProgress(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousStainFade
					+ (currentStainFade - previousStainFade) * clampedPartialTick;
		}

		public float cancellationRenderTicks(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousCancellationTicks
					+ (currentCancellationTicks - previousCancellationTicks) * clampedPartialTick;
		}

		public float staffPlantingRenderTicks(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousStaffPlantingTicks
					+ (currentStaffPlantingTicks - previousStaffPlantingTicks) * clampedPartialTick;
		}

		public float phaseRenderTicks(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousPhaseTicks
					+ (currentPhaseTicks - previousPhaseTicks) * clampedPartialTick;
		}

		public boolean consumeBoundaryCompletion(CardinalRiteBoundaryProgress.Segment segment) {
			BoundaryGrowth growth = boundaryGrowth.get(BoundarySegmentKey.of(segment));
			return growth != null && growth.consumeCompletion();
		}

		private void continueBoundaryGrowthFrom(RiteEntry previous) {
			previousStainFade = previous.previousStainFade;
			currentStainFade = previous.currentStainFade;
			previousCancellationTicks = previous.previousCancellationTicks;
			currentCancellationTicks = previous.currentCancellationTicks;
			float continuedPlantingTicks = previous.currentStaffPlantingTicks;
			previousStaffPlantingTicks = continuedPlantingTicks;
			currentStaffPlantingTicks = Math.max(continuedPlantingTicks, staffPlantingTicks);
			if (phase.equals(previous.phase)) {
				float continuedPhaseTicks = Math.max(previous.currentPhaseTicks, phaseTicks);
				previousPhaseTicks = continuedPhaseTicks;
				currentPhaseTicks = continuedPhaseTicks;
			}
			for (var entry : boundaryGrowth.entrySet()) {
				BoundaryGrowth previousGrowth = previous.boundaryGrowth.get(entry.getKey());
				if (previousGrowth != null) entry.getValue().continueFrom(previousGrowth);
			}
		}

		private void tickBoundaryGrowth() {
			previousStainFade = currentStainFade;
			currentStainFade = Math.min(1.0F, currentStainFade + 1.0F / 30.0F);
			previousCancellationTicks = currentCancellationTicks;
			if (currentCancellationTicks < cancellationTicks) {
				currentCancellationTicks = Math.min(cancellationTicks, currentCancellationTicks + 1.0F);
			} else if (currentCancellationTicks > cancellationTicks && cancellationTicks == 0) {
				currentCancellationTicks = Math.max(0.0F, currentCancellationTicks - 0.5F);
			}
			previousStaffPlantingTicks = currentStaffPlantingTicks;
			if (currentStaffPlantingTicks >= 0.0F
					&& currentStaffPlantingTicks < CardinalRitePlantingSequence.DURATION_TICKS) {
				currentStaffPlantingTicks = Math.min(
						CardinalRitePlantingSequence.DURATION_TICKS,
						currentStaffPlantingTicks + 1.0F);
			}
			previousPhaseTicks = currentPhaseTicks;
			currentPhaseTicks++;
			for (BoundaryGrowth growth : boundaryGrowth.values()) growth.tick();
		}
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
		private final NodeKind kind;
		private final IchorianSigilAnatomy.Role role;
		private float previousRenderRadius;
		private float currentRenderRadius;

		public SanguineBlob(double x, double y, double z, float radius, int color, long seed) {
			this(x, y, z, radius, color, seed, 1.0F, NodeKind.SIGIL_NODE,
					IchorianSigilAnatomy.Role.JOINT);
		}

		public SanguineBlob(double x, double y, double z, float radius,
				int color, long seed, float integrity) {
			this(x, y, z, radius, color, seed, integrity, NodeKind.SIGIL_NODE,
					IchorianSigilAnatomy.Role.JOINT);
		}

		public SanguineBlob(double x, double y, double z, float radius,
				int color, long seed, float integrity, NodeKind kind,
				IchorianSigilAnatomy.Role role) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.radius = Math.max(0.0F, radius);
			this.color = color;
			this.seed = seed;
			this.integrity = integrity;
			this.kind = kind;
			this.role = role;
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
		public NodeKind kind() { return kind; }
		public IchorianSigilAnatomy.Role role() { return role; }

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
		Map<RiteKey, RiteEntry> previousRites = new HashMap<>();
		for (RiteEntry entry : activeRites) {
			previousRites.put(new RiteKey(entry.getCenter(), entry.getRecipeId()), entry);
			for (SanguineBlob blob : entry.getSanguineBlobs()) {
				previousBlobs.put(new BlobKey(entry.getCenter(), blob.seed()), blob);
			}
		}
		for (RiteEntry entry : rites) {
			RiteEntry previousRite = previousRites.get(
					new RiteKey(entry.getCenter(), entry.getRecipeId()));
			if (previousRite != null) entry.continueBoundaryGrowthFrom(previousRite);
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
	}

	public static boolean isStaffPlanted(UUID playerId) {
		return playerId != null && activeRites.stream()
				.anyMatch(rite -> rite.hasPlantedStaff() && playerId.equals(rite.getOwner()));
	}

	private record BlobKey(BlockPos center, long seed) {
	}

	private record RiteKey(BlockPos center, ResourceLocation recipeId) {
	}

	private record BoundarySegmentKey(
			int ring, int startAnchorIndex, long startAngleBits, long sweepAngleBits) {
		private static BoundarySegmentKey of(CardinalRiteBoundaryProgress.Segment segment) {
			return new BoundarySegmentKey(segment.ring(), segment.startAnchorIndex(),
					Double.doubleToLongBits(segment.startAngle()),
					Double.doubleToLongBits(segment.sweepAngle()));
		}
	}

	private static final class BoundaryGrowth {
		private static final float STEP = 1.0F / 20.0F;

		private float previous;
		private float current;
		private float previousEffectAge = -1.0F;
		private float currentEffectAge = -1.0F;
		private boolean completionPending;

		private float render(float partialTick) {
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previous + (current - previous) * clampedPartialTick;
		}

		private void continueFrom(BoundaryGrowth source) {
			previous = source.previous;
			current = source.current;
			previousEffectAge = source.previousEffectAge;
			currentEffectAge = source.currentEffectAge;
			completionPending = source.completionPending;
		}

		private void tick() {
			previous = current;
			if (current < 1.0F) {
				current = Math.min(1.0F, current + STEP);
				if (current >= 1.0F) {
					previousEffectAge = -1.0F;
					currentEffectAge = 0.0F;
					completionPending = true;
				}
			} else if (currentEffectAge >= 0.0F) {
				previousEffectAge = currentEffectAge;
				currentEffectAge++;
			}
		}

		private float renderEffectAge(float partialTick) {
			if (currentEffectAge < 0.0F) return -1.0F;
			float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
			return previousEffectAge
					+ (currentEffectAge - previousEffectAge) * clampedPartialTick;
		}

		private boolean consumeCompletion() {
			if (!completionPending) return false;
			completionPending = false;
			return true;
		}
	}
}
