package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantLastBroodTest {
	private static final double STEADY = MycophantLastBroodRules.PULL_PER_TICK * MycophantLastBrood.PULL_DAMPING
			/ (1.0D - MycophantLastBrood.PULL_DAMPING);

	@Test
	void thePullSettlesAtASteadySpeedTowardTheSac() {
		double vx = 0.0D, vz = 0.0D;
		for (int i = 0; i < 200; i++) {
			double[] next = MycophantLastBrood.pullStep(vx, vz, 3.0D, -4.0D);
			vx = next[0];
			vz = next[1];
			assertTrue(Math.hypot(vx, vz) <= STEADY + 1.0E-9, "never faster than the steady pull");
		}
		assertEquals(STEADY, Math.hypot(vx, vz), 1.0E-6);
		assertEquals(0.6D, vx / Math.hypot(vx, vz), 1.0E-6);
		assertEquals(-0.8D, vz / Math.hypot(vx, vz), 1.0E-6);
		assertTrue(STEADY > 0.15D && STEADY < 0.22D, "about sprint pace, so sprinting away only just holds ground: " + STEADY);
	}

	@Test
	void anOlderPushDiesAwayInsteadOfAddingUp() {
		double vx = 2.0D, vz = 0.0D;
		for (int i = 0; i < 30; i++) {
			double[] next = MycophantLastBrood.pullStep(vx, vz, -1.0D, 0.0D);
			vx = next[0];
			vz = next[1];
		}
		assertEquals(-STEADY, vx, 1.0E-3);
		double[] still = MycophantLastBrood.pullStep(0.3D, 0.0D, 0.0D, 0.0D);
		assertEquals(0.3D * MycophantLastBrood.PULL_DAMPING, still[0], 1.0E-9);
	}

	@Test
	void reachedMeansAtTheSacAcrossTheFloor() {
		assertTrue(MycophantLastBrood.reached(0.8D, 0.55D, 0.6D));
		assertFalse(MycophantLastBrood.reached(1.0D, 0.55D, 1.0D));
		assertFalse(MycophantLastBrood.reached(0.2D, 4.0D, 0.0D), "not from above her");
	}
}
