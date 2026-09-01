package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class BloodwoodGrowthRules {
	public static final double PROJECTION_BLOOD_COST = 900.0D;

	private static final Set<Offset> LOG_OFFSETS = buildLogOffsets();
	private static final Set<Offset> LEAF_OFFSETS = buildLeafOffsets();
	private static final Set<Offset> REQUIRED_CLEARANCE = buildRequiredClearance();

	private BloodwoodGrowthRules() {
	}

	public static Set<Offset> logOffsets() {
		return LOG_OFFSETS;
	}

	public static Set<Offset> leafOffsets() {
		return LEAF_OFFSETS;
	}

	public static Set<Offset> requiredClearance() {
		return REQUIRED_CLEARANCE;
	}

	private static Set<Offset> buildLogOffsets() {
		LinkedHashSet<Offset> offsets = new LinkedHashSet<>();
		for (int y = 0; y <= 5; y++) {
			offsets.add(new Offset(0, y, 0));
		}

		addBranch(offsets, 1, 5, 0, 4, 6, 0);
		addBranch(offsets, -1, 5, 0, -4, 6, 0);
		addBranch(offsets, 0, 5, 1, 0, 6, 4);
		addBranch(offsets, 0, 5, -1, 0, 6, -4);
		addBranch(offsets, 1, 5, 1, 3, 7, 3);
		addBranch(offsets, -1, 5, -1, -3, 7, -3);
		addBranch(offsets, -1, 5, 1, -3, 6, 3);
		return Collections.unmodifiableSet(offsets);
	}

	private static void addBranch(Set<Offset> offsets, int startX, int startY, int startZ,
			int endX, int endY, int endZ) {
		int steps = Math.max(Math.abs(endX - startX), Math.max(Math.abs(endY - startY), Math.abs(endZ - startZ)));
		for (int i = 0; i <= steps; i++) {
			double t = steps == 0 ? 0.0D : (double) i / steps;
			int x = (int) Math.round(startX + (endX - startX) * t);
			int y = (int) Math.round(startY + (endY - startY) * t);
			int z = (int) Math.round(startZ + (endZ - startZ) * t);
			offsets.add(new Offset(x, y, z));
		}
	}

	private static Set<Offset> buildLeafOffsets() {
		LinkedHashSet<Offset> offsets = new LinkedHashSet<>();
		for (int x = -5; x <= 5; x++) {
			for (int z = -5; z <= 5; z++) {
				int distance = Math.abs(x) + Math.abs(z);
				if (distance <= 6 && !(Math.abs(x) == 5 && Math.abs(z) > 1) && !(Math.abs(z) == 5 && Math.abs(x) > 1)) {
					offsets.add(new Offset(x, 6, z));
				}
				if (distance <= 4) {
					offsets.add(new Offset(x, 7, z));
				}
				if (distance <= 2) {
					offsets.add(new Offset(x, 8, z));
				}
			}
		}
		for (Offset branchEnd : new Offset[] {
				new Offset(4, 6, 0), new Offset(-4, 6, 0),
				new Offset(0, 6, 4), new Offset(0, 6, -4),
				new Offset(3, 7, 3), new Offset(-3, 7, -3),
				new Offset(-3, 6, 3)
		}) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					offsets.add(new Offset(branchEnd.x() + x, branchEnd.y(), branchEnd.z() + z));
				}
			}
		}
		offsets.removeAll(LOG_OFFSETS);
		return Collections.unmodifiableSet(offsets);
	}

	private static Set<Offset> buildRequiredClearance() {
		LinkedHashSet<Offset> offsets = new LinkedHashSet<>();
		offsets.addAll(LOG_OFFSETS);
		offsets.addAll(LEAF_OFFSETS);
		return Collections.unmodifiableSet(offsets);
	}

	public record Offset(int x, int y, int z) {
	}
}
