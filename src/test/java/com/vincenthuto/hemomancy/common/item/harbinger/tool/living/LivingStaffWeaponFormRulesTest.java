package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

public final class LivingStaffWeaponFormRulesTest {
	private LivingStaffWeaponFormRulesTest() {
	}

	@Test
	void allChecksPass() {
		hotSwapCostStartsAtTwoHundredFiftyAndDropsByFiftyPerLevel();
		theFifthRingCutsTheSwapAndLowersItsFloor();
		livingWeaponFormNamesAreRecognized();
		onlyTheAxeAndPickaxeDig();
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

	private static void theFifthRingCutsTheSwapAndLowersItsFloor() {
		assertEquals("rings below five change nothing", 200.0D, LivingStaffWeaponFormRules.hotSwapCost(1, 4));
		assertEquals("ring five takes 25 mL off", 175.0D, LivingStaffWeaponFormRules.hotSwapCost(1, 5));
		assertEquals("ring five lowers the floor to 25 mL", 25.0D, LivingStaffWeaponFormRules.hotSwapCost(4, 5));
		assertEquals("without ring five the floor stays 50 mL", 50.0D, LivingStaffWeaponFormRules.hotSwapCost(4, 0));
	}

	private static void onlyTheAxeAndPickaxeDig() {
		assertTrue("axe digs", LivingStaffWeaponFormRules.isDiggingForm("conjure_axe"));
		assertTrue("pickaxe digs", LivingStaffWeaponFormRules.isDiggingForm("conjure_pickaxe"));
		assertFalse("blade does not dig", LivingStaffWeaponFormRules.isDiggingForm("conjure_blade"));
	}

	private static void livingWeaponFormNamesAreRecognized() {
		assertTrue("pickaxe form recognized (Track 8, the ninth form)",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_pickaxe"));
		assertTrue("sickle form recognized",
				LivingStaffWeaponFormRules.isStaffWeaponFormManip("conjure_sickle"));
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
