package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution.Category;

import java.util.List;

public final class ManipulationCostRulesTest {
	private ManipulationCostRulesTest() {
	}

	public static void main(String[] args) {
		discountsAndSurchargesStackMultiplicatively();
		neutralSourcesRemainVisibleWithoutChangingCost();
		unsafeMultipliersCannotCreateNegativeOrInvalidCosts();
	}

	private static void discountsAndSurchargesStackMultiplicatively() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(200.0D, List.of(
				ManipulationCostContribution.multiplier("efficiency", "Efficiency", Category.SKILL, 0.8D),
				ManipulationCostContribution.multiplier("blood_moon", "Blood Moon", Category.WORLD, 0.75D),
				ManipulationCostContribution.multiplier("pome_corruption", "Pome Corruption", Category.RITE, 1.24D)));

		assertDouble("discount multipliers multiply together", 0.6D, snapshot.discountMultiplier());
		assertDouble("surcharge multipliers multiply together", 1.24D, snapshot.surchargeMultiplier());
		assertDouble("total multiplier includes discounts and surcharges", 0.744D, snapshot.totalMultiplier());
		assertDouble("effective cost uses final multiplier", 148.8D, snapshot.effectiveCost());
	}

	private static void neutralSourcesRemainVisibleWithoutChangingCost() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(100.0D, List.of(
				ManipulationCostContribution.multiplier("mastery", "Manipulation Mastery", Category.MASTERY, 1.0D)));

		assertDouble("neutral contribution does not change multiplier", 1.0D, snapshot.totalMultiplier());
		assertDouble("neutral contribution does not change cost", 100.0D, snapshot.effectiveCost());
		if (snapshot.contribution("mastery").isEmpty()) {
			throw new AssertionError("neutral contribution should remain available for diagnostics");
		}
	}

	private static void unsafeMultipliersCannotCreateNegativeOrInvalidCosts() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(Double.NaN, List.of(
				ManipulationCostContribution.multiplier("bad_config", "Bad Config", Category.OTHER, Double.NaN),
				ManipulationCostContribution.multiplier("negative", "Negative Multiplier", Category.OTHER, -1.0D)));

		assertDouble("invalid base cost becomes zero", 0.0D, snapshot.baseCost());
		assertDouble("invalid and negative multipliers are clamped into a non-negative total", 0.0D,
				snapshot.totalMultiplier());
		assertDouble("effective cost never goes negative or NaN", 0.0D, snapshot.effectiveCost());
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
