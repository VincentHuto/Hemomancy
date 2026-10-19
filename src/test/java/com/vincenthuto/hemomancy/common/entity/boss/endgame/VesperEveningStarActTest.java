package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperEveningStarActTest {
	private static final float MAX = 600.0F;

	@Test
	void actBoundariesSplitTheHealthBarInThirds() {
		assertEquals(VesperEveningStarAct.LOCKED, VesperEveningStarAct.of(MAX, MAX));
		assertEquals(VesperEveningStarAct.LOCKED, VesperEveningStarAct.of(MAX * 2.0F / 3.0F + 1.0F, MAX));
		assertEquals(VesperEveningStarAct.OPEN, VesperEveningStarAct.of(MAX * 2.0F / 3.0F, MAX));
		assertEquals(VesperEveningStarAct.OPEN, VesperEveningStarAct.of(MAX / 3.0F + 1.0F, MAX));
		assertEquals(VesperEveningStarAct.SICKLES, VesperEveningStarAct.of(MAX / 3.0F, MAX));
		assertEquals(VesperEveningStarAct.SICKLES, VesperEveningStarAct.of(1.0F, MAX));
	}

	@Test
	void lockedLocksTheWeaponToItsStance() {
		assertTrue(VesperEveningStarAct.LOCKED.locksWeaponToStance());
		assertFalse(VesperEveningStarAct.OPEN.locksWeaponToStance());
	}

	@Test
	void onlyTheOpenActCastsAnySchool() {
		assertFalse(VesperEveningStarAct.LOCKED.castsAnySchool());
		assertTrue(VesperEveningStarAct.OPEN.castsAnySchool());
		assertFalse(VesperEveningStarAct.SICKLES.castsAnySchool());
	}

	@Test
	void rageBeginsAtOneThird() {
		assertTrue(VesperRageCombatRules.isRageThreshold(MAX / 3.0F, MAX));
		assertFalse(VesperRageCombatRules.isRageThreshold(MAX / 3.0F + 1.0F, MAX));
	}

	@Test
	void committedStrafeSideHoldsBetweenTwentyAndFortyTicks() {
		for (long seed = 0; seed < 50; seed++) {
			int hold = VesperEveningStarAct.strafeHoldTicks(net.minecraft.util.RandomSource.create(seed));
			assertTrue(hold >= 20 && hold <= 40, "hold " + hold);
		}
	}
}
