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
		assertEquals(1.0F, MycophantClaimRules.meterPerTick(1) * 160.0F, 1.0E-5F);
		assertTrue(MycophantClaimRules.meterPerTick(2) > MycophantClaimRules.meterPerTick(1));
		assertEquals(5, MycophantClaimRules.hitsToBreak(1));
		assertEquals(6, MycophantClaimRules.hitsToBreak(2));
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
	void theDrownClampCoversTheWholeSecondAfterAMeterRelease() {
		assertTrue(MycophantClaimRules.drownClampActive(100, 100));
		assertTrue(MycophantClaimRules.drownClampActive(119, 100));
		assertFalse(MycophantClaimRules.drownClampActive(120, 100));
		assertFalse(MycophantClaimRules.drownClampActive(100, -1));
	}
}
