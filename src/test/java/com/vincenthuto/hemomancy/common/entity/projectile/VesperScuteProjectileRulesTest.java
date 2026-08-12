package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperScuteProjectileRulesTest {
	@Test
	void scutesExpireAtSixtyTicksOrFortyEightBlocks() {
		assertFalse(VesperScuteProjectileRules.shouldExpire(59, 48.0D * 48.0D));
		assertTrue(VesperScuteProjectileRules.shouldExpire(60, 0.0D));
		assertTrue(VesperScuteProjectileRules.shouldExpire(1, 48.0D * 48.0D + 0.01D));
	}

	@Test
	void scutesIgnoreTheirBossAndBossOwnedPuppets() {
		assertFalse(VesperScuteProjectileRules.mayHit(true, false, false, true, true));
		assertFalse(VesperScuteProjectileRules.mayHit(false, true, false, true, true));
		assertFalse(VesperScuteProjectileRules.mayHit(false, false, true, true, true));
		assertFalse(VesperScuteProjectileRules.mayHit(false, false, false, false, true));
		assertFalse(VesperScuteProjectileRules.mayHit(false, false, false, true, false));
		assertTrue(VesperScuteProjectileRules.mayHit(false, false, false, true, true));
	}
}
