package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

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
		/** Added to an anchor's blood in {@link #getAnchorStates()} when the anchor is broken. */
		public static final int ANCHOR_DAMAGED = 1000;
		private final BlockPos center;
		private final int riteSize;
		private final double progress;
		private final ResourceLocation recipeId;
		private final boolean unstained;
		private final String phase;
		/** Parsed {@link #phase}; {@code null} for the client-only SUCCESSION phase. */
		private final CardinalRitePhase ritePhase;
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
		/** Per authored anchor: blood in mL, plus {@link #ANCHOR_DAMAGED} when instability has broken it. */
		private int[] anchorStates = new int[0];
		/** The rite's world stage and how many steps it has; see {@code CardinalRiteEscalation}. */
		private int escalation;
		private int escalationTotal;
		private int openRings = -1;
		private int[] anchorRequirements = new int[0];
		private List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems = List.of();
		private List<SocketMarker> socketMarkers = List.of();
		/** A Working craft's subject blocks, their block state ids, and how full the floor's climb is. */
		private List<BlockPos> subjectPositions = List.of();
		private int[] subjectStateIds = new int[0];
		private float subjectFill;
		private final Map<BoundarySegmentKey, BoundaryGrowth> boundaryGrowth;
		private float previousStainFade;
		private float currentStainFade;
		private float previousCancellationTicks;
		private float currentCancellationTicks;
		private float previousStaffPlantingTicks;
		private float currentStaffPlantingTicks;
		private float previousPhaseTicks;
		private float currentPhaseTicks;
		/** Procession spin carried into culmination so the rings keep turning without a snap. */
		private float carriedSpinTicks;

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
			this.phase = phase == null ? "LEGACY" : phase;
			this.ritePhase = SUCCESSION_PHASE.equals(this.phase) ? null : CardinalRitePhase.byName(this.phase);
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
		/** The typed phase, or {@code null} for the client-only SUCCESSION phase. */
		public CardinalRitePhase ritePhase() { return ritePhase; }
		public boolean isPhase(CardinalRitePhase candidate) { return ritePhase == candidate; }
		public boolean isLegacy() { return ritePhase == CardinalRitePhase.LEGACY; }
		public boolean isSuccession() { return ritePhase == null; }
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
		public int[] getAnchorStates() { return anchorStates; }
		public RiteEntry withAnchorStates(int[] states) {
			anchorStates = states == null ? new int[0] : states.clone();
			return this;
		}
		public int getEscalation() { return escalation; }
		public int getEscalationTotal() { return escalationTotal; }
		/** Rings woken so far; every ring when the server sent none. */
		public int getOpenRings() { return openRings < 0 ? totalRings : openRings; }
		public int[] getAnchorRequirements() { return anchorRequirements; }
		/** An anchor's requirement in mL; the flat cost when the server sent none. */
		public int anchorRequirement(int anchorIndex) {
			return anchorIndex >= 0 && anchorIndex < anchorRequirements.length ? anchorRequirements[anchorIndex]
					: com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
		}
		/** Whether this anchor's ring still sleeps. */
		public boolean anchorDormant(int anchorIndex) {
			return anchorIndex >= 0 && anchorIndex < anchorRequirements.length && anchorRequirements[anchorIndex] == 0;
		}
		public RiteEntry withRings(int open, int[] requirements) {
			openRings = Math.max(0, open);
			anchorRequirements = requirements == null ? new int[0] : requirements.clone();
			return this;
		}
		public RiteEntry withEscalation(int stage, int total) {
			escalationTotal = Math.max(0, total);
			escalation = Math.clamp(stage, 0, escalationTotal);
			return this;
		}
		public List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> getProblems() {
			return problems;
		}
		public List<SocketMarker> getSocketMarkers() { return socketMarkers; }
		public RiteEntry withGuidance(List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems,
				List<SocketMarker> markers) {
			this.problems = problems == null ? List.of() : List.copyOf(problems);
			this.socketMarkers = markers == null ? List.of() : List.copyOf(markers);
			return this;
		}
		public List<BlockPos> getSubjectPositions() { return subjectPositions; }
		public int[] getSubjectStateIds() { return subjectStateIds; }
		public float getSubjectFill() { return subjectFill; }
		public boolean hasSubject() { return !subjectPositions.isEmpty(); }
		public RiteEntry withSubject(List<BlockPos> positions, int[] stateIds, float fill) {
			subjectPositions = positions == null ? List.of() : List.copyOf(positions);
			subjectStateIds = stateIds == null ? new int[0] : stateIds.clone();
			subjectFill = Math.clamp(fill, 0.0F, 1.0F);
			return this;
		}
		public static int anchorBlood(int state) { return state % ANCHOR_DAMAGED; }
		public static boolean anchorDamaged(int state) { return state >= ANCHOR_DAMAGED; }

		public float boundaryGrowth(CardinalRiteBoundaryProgress.Segment segment, float partialTick) {
			BoundaryGrowth growth = boundaryGrowth.get(BoundarySegmentKey.of(segment));
			return growth == null ? 1.0F : growth.render(partialTick);
		}

		public float boundaryEffectAge(CardinalRiteBoundaryProgress.Segment segment, float partialTick) {
			BoundaryGrowth growth = boundaryGrowth.get(BoundarySegmentKey.of(segment));
			return growth == null ? -1.0F : growth.renderEffectAge(partialTick);
		}

		public float stainFadeProgress(float partialTick) {
			return interpolate(previousStainFade, currentStainFade, partialTick);
		}

		public float cancellationRenderTicks(float partialTick) {
			return interpolate(previousCancellationTicks, currentCancellationTicks, partialTick);
		}

		public float staffPlantingRenderTicks(float partialTick) {
			return interpolate(previousStaffPlantingTicks, currentStaffPlantingTicks, partialTick);
		}

		public float phaseRenderTicks(float partialTick) {
			return interpolate(previousPhaseTicks, currentPhaseTicks, partialTick);
		}

		/**
		 * Ticks the finale rings have been spinning: procession ticks, then
		 * culmination ticks on top of the procession this client saw, so the spin
		 * starts from rest and stays continuous across the phase change.
		 */
		public float ringSpinTicks(float partialTick) {
			if (ritePhase == CardinalRitePhase.OFFERING_PROCESSION) return phaseRenderTicks(partialTick);
			if (ritePhase == CardinalRitePhase.CULMINATION) return carriedSpinTicks + phaseRenderTicks(partialTick);
			return 0.0F;
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
				carriedSpinTicks = previous.carriedSpinTicks;
			} else if (ritePhase == CardinalRitePhase.CULMINATION
					&& previous.ritePhase == CardinalRitePhase.OFFERING_PROCESSION) {
				carriedSpinTicks = previous.carriedSpinTicks + previous.currentPhaseTicks;
			}
			for (var entry : boundaryGrowth.entrySet()) {
				BoundaryGrowth previousGrowth = previous.boundaryGrowth.get(entry.getKey());
				if (previousGrowth != null) entry.getValue().continueFrom(previousGrowth);
			}
		}

		/**
		 * First sight of a rite (joining, changing dimension, walking into range):
		 * what is already sealed is shown sealed, without replaying its growth or
		 * its seal sounds. Only segments that appear later grow in.
		 */
		private void showExistingProgress() {
			for (BoundaryGrowth growth : boundaryGrowth.values()) growth.completeSilently();
			if (!boundarySegments.isEmpty() || phaseTicks >= STAIN_FADE_TICKS) {
				previousStainFade = 1.0F;
				currentStainFade = 1.0F;
			}
		}

		private void tickBoundaryGrowth() {
			previousStainFade = currentStainFade;
			currentStainFade = Math.min(1.0F, currentStainFade + 1.0F / STAIN_FADE_TICKS);
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

	/** A required support socket whose sigil still sleeps; the client raises a blood thread over it. */
	public record SocketMarker(BlockPos pos, String sigil) {
		public SocketMarker {
			sigil = sigil == null ? "" : sigil;
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
			return interpolate(previousRenderRadius, currentRenderRadius, partialTick);
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

	private static final String SUCCESSION_PHASE = "SUCCESSION";
	private static final float STAIN_FADE_TICKS = 30.0F;
	/** How long a rite that left the sync list can still be found for its ending. */
	private static final int RECENTLY_ENDED_TICKS = 20;

	private static List<RiteEntry> activeRites = Collections.emptyList();
	private static final Map<BlockPos, EndedRite> RECENTLY_ENDED = new HashMap<>();
	private static long clientTicks;

	private static float interpolate(float previous, float current, float partialTick) {
		float clampedPartialTick = Math.max(0.0F, Math.min(1.0F, partialTick));
		return previous + (current - previous) * clampedPartialTick;
	}

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
			return interpolate(previous, current, partialTick);
		}

		private void completeSilently() {
			previous = 1.0F;
			current = 1.0F;
			previousEffectAge = -1.0F;
			currentEffectAge = -1.0F;
			completionPending = false;
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
			return interpolate(previousEffectAge, currentEffectAge, partialTick);
		}

		private boolean consumeCompletion() {
			if (!completionPending) return false;
			completionPending = false;
			return true;
		}
	}
}
