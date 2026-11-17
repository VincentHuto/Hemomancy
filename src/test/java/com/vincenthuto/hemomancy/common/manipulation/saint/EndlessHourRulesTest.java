package com.vincenthuto.hemomancy.common.manipulation.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** D-1 spec 3.4: the Hour's reach, stillness fill and drain, preservation, the caster's guard, set and Bow. */
class EndlessHourRulesTest {
	@Test
	void theHourReachesNineBlocksWithTheDarkFromSixAndHoldsOnlyWhilePlanted() {
		assertTrue(EndlessHourRules.inHour(9.0D));
		assertFalse(EndlessHourRules.inHour(9.1D));
		assertFalse(EndlessHourRules.inDark(5.9D));
		assertTrue(EndlessHourRules.inDark(6.0D) && EndlessHourRules.inDark(9.0D));
		assertTrue(EndlessHourRules.planted(1.5D));
		assertFalse(EndlessHourRules.planted(1.6D));
		assertEquals(0.0D, EndlessHourRules.seams(0), 1e-9);
		assertEquals(1.0D, EndlessHourRules.seams(20), 1e-9);
	}

	@Test
	void standingFillsActingAndRimeFillMoreAndTravelDrains() {
		assertEquals(1.2D, EndlessHourRules.step(0.05D, false, 0, false, false, false), 1e-9);
		assertEquals(1.8D, EndlessHourRules.step(0.05D, true, 0, false, false, false), 1e-9);
		assertEquals(2.6D, EndlessHourRules.step(0.05D, true, 2, false, false, false), 1e-9);
		assertEquals(1.2D * 1.4D, EndlessHourRules.step(0.05D, false, 0, true, false, false), 1e-9);
		assertEquals(-1.5D, EndlessHourRules.step(0.2D, true, 3, true, false, false), 1e-9, "a running body never fills");
		assertEquals(-2.0D, EndlessHourRules.step(0.2D, false, 0, false, true, false), 1e-9, "warmth drains half more");
		assertEquals(1.2D, EndlessHourRules.step(0.5D, false, 0, false, false, true), 1e-9, "a Disrupted body counts as standing");
	}

	@Test
	void preservationTakesAFullBodyAndABossOnlyPressedAtNinety() {
		assertEquals(100.0D, EndlessHourRules.still(99.0D, 5.0D, false), 1e-9);
		assertEquals(90.0D, EndlessHourRules.still(89.0D, 5.0D, true), 1e-9);
		assertEquals(0.0D, EndlessHourRules.still(1.0D, -1.5D, false), 1e-9);
		assertTrue(EndlessHourRules.preserves(100.0D, false));
		assertFalse(EndlessHourRules.preserves(99.9D, false));
		assertTrue(EndlessHourRules.preserves(90.0D, true));
	}

	@Test
	void theCasterIsGuardedPlantedSetsSafeAndBowsSoft() {
		assertEquals(0.6D, EndlessHourRules.taken(true, false, false), 1e-9);
		assertEquals(0.3D, EndlessHourRules.taken(false, true, false), 1e-9);
		assertEquals(1.5D, EndlessHourRules.taken(false, false, true), 1e-9);
		assertTrue(EndlessHourRules.bows(6.0D, 20.0D));
		assertFalse(EndlessHourRules.bows(5.9D, 20.0D));
	}
}
