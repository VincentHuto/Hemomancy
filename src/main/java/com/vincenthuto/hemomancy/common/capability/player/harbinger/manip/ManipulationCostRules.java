package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution.Category;

import java.util.ArrayList;
import java.util.List;

public final class ManipulationCostRules {
	/** Stacked discounts never take a price below this share of its base (LP-6). Surcharges still apply on top. */
	public static final double DISCOUNT_FLOOR = 0.4D;
	public static final String DISCOUNT_FLOOR_SOURCE = "discount_floor";
	public static final String DISCOUNT_FLOOR_LABEL = "message.hemomancy.manipulation_cost_ledger.discount_floor";

	private ManipulationCostRules() {
	}

	public static ManipulationCostSnapshot snapshot(double baseCost,
			List<ManipulationCostContribution> contributions) {
		double safeBase = Math.max(0.0D, finiteOrZero(baseCost));
		double discount = 1.0D;
		double surcharge = 1.0D;
		double total = 1.0D;
		boolean blocked = false;
		List<ManipulationCostContribution> safeContributions =
				contributions == null ? List.of() : contributions;

		for (ManipulationCostContribution contribution : safeContributions) {
			double multiplier = contribution.multiplier();
			total *= multiplier;
			if (contribution.isDiscount()) {
				blocked |= multiplier <= 0.0D;
				discount *= multiplier;
			} else if (contribution.isSurcharge()) {
				surcharge *= multiplier;
			}
		}

		if (!blocked && discount < DISCOUNT_FLOOR) {
			double lift = DISCOUNT_FLOOR / discount;
			List<ManipulationCostContribution> floored = new ArrayList<>(safeContributions);
			floored.add(ManipulationCostContribution.multiplier(DISCOUNT_FLOOR_SOURCE, DISCOUNT_FLOOR_LABEL,
					Category.OTHER, lift));
			safeContributions = floored;
			total *= lift;
			discount = DISCOUNT_FLOOR;
		}

		double effective = Math.max(0.0D, safeBase * total);
		return new ManipulationCostSnapshot(safeBase, safeContributions, discount, surcharge, total, effective);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
