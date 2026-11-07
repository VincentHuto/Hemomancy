package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Kindling (design section 4, resolutions B-5 and D-6). */
final class PutricielKindlingRulesTest {
	@Test
	void theMaximumScalesByTheKitsOneRule() {
		assertEquals(100.0F, PutricielKindlingRules.max(1), 1.0E-4F);
		assertEquals(150.0F, PutricielKindlingRules.max(2), 1.0E-4F);
		assertEquals(250.0F, PutricielKindlingRules.max(4), 1.0E-4F);
	}

	@Test
	void damageFillsOnePointPerFiveAndFlammeusTwoAndAHalfTimes() {
		assertEquals(2.0F, PutricielKindlingRules.fromDamage(10.0F, false, 1.0F, PutricielStage.ROT), 1.0E-4F);
		assertEquals(5.0F, PutricielKindlingRules.fromDamage(10.0F, true, 1.0F, PutricielStage.ROT), 1.0E-4F);
		assertEquals(3.0F, PutricielKindlingRules.fromDamage(10.0F, false, PutricielOpenings.kindlingMultiplier(
				PutricielOpenings.SPENT_SACS_ID), PutricielStage.ROT), 1.0E-4F);
		assertEquals(4.0F, PutricielKindlingRules.fromDamage(10.0F, false, PutricielOpenings.kindlingMultiplier(
				PutricielOpenings.EMBERING_ID), PutricielStage.EMBERING), 1.0E-4F);
	}

	@Test
	void theFireNeverStokesItself() {
		assertEquals(0.0F, PutricielKindlingRules.fromDamage(40.0F, true, 2.0F, PutricielStage.BURNING));
		assertEquals(0.0F, PutricielKindlingRules.fromDamage(40.0F, true, 2.0F, PutricielStage.IGNITING));
	}

	@Test
	void creepPerPhaseKeepsItsTimingForAnyGroup() {
		// Rot I fills from creep alone in 1200 ticks (a minute), whatever the group: the creep is a fraction of the max
		for (int n = 1; n <= 4; n++) {
			float perTick = PutricielKindlingRules.creep(PutricielPhase.ROT_I, n);
			assertEquals(1200.0F, PutricielKindlingRules.max(n) / perTick, 1.0F);
		}
		assertEquals(1.0F / 20.0F, PutricielKindlingRules.creep(PutricielPhase.ROT_II, 1), 1.0E-6F);
		assertEquals(1.0F / 15.0F, PutricielKindlingRules.creep(PutricielPhase.ROT_III, 1), 1.0E-6F);
		assertEquals(0.0F, PutricielKindlingRules.creep(PutricielPhase.INTERVAL, 1));
	}

	@Test
	void theDriftHoldsBurnsAndPauses() {
		assertEquals(0.0F, PutricielKindlingRules.drift(PutricielStage.IGNITING, PutricielPhase.INTERVAL, 1, false, false));
		assertEquals(0.0F, PutricielKindlingRules.drift(PutricielStage.EMBERING, PutricielPhase.INTERVAL, 1, false, false));
		assertEquals(-1.0F / 3.0F, PutricielKindlingRules.drift(PutricielStage.BURNING, PutricielPhase.INTERVAL, 1, false, false), 1.0E-6F);
		assertEquals(0.0F, PutricielKindlingRules.drift(PutricielStage.BURNING, PutricielPhase.INTERVAL, 1, true, false));
		float communion = PutricielKindlingRules.drift(PutricielStage.ROT, PutricielPhase.ROT_II, 1, false, true);
		assertEquals(1.0F / 20.0F - 0.5F, communion, 1.0E-6F);
		// the burn-down takes 300 ticks from full for any group
		for (int n = 1; n <= 4; n++) assertEquals(300.0F, PutricielKindlingRules.max(n) / PutricielKindlingRules.burn(n), 0.5F);
	}

	@Test
	void theTwoOfferingsArmAt66And33() {
		assertEquals(0, PutricielKindlingRules.armed(0.9F));
		assertEquals(1, PutricielKindlingRules.armed(0.66F));
		assertEquals(1, PutricielKindlingRules.armed(0.4F));
		assertEquals(2, PutricielKindlingRules.armed(0.33F));
		assertEquals(2, PutricielKindlingRules.armed(0.0F));
	}

	@Test
	void fullAndTheAbsorptionCap() {
		assertTrue(PutricielKindlingRules.full(100.0F, 100.0F));
		assertFalse(PutricielKindlingRules.full(99.0F, 100.0F));
		assertEquals(1.5F, PutricielKindlingRules.absorb(0.0F), 1.0E-6F);
		assertEquals(0.5F, PutricielKindlingRules.absorb(39.5F), 1.0E-6F);
		assertEquals(0.0F, PutricielKindlingRules.absorb(40.0F));
		assertEquals(25.0F, PutricielKindlingRules.reposeHeadStart(1), 1.0E-4F);
	}
}
