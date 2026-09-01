package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.Set;

public final class BloodwoodGrowthRulesTest {
	private BloodwoodGrowthRulesTest() {
	}

	public static void main(String[] args) {
		Set<?> logs = BloodwoodGrowthRules.logOffsets();
		Set<?> leaves = BloodwoodGrowthRules.leafOffsets();
		assertEquals(900.0D, BloodwoodGrowthRules.PROJECTION_BLOOD_COST, "projection cost");
		assertTrue(BloodwoodGrowthRules.requiredClearance().size() > 80,
				"clearance footprint should cover tree volume");
		assertTrue(logs.size() >= 18, "tree should have trunk and branches");
		assertTrue(leaves.size() >= 70, "tree should have broad dragon-tree canopy");
		for (Object log : logs) {
			assertFalse(leaves.contains(log), "logs and leaves should not overlap");
		}
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(boolean value, String label) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
