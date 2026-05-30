package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;

public final class LivingStaffUtilitySelectionRulesTest {
	private LivingStaffUtilitySelectionRulesTest() {
	}

	public static void main(String[] args) {
		selectedUtilityMatchesByName();
		blankSelectionDoesNotMatch();
	}

	private static void selectedUtilityMatchesByName() {
		assertTrue("selected blood absorption activates staff absorption",
				LivingStaffUtilitySelectionRules.isSelectedUtility(
						ManipulationEquipHelper.BLOOD_ABSORPTION,
						ManipulationEquipHelper.BLOOD_ABSORPTION));
		assertTrue("selected blood projection activates staff projection",
				LivingStaffUtilitySelectionRules.isSelectedUtility(
						ManipulationEquipHelper.BLOOD_PROJECTION,
						ManipulationEquipHelper.BLOOD_PROJECTION));
		assertFalse("projection selection does not activate absorption",
				LivingStaffUtilitySelectionRules.isSelectedUtility(
						ManipulationEquipHelper.BLOOD_PROJECTION,
						ManipulationEquipHelper.BLOOD_ABSORPTION));
	}

	private static void blankSelectionDoesNotMatch() {
		assertFalse("null selection does not match",
				LivingStaffUtilitySelectionRules.isSelectedUtility(null,
						ManipulationEquipHelper.BLOOD_ABSORPTION));
		assertFalse("blank selection does not match",
				LivingStaffUtilitySelectionRules.isSelectedUtility("",
						ManipulationEquipHelper.BLOOD_ABSORPTION));
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
