package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.util.List;

public final class MaxBloodRules {
	private MaxBloodRules() {
	}

	public static MaxBloodSnapshot snapshot(double baseMax, List<MaxBloodContribution> contributions) {
		double safeBase = Math.max(1.0D, finiteOrZero(baseMax));
		double positive = 0.0D;
		double negative = 0.0D;
		List<MaxBloodContribution> safeContributions = contributions == null ? List.of() : contributions;
		for (MaxBloodContribution contribution : safeContributions) {
			positive += contribution.positiveAmount();
			negative += contribution.negativeAmount();
		}
		double net = positive - negative;
		double total = Math.max(1.0D, safeBase + net);
		return new MaxBloodSnapshot(safeBase, safeContributions, positive, negative, net, total);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
