package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.List;

public final class ManipulationCostRules {
	private ManipulationCostRules() {
	}

	public static ManipulationCostSnapshot snapshot(double baseCost,
			List<ManipulationCostContribution> contributions) {
		double safeBase = Math.max(0.0D, finiteOrZero(baseCost));
		double discount = 1.0D;
		double surcharge = 1.0D;
		double total = 1.0D;
		List<ManipulationCostContribution> safeContributions =
				contributions == null ? List.of() : contributions;

		for (ManipulationCostContribution contribution : safeContributions) {
			double multiplier = contribution.multiplier();
			total *= multiplier;
			if (contribution.isDiscount()) {
				discount *= multiplier;
			} else if (contribution.isSurcharge()) {
				surcharge *= multiplier;
			}
		}

		double effective = Math.max(0.0D, safeBase * total);
		return new ManipulationCostSnapshot(safeBase, safeContributions, discount, surcharge, total, effective);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
