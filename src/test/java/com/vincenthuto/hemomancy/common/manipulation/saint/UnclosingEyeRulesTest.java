package com.vincenthuto.hemomancy.common.manipulation.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** D-1 spec 3.2: the Eye's charge, sweep, growth stages, ally rules and the beam's reach. */
class UnclosingEyeRulesTest {
	@Test
	void chargeBuysRangeFromTwelveToTwentyFour() {
		assertEquals(0.0F, UnclosingEyeRules.charge(UnclosingEyeRules.MIN_CHARGE), 1e-6);
		assertEquals(1.0F, UnclosingEyeRules.charge(UnclosingEyeRules.FULL_CHARGE), 1e-6);
		assertEquals(1.0F, UnclosingEyeRules.charge(90), 1e-6);
		assertEquals(12.0D, UnclosingEyeRules.range(0.0F), 1e-9);
		assertEquals(24.0D, UnclosingEyeRules.range(1.0F), 1e-9);
		assertEquals(2.0F, UnclosingEyeRules.pulseDamage(0.0F), 1e-6);
		assertEquals(3.5F, UnclosingEyeRules.pulseDamage(1.0F), 1e-6);
	}

	@Test
	void theBeamTurnsAtMostFiveDegreesATickTheShortWayRound() {
		assertEquals(5.0D, UnclosingEyeRules.sweep(0.0D, 90.0D), 1e-9);
		assertEquals(2.0D, UnclosingEyeRules.sweep(0.0D, 2.0D), 1e-9);
		assertEquals(-5.0D, UnclosingEyeRules.sweep(0.0D, 270.0D), 1e-9, "the short way round");
		assertEquals(175.0D + 5.0D, UnclosingEyeRules.sweep(175.0D, -170.0D), 1e-9);
	}

	@Test
	void growthStagesAreHers() {
		assertEquals(0, UnclosingEyeRules.stage(2));
		assertEquals(1, UnclosingEyeRules.stage(3));
		assertEquals(2, UnclosingEyeRules.stage(6));
		assertEquals(3, UnclosingEyeRules.stage(8));
		assertTrue(UnclosingEyeRules.knots(5, 6));
		assertFalse(UnclosingEyeRules.knots(6, 7));
	}

	@Test
	void searingShedsAndAWholeAllyGrowsAtHalfRate() {
		assertEquals(UnclosingEyeRules.Step.SHED, UnclosingEyeRules.step(true, false, 0));
		assertEquals(UnclosingEyeRules.Step.GROW, UnclosingEyeRules.step(false, true, 0));
		assertEquals(UnclosingEyeRules.Step.HOLD, UnclosingEyeRules.step(false, true, 1));
		assertEquals(UnclosingEyeRules.Step.GROW, UnclosingEyeRules.step(false, false, 1));
		assertEquals(0, UnclosingEyeRules.grow(0, UnclosingEyeRules.Step.SHED));
		assertEquals(4, UnclosingEyeRules.grow(3, UnclosingEyeRules.Step.GROW));
	}

	@Test
	void theBurstSparesAnAllyItsLastHealthAndBossesTakeLess() {
		assertEquals(8.0F, UnclosingEyeRules.burstDamage(false, false), 1e-6);
		assertEquals(5.0F, UnclosingEyeRules.burstDamage(true, false), 1e-6);
		assertEquals(4.0F, UnclosingEyeRules.allyBurst(20.0F), 1e-6);
		assertEquals(2.5F, UnclosingEyeRules.allyBurst(3.5F), 1e-6);
		assertEquals(0.0F, UnclosingEyeRules.allyBurst(1.0F), 1e-6);
	}

	@Test
	void theBeamIsNarrowButABodysBreadthTouchesIt() {
		assertTrue(UnclosingEyeRules.inBeam(10.0D, 0.5D, 12.0D, 0.3D));
		assertFalse(UnclosingEyeRules.inBeam(10.0D, 1.5D, 12.0D, 0.3D));
		assertFalse(UnclosingEyeRules.inBeam(-1.0D, 0.0D, 12.0D, 0.3D), "nothing behind the chest");
		assertFalse(UnclosingEyeRules.inBeam(13.0D, 0.0D, 12.0D, 0.3D), "nothing past the reach");
	}
}
