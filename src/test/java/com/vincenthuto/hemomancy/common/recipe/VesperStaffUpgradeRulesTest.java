package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusRules;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VesperStaffUpgradeRulesTest {
	private VesperStaffUpgradeRulesTest() {
	}

	public static void main(String[] args) {
		matchesOnlyOneStaffAndOneMemory();
		upgradeDataPreservesExistingStaffDataAndAddsMarker();
	}

	private static void matchesOnlyOneStaffAndOneMemory() {
		assertTrue("one staff and one memory matches",
				VesperStaffUpgradeRules.matchesUpgrade(1, 1, 0));
		assertFalse("two staffs do not match",
				VesperStaffUpgradeRules.matchesUpgrade(2, 1, 0));
		assertFalse("missing memory does not match",
				VesperStaffUpgradeRules.matchesUpgrade(1, 0, 0));
		assertFalse("extra item does not match",
				VesperStaffUpgradeRules.matchesUpgrade(1, 1, 1));
	}

	private static void upgradeDataPreservesExistingStaffDataAndAddsMarker() {
		Map<String, Object> original = new LinkedHashMap<>();
		original.put("Inventory", "preserved");
		original.put("BloodHandled", 81.0D);

		Map<String, Object> upgraded = VesperStaffUpgradeRules.copyAndAwaken(original);

		assertEquals("inventory data preserved", "preserved", upgraded.get("Inventory"));
		assertEquals("blood handled data preserved", 81.0D, upgraded.get("BloodHandled"));
		assertEquals("Vesper marker added", Boolean.TRUE,
				upgraded.get(LivingStaffFocusRules.VESPER_MEMORY_AWAKENED_KEY));
		assertFalse("original map is not mutated",
				original.containsKey(LivingStaffFocusRules.VESPER_MEMORY_AWAKENED_KEY));
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
