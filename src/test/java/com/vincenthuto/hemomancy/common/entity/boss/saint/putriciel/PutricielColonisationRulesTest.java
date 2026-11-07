package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Colonisation (design section 5): radius and gain per phase, the thresholds, the flag word. */
final class PutricielColonisationRulesTest {
	@Test
	void radiusAndGainPerPhase() {
		assertEquals(7.0D, PutricielColonisationRules.radius(PutricielPhase.ROT_I));
		assertEquals(8.0D, PutricielColonisationRules.radius(PutricielPhase.ROT_II));
		assertEquals(9.0D, PutricielColonisationRules.radius(PutricielPhase.ROT_III));
		assertEquals(4.0D, PutricielColonisationRules.radius(PutricielPhase.INTERVAL), "the purging heat");
		assertEquals(1.0F, PutricielColonisationRules.gain(PutricielPhase.ROT_I));
		assertEquals(1.5F, PutricielColonisationRules.gain(PutricielPhase.ROT_II));
		assertEquals(2.0F, PutricielColonisationRules.gain(PutricielPhase.ROT_III));
		assertEquals(0.0F, PutricielColonisationRules.gain(PutricielPhase.INTERVAL), "the rot presence stops in the fire");
	}

	@Test
	void theFourThresholds() {
		assertEquals(0, PutricielColonisationRules.tier(4.5F));
		assertEquals(1, PutricielColonisationRules.tier(5.0F));
		assertEquals(2, PutricielColonisationRules.tier(10.0F));
		assertEquals(3, PutricielColonisationRules.tier(15.0F));
		assertEquals(4, PutricielColonisationRules.tier(20.0F));
		assertTrue(PutricielColonisationRules.overgrows(20.0F));
		assertFalse(PutricielColonisationRules.overgrows(19.5F));
		assertTrue(PutricielColonisationRules.OVERGROWN_FALL_TO < PutricielColonisationRules.FRUITING);
	}

	@Test
	void valuesClampToTwenty() {
		assertEquals(20.0F, PutricielColonisationRules.bounded(27.0F));
		assertEquals(0.0F, PutricielColonisationRules.bounded(-3.0F));
	}

	@Test
	void theFlagWordCarriesTheValueInHalvesAndItsStates() {
		for (float value = 0.0F; value <= 20.0F; value += 0.5F) {
			int flags = PutricielColonisationRules.flags(value, false, false);
			assertEquals(value, PutricielColonisationRules.value(flags), 1.0E-6F);
			assertEquals(value >= 15.0F, PutricielColonisationRules.fruitingFlag(flags));
			assertTrue((flags & ~0xFFFF) == 0, "Saint flags stay in bits 0..15");
		}
		int flags = PutricielColonisationRules.flags(12.0F, true, true);
		assertTrue(PutricielColonisationRules.overgrown(flags));
		assertTrue(PutricielColonisationRules.burning(flags));
		assertEquals(12.0F, PutricielColonisationRules.value(flags), 1.0E-6F);
	}
}
