package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantHazardRulesTest {
	@Test
	void hazardsExpireWithoutALivingOwner() {
		assertTrue(MycophantHazardRules.shouldExpire(false, 0, 999));
		assertTrue(MycophantHazardRules.shouldExpire(true, 160, 160));
		assertFalse(MycophantHazardRules.shouldExpire(true, 10, 160));
		// a binding tendril has no life of its own; only its owner ends it
		assertFalse(MycophantHazardRules.shouldExpire(true, 5000, Integer.MAX_VALUE));
		assertTrue(MycophantHazardRules.shouldExpire(false, 5000, Integer.MAX_VALUE));
	}

	@Test
	void aNetComesDownAfterItsDelay() {
		assertFalse(MycophantHazardRules.landed(15, 16));
		assertTrue(MycophantHazardRules.landed(16, 16));
	}

	@Test
	void standingInAFloorHazardNeedsFeetNearItsPlane() {
		assertTrue(MycophantHazardRules.standingIn(1.0, 0.0, 1.0, 1.5));
		assertFalse(MycophantHazardRules.standingIn(1.2, 0.0, 1.2, 1.5));
		assertFalse(MycophantHazardRules.standingIn(0.0, 2.5, 0.0, 4.0));
		assertFalse(MycophantHazardRules.standingIn(0.0, -0.8, 0.0, 4.0));
	}

	@Test
	void aGlobsFlightLandsWhereTheLobAimed() {
		for (int flight : new int[] { 14, 20, 31 }) {
			double[] velocity = MycophantStrikeRules.lobVelocity(9.0, -1.0, -4.0, flight, MycophantHazardRules.GLOB_GRAVITY);
			double[] position = { 0.0, 0.0, 0.0 };
			for (int tick = 0; tick < flight; tick++) MycophantHazardRules.step(position, velocity, MycophantHazardRules.GLOB_GRAVITY);
			assertEquals(9.0, position[0], 1.0E-9);
			assertEquals(-1.0, position[1], 1.0E-9);
			assertEquals(-4.0, position[2], 1.0E-9);
		}
	}

	@Test
	void aTendrilBlowMustBeAimedAlongItsLength() {
		double[] root = { 0.0, 1.6, 0.0 };
		double[] anchor = { 8.0, 0.0, 0.0 };
		double[] down = { 0.0, -1.0, 0.0 };
		// looking straight down onto its middle from just above
		assertEquals(0.0, MycophantHazardRules.aimDistance(new double[] { 4.0, 3.0, 0.0 }, down, 4.0, root, anchor), 1.0E-9);
		// looking down two blocks to one side of it
		assertEquals(2.0, MycophantHazardRules.aimDistance(new double[] { 4.0, 3.0, 2.0 }, down, 4.0, root, anchor), 1.0E-9);
		// aiming away from it: the nearest point is the attacker's own eye
		double away = MycophantHazardRules.aimDistance(new double[] { 4.0, 3.0, 1.0 }, new double[] { 0.0, 0.0, 1.0 }, 4.0, root, anchor);
		assertTrue(away > MycophantHazardRules.TENDRIL_HIT_SLACK);
	}
}
