package com.vincenthuto.hemomancy.common.manipulation.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** D-1 spec 3.3: the field's growth, colonisation and its stages, Kindling and the Ignition. */
class BloomOfRotRulesTest {
	@Test
	void theFieldGrowsFromThreeToEightOverFortyTicks() {
		assertEquals(3.0D, BloomOfRotRules.radius(0), 1e-9);
		assertEquals(8.0D, BloomOfRotRules.radius(40), 1e-9);
		assertEquals(8.0D, BloomOfRotRules.radius(200), 1e-9);
		assertTrue(BloomOfRotRules.radius(20) > 3.0D && BloomOfRotRules.radius(20) < 8.0D);
		assertTrue(BloomOfRotRules.onField(8.0D, 2.0D, 8.0D));
		assertFalse(BloomOfRotRules.onField(8.1D, 0.0D, 8.0D));
		assertFalse(BloomOfRotRules.onField(1.0D, 2.5D, 8.0D), "a body above the floor's reach is off the field");
	}

	@Test
	void stillnessRimeAndPressureEachAddOneColonisation() {
		assertEquals(1, BloomOfRotRules.gain(false, false, false));
		assertEquals(2, BloomOfRotRules.gain(true, false, false));
		assertEquals(4, BloomOfRotRules.gain(true, true, true));
		assertEquals(12, BloomOfRotRules.colonise(11, 4), "capped at 12 however many fields lay it");
	}

	@Test
	void theStagesAreTheirsAtASmallerScale() {
		assertEquals(0, BloomOfRotRules.stage(3));
		assertEquals(1, BloomOfRotRules.stage(4));
		assertEquals(2, BloomOfRotRules.stage(8));
		assertEquals(3, BloomOfRotRules.stage(12));
	}

	@Test
	void kindlingRisesWithDamageAndFireFeedsItTwoAndAHalfTimes() {
		assertEquals(1.0D, BloomOfRotRules.kindlingFromDamage(3.0D, false), 1e-9);
		assertEquals(2.5D, BloomOfRotRules.kindlingFromDamage(3.0D, true), 1e-9);
		assertEquals(100.0D, BloomOfRotRules.kindle(99.0D, 5.0D), 1e-9);
		assertFalse(BloomOfRotRules.canIgnite(39.9D));
		assertTrue(BloomOfRotRules.canIgnite(40.0D));
		assertTrue(BloomOfRotRules.ignitesItself(100.0D));
	}

	@Test
	void theIgnitionBurnsHarderForEachColonisationAndAtFullKindling() {
		assertEquals(2.0F, BloomOfRotRules.ignitionDamage(0, 50.0D), 1e-6);
		assertEquals(11.0F, BloomOfRotRules.ignitionDamage(12, 50.0D), 1e-6);
		assertEquals(13.75F, BloomOfRotRules.ignitionDamage(12, 100.0D), 1e-6);
		assertEquals(0.0D, BloomOfRotRules.fireReach(0, 8.0D), 1e-9);
		assertEquals(4.0D, BloomOfRotRules.fireReach(6, 8.0D), 1e-9);
		assertEquals(8.0D, BloomOfRotRules.fireReach(20, 8.0D), 1e-9);
	}
}
