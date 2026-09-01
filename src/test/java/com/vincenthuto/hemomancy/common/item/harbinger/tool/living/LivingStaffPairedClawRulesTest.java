package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivingStaffPairedClawRulesTest {
	@Test
	void emptyOffhandReceivesASecondClaw() {
		assertTrue(LivingStaffWeaponFormRules.shouldEquipPairedOffhandClaw(
				LivingStaffWeaponFormRules.CONJURE_CLAWS, true));
	}

	@Test
	void occupiedOffhandIsNeverReplaced() {
		assertFalse(LivingStaffWeaponFormRules.shouldEquipPairedOffhandClaw(
				LivingStaffWeaponFormRules.CONJURE_CLAWS, false));
	}

	@Test
	void otherLivingWeaponFormsRemainSingleHanded() {
		assertFalse(LivingStaffWeaponFormRules.shouldEquipPairedOffhandClaw(
				LivingStaffWeaponFormRules.CONJURE_BLADE, true));
	}

	@Test
	void onlyTwoClawFormsCountAsTheSanctionedPair() {
		assertTrue(LivingStaffWeaponFormRules.isPairedClawForm(
				LivingStaffWeaponFormRules.CONJURE_CLAWS,
				LivingStaffWeaponFormRules.CONJURE_CLAWS));
		assertFalse(LivingStaffWeaponFormRules.isPairedClawForm(
				LivingStaffWeaponFormRules.CONJURE_CLAWS,
				LivingStaffWeaponFormRules.CONJURE_BLADE));
	}
}
