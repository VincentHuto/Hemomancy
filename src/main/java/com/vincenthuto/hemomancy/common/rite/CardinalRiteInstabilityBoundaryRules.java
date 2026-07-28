package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CardinalRiteInstabilityBoundaryRules {
	public static final int REPAIR_BLOOD_ML = 50;

	private CardinalRiteInstabilityBoundaryRules() {
	}

	public static int brokenAnchorCount(double instabilityPercent, int anchorCount) {
		if (anchorCount <= 0) return 0;
		double scaled = Math.max(0.0D, Math.min(100.0D, instabilityPercent)) * anchorCount / 100.0D;
		return Math.min(anchorCount, (int) Math.floor(scaled + 1.0E-9D));
	}

	public static double flickerProgress(double instabilityPercent, int anchorCount) {
		if (anchorCount <= 0) return 0.0D;
		double scaled = Math.max(0.0D, Math.min(100.0D, instabilityPercent)) * anchorCount / 100.0D;
		if (scaled >= anchorCount) return 0.0D;
		return scaled - Math.floor(scaled + 1.0E-9D);
	}

	public static int repairInstabilityAmount(int anchorCount) {
		return anchorCount <= 0 ? 0 : Math.max(1, (int) Math.ceil(100.0D / anchorCount));
	}

	public static List<Integer> damagePriority(List<CardinalRiteCeremonyDefinition.Anchor> anchors) {
		List<Integer> priority = new ArrayList<>();
		for (int index = 0; index < anchors.size(); index++) priority.add(index);
		priority.sort(Comparator
				.<Integer>comparingInt(index -> anchors.get(index).ring()).reversed()
				.thenComparingInt(index -> anchors.get(index).order())
				.thenComparingInt(Integer::intValue));
		return List.copyOf(priority);
	}
}
