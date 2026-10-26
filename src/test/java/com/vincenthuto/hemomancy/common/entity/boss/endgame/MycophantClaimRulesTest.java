package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantClaimRulesTest {
	@Test
	void filledMeterDamageNeverLethal() {
		assertEquals(2.0F, MycophantClaimRules.clampNonlethal(6.0F, 14.0F), 1.0E-5F);
		assertEquals(0.0F, MycophantClaimRules.clampNonlethal(3.0F, 10.0F), 1.0E-5F);
		assertEquals(10.0F, MycophantClaimRules.clampNonlethal(20.0F, 10.0F), 1.0E-5F);
		for (float health = 4.5F; health < 40.0F; health += 1.3F) {
			for (float damage = 0.0F; damage < 50.0F; damage += 2.7F) {
				assertTrue(health - MycophantClaimRules.clampNonlethal(health, damage) >= 4.0F - 1.0E-5F);
			}
		}
	}

	@Test
	void meterRates() {
		// the embrace fills over 5 s, the fusion over 4 s
		assertEquals(1.0F, MycophantClaimRules.meterPerTick(1) * 100.0F, 1.0E-5F);
		assertEquals(1.0F, MycophantClaimRules.meterPerTick(2) * 80.0F, 1.0E-5F);
		assertTrue(MycophantClaimRules.meterPerTick(2) > MycophantClaimRules.meterPerTick(1));
		assertEquals(4, MycophantClaimRules.hitsToBreak(1));
		assertEquals(5, MycophantClaimRules.hitsToBreak(2));
		assertEquals(10.0F, MycophantClaimRules.filledMeterDamage(1));
		assertEquals(14.0F, MycophantClaimRules.filledMeterDamage(2));
		assertEquals(0.10F, MycophantClaimRules.bloodDrainFraction(1), 1.0E-6F);
		assertEquals(0.15F, MycophantClaimRules.bloodDrainFraction(2), 1.0E-6F);
	}

	@Test
	void fusionBonus() {
		assertEquals(57.6F, MycophantClaimRules.fusionBreakBonus(720.0F), 1.0E-4F);
	}

	@Test
	void guard() {
		assertEquals(2.0F, MycophantClaimRules.guardedDamage(10.0F, 30.0), 1.0E-5F);
		assertEquals(2.0F, MycophantClaimRules.guardedDamage(10.0F, 70.0), 1.0E-5F);
		assertEquals(10.0F, MycophantClaimRules.guardedDamage(10.0F, 120.0), 1.0E-5F);
	}

	@Test
	void castBreaks() {
		assertTrue(MycophantClaimRules.castBreaks(true, false, false));
		assertTrue(MycophantClaimRules.castBreaks(true, true, false));
		assertTrue(MycophantClaimRules.castBreaks(false, true, false));
		assertFalse(MycophantClaimRules.castBreaks(true, true, true));
		assertFalse(MycophantClaimRules.castBreaks(false, false, false));
		assertEquals(20, MycophantClaimRules.castWindowTicks());
	}

	@Test
	void siphon() {
		assertEquals(10.0, MycophantClaimRules.siphonRange());
		assertEquals(0.06, MycophantClaimRules.siphonReelPerTick(), 1.0E-9);
		assertEquals(2.0F, MycophantClaimRules.siphonDrainPerSecond(1));
		assertEquals(3.0F, MycophantClaimRules.siphonDrainPerSecond(2));
	}

	@Test
	void aCaughtPlayerIsDrawnInBeforeTheServerSlackTightens() {
		// the client starts pulling a few ticks late (the catch has to reach it) and still lands inside the steady slack
		double gap = 3.4D;
		for (int tick = 3; tick < MycophantClaimRules.holdPullTicks(); tick++) gap *= 1.0D - MycophantClaimRules.holdPullPerTick();
		assertTrue(gap < MycophantClaimRules.holdSlack(MycophantClaimRules.holdPullTicks()) * 0.1D);
		// while drawn in, the widest catch never earns a server snap
		assertTrue(MycophantClaimRules.holdSlack(0) > 3.4D);
		assertEquals(1.0D, MycophantClaimRules.holdSlack(MycophantClaimRules.holdPullTicks()), 1.0E-9);
		assertEquals(1.0D, MycophantClaimRules.holdSlack(200), 1.0E-9);
		// the draw-in is a small share of even the shortest (fusion) hold
		assertTrue(MycophantClaimRules.holdPullTicks() * 4 < 1.0F / MycophantClaimRules.meterPerTick(2));
	}

	@Test
	void theDrownClampCoversTheWholeSecondAfterAMeterRelease() {
		assertTrue(MycophantClaimRules.drownClampActive(100, 100));
		assertTrue(MycophantClaimRules.drownClampActive(119, 100));
		assertFalse(MycophantClaimRules.drownClampActive(120, 100));
		assertFalse(MycophantClaimRules.drownClampActive(100, -1));
	}
}
