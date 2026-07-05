package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution.Category;

import java.util.List;

public final class MaxBloodRulesTest {
	private MaxBloodRulesTest() {
	}

	public static void main(String[] args) {
		capacitySpleenAndEternalCovenantStackAdditively();
		scarPenaltiesSubtractAfterBonuses();
		negativeTotalsClampToMinimumCapacity();
	}

	private static void capacitySpleenAndEternalCovenantStackAdditively() {
		MaxBloodSnapshot snapshot = MaxBloodRules.snapshot(5000.0D, List.of(
				MaxBloodContribution.bonus("capacity", "Capacity", Category.SKILL, 1500.0D),
				MaxBloodContribution.bonus("spleen", "Spleen", Category.ORGAN, 3000.0D),
				MaxBloodContribution.bonus("eternal_covenant", "Eternal Covenant", Category.RITE, 500.0D)));

		assertDouble("base is preserved", 5000.0D, snapshot.baseMax());
		assertDouble("positive max-blood bonuses add together", 5000.0D, snapshot.positiveBonus());
		assertDouble("no penalties present", 0.0D, snapshot.negativePenalty());
		assertDouble("total max blood is base plus all bonuses", 10000.0D, snapshot.totalMax());
	}

	private static void scarPenaltiesSubtractAfterBonuses() {
		MaxBloodSnapshot snapshot = MaxBloodRules.snapshot(5000.0D, List.of(
				MaxBloodContribution.bonus("capacity", "Capacity", Category.SKILL, 2500.0D),
				MaxBloodContribution.penalty("scar_heart", "Scar Heart", Category.SCAR, 100.0D),
				MaxBloodContribution.penalty("scar_phoenix", "Scar Phoenix", Category.SCAR, 400.0D)));

		assertDouble("capacity bonus is positive", 2500.0D, snapshot.positiveBonus());
		assertDouble("scar penalties are tracked positively as a penalty total", 500.0D, snapshot.negativePenalty());
		assertDouble("scar penalties subtract from the final max", 7000.0D, snapshot.totalMax());
		assertDouble("scar contribution remains negative for display", -400.0D,
				snapshot.contribution("scar_phoenix").orElseThrow().amount());
	}

	private static void negativeTotalsClampToMinimumCapacity() {
		MaxBloodSnapshot snapshot = MaxBloodRules.snapshot(5000.0D, List.of(
				MaxBloodContribution.penalty("catastrophic_scar", "Catastrophic Scar", Category.SCAR, 7000.0D)));

		assertDouble("total max blood never falls below one", 1.0D, snapshot.totalMax());
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
