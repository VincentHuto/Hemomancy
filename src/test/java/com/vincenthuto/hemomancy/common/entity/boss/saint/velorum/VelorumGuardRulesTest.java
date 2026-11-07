package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumGuardRulesTest {
	@Test
	void thirtyPercentOutsideOpeningsWholeInsideAndCastsInTheOverheld() {
		assertEquals(0.3F, VelorumGuardRules.factor(false, false, false));
		assertEquals(1.0F, VelorumGuardRules.factor(true, false, false));
		assertEquals(1.0F, VelorumGuardRules.factor(false, true, false));
		assertEquals(1.25F, VelorumGuardRules.factor(true, false, true));
		assertEquals(0.3F, VelorumGuardRules.factor(false, false, true));
	}

	@Test
	void theFloorsHoldOutsideOpenings() {
		assertEquals(0.60F, VelorumGuardRules.floor(0.9F, 0, false));
		assertEquals(0.60F, VelorumGuardRules.floor(0.60F, 0, false));
		assertEquals(0.25F, VelorumGuardRules.floor(0.60F, 1, false));
		assertEquals(0.25F, VelorumGuardRules.floor(0.4F, 1, false));
		assertEquals(0.10F, VelorumGuardRules.floor(0.2F, 2, false));
		// under the last tenth nothing outside an opening moves him
		assertEquals(0.08F, VelorumGuardRules.floor(0.08F, 2, false));
		assertEquals(0.0F, VelorumGuardRules.floor(0.08F, 2, true));
		assertEquals(0.0F, VelorumGuardRules.floor(0.9F, 0, true));
	}

	@Test
	void onlyAnOpeningPassesAPhaseFloor() {
		assertEquals(0, VelorumGuardRules.passedBelow(0.6F, 0));
		assertEquals(1, VelorumGuardRules.passedBelow(0.5F, 0));
		assertEquals(2, VelorumGuardRules.passedBelow(0.2F, 0));
		assertEquals(2, VelorumGuardRules.passedBelow(0.01F, 2));
		assertEquals(1, VelorumGuardRules.phase(0));
		assertEquals(2, VelorumGuardRules.phase(1));
		assertEquals(3, VelorumGuardRules.phase(2));
		assertEquals(3, VelorumGuardRules.phase(5));
	}

	@Test
	void floorsAreNamed() {
		assertEquals(0, VelorumGuardRules.phaseFloor(0.60F));
		assertEquals(1, VelorumGuardRules.phaseFloor(0.25F));
		assertEquals(-1, VelorumGuardRules.phaseFloor(0.10F));
		assertTrue(VelorumGuardRules.lastTenth(0.1F));
		assertFalse(VelorumGuardRules.lastTenth(0.11F));
	}
}
