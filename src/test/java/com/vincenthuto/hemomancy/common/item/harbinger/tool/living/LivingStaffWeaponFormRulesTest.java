package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffWeaponFormRulesTest {
	private LivingStaffWeaponFormRulesTest() {
	}

	public static void main(String[] args) {
		hotSwapCostStartsAtTwoHundredFiftyAndDropsByFiftyPerLevel();
		livingWeaponFormNamesAreRecognized();
		matchingSelectedFormCanToggleOff();
	}

	private static void hotSwapCostStartsAtTwoHundredFiftyAndDropsByFiftyPerLevel() {
		assertEquals("negative level clamps to base cost", 250.0D,
				LivingStaffWeaponFormRules.hotSwapCostForWeaponsMasterLevel(-1));
		assertEquals("level zero uses base cost", 250.0D,
				LivingStaffWeaponFormRules.hotSwapCostForWeaponsMasterLevel(0));
		assertEquals("level one discounts by 50mL", 200.0D,
				LivingStaffWeaponFormRules.hotSwapCostForWeaponsMasterLevel(1));
		assertEquals("level four leaves a 50mL minimum", 50.0D,
				LivingStaffWeaponFormRules.hotSwapCostForWeaponsMasterLevel(4));
		assertEquals("levels above max keep the 50mL minimum", 50.0D,
				LivingStaffWeaponFormRules.hotSwapCostForWeaponsMasterLevel(5));
	}

	private static void livingWeaponFormNamesAreRecognized() {
		assertTrue("blade form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_blade"));
		assertTrue("axe form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_axe"));
		assertTrue("spear form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_spear"));
		assertTrue("claws form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_claws"));
		assertTrue("crossbow form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_crossbow"));
		assertTrue("torch form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_torch"));
		assertTrue("flail form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_flail"));
		assertFalse("staff conjuration is not a weapon form",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_staff"));
	}

	private static void matchingSelectedFormCanToggleOff() {
		assertTrue("matching spear form toggles off",
				LivingStaffWeaponFormRules.shouldToggleOffSelectedForm("conjure_spear", "conjure_spear"));
		assertFalse("different selected form does not toggle off",
				LivingStaffWeaponFormRules.shouldToggleOffSelectedForm("conjure_spear", "conjure_blade"));
		assertFalse("non-weapon selected manipulation does not toggle off",
				LivingStaffWeaponFormRules.shouldToggleOffSelectedForm("conjure_staff", "conjure_staff"));
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Double.compare(expected, actual) != 0) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
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
