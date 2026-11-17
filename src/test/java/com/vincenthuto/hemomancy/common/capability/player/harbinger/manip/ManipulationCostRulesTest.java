package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution.Category;

import java.util.List;

public final class ManipulationCostRulesTest {
	private ManipulationCostRulesTest() {
	}

	@Test
	void allChecksPass() {
		discountsAndSurchargesStackMultiplicatively();
		neutralSourcesRemainVisibleWithoutChangingCost();
		unsafeMultipliersCannotCreateNegativeOrInvalidCosts();
		stackedDiscountsStopAtTheFloor();
		surchargesStillApplyOverTheFloor();
		aBlockedLineStaysBlockedUnderTheFloor();
	}

	private static void stackedDiscountsStopAtTheFloor() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(300.0D, List.of(
				ManipulationCostContribution.multiplier("efficiency", "Efficiency", Category.SKILL, 0.5D),
				ManipulationCostContribution.multiplier("blood_moon", "Blood Moon", Category.WORLD, 0.75D),
				ManipulationCostContribution.multiplier("sanguine_dominion", "Sanguine Dominion", Category.RITE, 0.75D)));

		assertDouble("discounts never go below 0.4x", 0.4D, snapshot.discountMultiplier());
		assertDouble("the floor holds the total at 0.4x", 0.4D, snapshot.totalMultiplier());
		assertDouble("the floored price", 120.0D, snapshot.effectiveCost());
		var floor = snapshot.contribution(ManipulationCostRules.DISCOUNT_FLOOR_SOURCE)
				.orElseThrow(() -> new AssertionError("the floor shows as its own ledger line"));
		assertDouble("the floor line lifts the discounts back to 0.4x", 0.4D / (0.5D * 0.75D * 0.75D), floor.multiplier());

		ManipulationCostSnapshot above = ManipulationCostRules.snapshot(100.0D, List.of(
				ManipulationCostContribution.multiplier("efficiency", "Efficiency", Category.SKILL, 0.5D)));
		if (above.contribution(ManipulationCostRules.DISCOUNT_FLOOR_SOURCE).isPresent()) {
			throw new AssertionError("a discount above the floor adds no floor line");
		}
		assertDouble("a discount above the floor is untouched", 50.0D, above.effectiveCost());
	}

	private static void surchargesStillApplyOverTheFloor() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(100.0D, List.of(
				ManipulationCostContribution.multiplier("efficiency", "Efficiency", Category.SKILL, 0.3D),
				ManipulationCostContribution.multiplier("clotted_vein", "Clotted vein", Category.OTHER, 1.25D)));

		assertDouble("the surcharge multiplies the floored discount", 0.5D, snapshot.totalMultiplier());
		assertDouble("surcharge reports only real surcharges", 1.25D, snapshot.surchargeMultiplier());
		assertDouble("floored price with a surcharge", 50.0D, snapshot.effectiveCost());
	}

	private static void aBlockedLineStaysBlockedUnderTheFloor() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(100.0D, List.of(
				ManipulationCostContribution.multiplier("purity", "Purity", Category.TEMPER, 0.0D),
				ManipulationCostContribution.multiplier("efficiency", "Efficiency", Category.SKILL, 0.5D)));

		assertDouble("a blocked line keeps the total at zero", 0.0D, snapshot.totalMultiplier());
		if (snapshot.contribution(ManipulationCostRules.DISCOUNT_FLOOR_SOURCE).isPresent()) {
			throw new AssertionError("a blocked line is not lifted by the floor");
		}
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
