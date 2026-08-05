package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data-driven ceremony declaration attached to a Cardinal Rite recipe.
 */
public record CardinalRiteCeremonyDefinition(
		CardinalRiteCeremonyProfile profile,
		List<Anchor> anchors,
		List<SupportSocket> supportSockets,
		List<String> waves,
		List<String> guaranteedWaves,
		List<BlockPos> fragileOffsets,
		int targetDurationTicks,
		String focusMode,
		int requiredHelpers,
		List<String> helperRoles,
		int stillIntervalTicks,
		Atmosphere atmosphere,
		String failureProfile) {

	public CardinalRiteCeremonyDefinition {
		profile = profile == null ? CardinalRiteCeremonyProfile.STANDARD : profile;
		anchors = List.copyOf(anchors == null ? List.of() : anchors);
		supportSockets = List.copyOf(supportSockets == null ? List.of() : supportSockets);
		waves = List.copyOf(waves == null ? List.of() : waves);
		guaranteedWaves = List.copyOf(guaranteedWaves == null ? List.of() : guaranteedWaves);
		fragileOffsets = List.copyOf(fragileOffsets == null ? List.of() : fragileOffsets);
		targetDurationTicks = Math.max(1, targetDurationTicks);
		focusMode = focusMode == null ? "" : focusMode;
		requiredHelpers = Math.max(0, requiredHelpers);
		helperRoles = List.copyOf(helperRoles == null ? List.of() : helperRoles);
		stillIntervalTicks = Math.max(0, stillIntervalTicks);
		atmosphere = atmosphere == null ? new Atmosphere("none", false, false) : atmosphere;
		failureProfile = failureProfile == null ? "" : failureProfile;
	}

	public int anchorBloodCostMl() {
		return anchors.size() * CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
	}

	public boolean abbreviated() {
		return profile == CardinalRiteCeremonyProfile.SIMPLE;
	}

	/**
	 * Generates the standard four-node-per-ring circulation for data-driven
	 * rites that select an authored layout family instead of listing every
	 * anchor coordinate by hand.
	 */
	public static List<Anchor> anchorsForLayout(int rings, int rotation,
			CardinalRiteCeremonyCatalog.Layout layout) {
		List<Anchor> anchors = new ArrayList<>();
		Set<BlockPos> occupied = new HashSet<>();
		for (int ring = 1; ring <= rings; ring++) {
			if (layout == CardinalRiteCeremonyCatalog.Layout.CARDINAL) {
				for (int step = 0; step < 4; step++) {
					int placementOrder = (step + rotation) & 3;
					Anchor tuned = CardinalRiteRingTuning.anchor(ring - 1, placementOrder, 1);
					anchors.add(new Anchor(tuned.x(), tuned.y(), tuned.z(), ring - 1, step));
					occupied.add(new BlockPos(tuned.x(), 0, tuned.z()));
				}
				continue;
			}
			int radius = ring + 2;
			int diagonalX = Math.max(1, (int) Math.round(radius / Math.sqrt(2.0D)));
			int diagonalZ = diagonalX;
			while (layout == CardinalRiteCeremonyCatalog.Layout.DIAGONAL
					&& diagonalRingOverlaps(occupied, diagonalX, diagonalZ)) {
				diagonalZ++;
			}
			int[][] points = switch (layout) {
				case DIAGONAL -> new int[][] {{-diagonalX,-diagonalZ},{diagonalX,-diagonalZ},
						{diagonalX,diagonalZ},{-diagonalX,diagonalZ}};
				case CROOKED -> new int[][] {{0,-radius},{radius,1},{0,radius},{-radius,-1}};
				case SERPENTINE -> new int[][] {{-radius,-1},{0,-radius},{radius,1},{0,radius}};
				default -> new int[][] {{0,-radius},{radius,0},{0,radius},{-radius,0}};
			};
			for (int step = 0; step < 4; step++) {
				int index = (step + rotation) & 3;
				int[] rotated = rotateForRing(points[index][0], points[index][1], ring - 1);
				int insetX = insetTowardCenter(rotated[0]);
				int insetZ = insetTowardCenter(rotated[1]);
				int[] staggered = staggerClearOfPillars(insetX, insetZ, ring - 1);
				anchors.add(new Anchor(staggered[0], 1, staggered[1], ring - 1, step));
				occupied.add(new BlockPos(staggered[0], 0, staggered[1]));
			}
		}
		return anchors;
	}

	private static int insetTowardCenter(int coordinate) {
		return coordinate - Integer.signum(coordinate);
	}

	private static int[] staggerClearOfPillars(int x, int z, int ringIndex) {
		if (ringIndex != 1) return new int[] {x, z};
		double angle = Math.PI / 8.0D;
		double cosine = Math.cos(angle);
		double sine = Math.sin(angle);
		return new int[] {
				(int) Math.round(x * cosine - z * sine),
				(int) Math.round(x * sine + z * cosine)
		};
	}

	private static int[] rotateForRing(int x, int z, int ringIndex) {
		double angle = Math.max(0, ringIndex) * Math.PI / 4.0D;
		double cosine = Math.cos(angle);
		double sine = Math.sin(angle);
		return new int[] {
				(int) Math.round(x * cosine - z * sine),
				(int) Math.round(x * sine + z * cosine)
		};
	}

	private static boolean diagonalRingOverlaps(Set<BlockPos> occupied, int x, int z) {
		return occupied.contains(new BlockPos(-x, 0, -z))
				|| occupied.contains(new BlockPos(x, 0, -z))
				|| occupied.contains(new BlockPos(x, 0, z))
				|| occupied.contains(new BlockPos(-x, 0, z));
	}

	public record Anchor(int x, int y, int z, int ring, int order) {
		public BlockPos offset() {
			return new BlockPos(x, y, z);
		}
	}

	public record SupportSocket(int x, int y, int z, String suggestedSigil, boolean required) {
		public BlockPos offset() {
			return new BlockPos(x, y, z);
		}
	}

	public record Atmosphere(String fog, boolean lightning, boolean dome) {
		public Atmosphere {
			fog = fog == null ? "none" : fog;
		}
	}
}
