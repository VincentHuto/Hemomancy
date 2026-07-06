package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution.Category;

import java.util.List;

public final class ManipulationSlotRulesTest {
	private ManipulationSlotRulesTest() {
	}

	public static void main(String[] args) {
		baseDegreeAndSkillStackUntilCap();
		skillLevelsPastCapAreReportedAsExcess();
		negativeSlotTotalsClampToZero();
	}

	private static void baseDegreeAndSkillStackUntilCap() {
		ManipulationSlotSnapshot snapshot = ManipulationSlotRules.snapshot(List.of(
				ManipulationSlotContribution.amount("base", "Base Slots", Category.BASE, 3),
				ManipulationSlotContribution.amount("degree", "Initiatory Degree", Category.DEGREE, 2),
				ManipulationSlotContribution.amount("skill", "Manipulation Slots", Category.SKILL, 3)),
				9);

		assertInt("requested slots add base, degree, and skill", 8, snapshot.requestedSlots());
		assertInt("applied slots stay below cap", 8, snapshot.appliedSlots());
		assertInt("no slot overflow below cap", 0, snapshot.cappedOffSlots());
	}

	private static void skillLevelsPastCapAreReportedAsExcess() {
		ManipulationSlotSnapshot snapshot = ManipulationSlotRules.snapshot(List.of(
				ManipulationSlotContribution.amount("base", "Base Slots", Category.BASE, 3),
				ManipulationSlotContribution.amount("degree", "Initiatory Degree", Category.DEGREE, 4),
				ManipulationSlotContribution.amount("skill", "Manipulation Slots", Category.SKILL, 5)),
				9);

		assertInt("applied slots are capped", 9, snapshot.appliedSlots());
		assertInt("overflow reports requested slots beyond cap", 3, snapshot.cappedOffSlots());
		assertInt("skill refund preserves legacy cap-room math", 3,
				ManipulationSlotRules.excessSkillLevels(3, 4, 5, 9));
	}

	private static void negativeSlotTotalsClampToZero() {
		ManipulationSlotSnapshot snapshot = ManipulationSlotRules.snapshot(List.of(
				ManipulationSlotContribution.amount("curse", "Slot Curse", Category.OTHER, -4)),
				9);

		assertInt("requested slots may show negative source math", -4, snapshot.requestedSlots());
		assertInt("applied slots never go below zero", 0, snapshot.appliedSlots());
	}

	private static void assertInt(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
