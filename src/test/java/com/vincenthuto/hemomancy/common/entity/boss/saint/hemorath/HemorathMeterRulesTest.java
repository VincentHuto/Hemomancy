package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathMeterRulesTest {
	private static final float M = HemorathMeterRules.BASE_MAX;

	@Test
	void theLeakGrowsWithTheSquareOfHowFullHeIs() {
		assertEquals(1.2F, HemorathMeterRules.leak(M, 0.0F, false, false, false, false), 1.0E-4F);
		assertEquals(2.67F, HemorathMeterRules.leak(M, 0.35F, false, false, false, false), 0.01F);
		assertEquals(4.2F, HemorathMeterRules.leak(M, 0.5F, false, false, false, false), 1.0E-3F);
		assertEquals(7.08F, HemorathMeterRules.leak(M, 0.7F, false, false, false, false), 0.01F);
		assertEquals(9.87F, HemorathMeterRules.leak(M, 0.85F, false, false, false, false), 0.01F);
		assertEquals(13.2F, HemorathMeterRules.leak(M, 1.0F, false, false, false, false), 1.0E-3F);
	}

	@Test
	void phaseTwoSearingDigestingAndTheFreshStartChangeTheLeak() {
		float base = HemorathMeterRules.leak(M, 0.5F, false, false, false, false);
		assertEquals(base * 1.3F, HemorathMeterRules.leak(M, 0.5F, true, false, false, false), 1.0E-3F);
		assertEquals(base * 0.5F, HemorathMeterRules.leak(M, 0.5F, false, true, false, false), 1.0E-3F);
		assertEquals(0.0F, HemorathMeterRules.leak(M, 0.5F, true, true, true, false));
		assertEquals(base * 0.5F, HemorathMeterRules.leak(M, 0.5F, false, false, false, true), 1.0E-3F);
		assertTrue(HemorathMeterRules.fresh(true, HemorathMeterRules.FRESH_TICKS - 1));
		assertFalse(HemorathMeterRules.fresh(true, HemorathMeterRules.FRESH_TICKS));
		assertFalse(HemorathMeterRules.fresh(false, 0));
	}

	@Test
	void theMaximumAndTheLeakScaleWithChallengers() {
		assertEquals(3000.0F, HemorathMeterRules.max(1));
		assertEquals(4500.0F, HemorathMeterRules.max(2));
		assertEquals(6000.0F, HemorathMeterRules.max(3));
		assertEquals(7500.0F, HemorathMeterRules.max(4));
		float solo = HemorathMeterRules.leak(HemorathMeterRules.max(1), 0.5F, false, false, false, false);
		float four = HemorathMeterRules.leak(HemorathMeterRules.max(4), 0.5F, false, false, false, false);
		assertEquals(2.5F, four / solo, 1.0E-4F);
	}

	@Test
	void statesThresholdsAndPhaseTwoIntake() {
		assertEquals(HemorathMeterRules.State.SUNKEN, HemorathMeterRules.state(0.34F));
		assertEquals(HemorathMeterRules.State.FILLING, HemorathMeterRules.state(0.35F));
		assertEquals(HemorathMeterRules.State.BRIMMING, HemorathMeterRules.state(0.7F));
		assertArrayEquals(new float[]{0.35F, 0.7F, 0.85F, 1.0F}, HemorathMeterRules.thresholds());
		assertEquals(0.70F, HemorathMeterRules.State.SUNKEN.damageTaken);
		assertEquals(1.30F, HemorathMeterRules.State.BRIMMING.damageTaken);
		assertEquals(135.0F, HemorathMeterRules.intake(100.0F, true), 1.0E-3F);
		assertEquals(100.0F, HemorathMeterRules.intake(100.0F, false));
		assertEquals(0.25F, HemorathMeterRules.wellShare(false));
		assertEquals(0.40F, HemorathMeterRules.wellShare(true));
	}

	@Test
	void overloadEmptiesInPhaseOneAndEndsAfter() {
		assertEquals(HemorathPhaseRules.Overload.EMPTIES, HemorathPhaseRules.overload(1));
		assertEquals(HemorathPhaseRules.Overload.ENDS, HemorathPhaseRules.overload(2));
	}
}
