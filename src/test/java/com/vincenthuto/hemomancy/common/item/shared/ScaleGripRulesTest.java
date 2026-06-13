package com.vincenthuto.hemomancy.common.item.shared;

import java.util.List;

public final class ScaleGripRulesTest {
	private ScaleGripRulesTest() {
	}

	public static void main(String[] args) {
		selectsFirstEligibleNormalStack();
		neverSelectsScaleGrip();
		returnsNoSelectionForEmptyInventory();
	}

	private static void selectsFirstEligibleNormalStack() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true),
				new ScaleGripRules.Candidate(1, "minecraft:diamond_pickaxe", false, false),
				new ScaleGripRules.Candidate(2, "minecraft:iron_ingot", false, false));
		assertEquals(1, ScaleGripRules.selectProtectedSlot(candidates).orElse(-1), "first eligible slot");
	}

	private static void neverSelectsScaleGrip() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true),
				new ScaleGripRules.Candidate(1, "hemomancy:scale_grip", false, true));
		assertFalse(ScaleGripRules.selectProtectedSlot(candidates).isPresent(), "scale grip cannot protect itself");
	}

	private static void returnsNoSelectionForEmptyInventory() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "minecraft:air", true, false),
				new ScaleGripRules.Candidate(1, "hemomancy:scale_grip", false, true));
		assertFalse(ScaleGripRules.selectProtectedSlot(candidates).isPresent(), "empty inventory");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertFalse(boolean value, String label) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
