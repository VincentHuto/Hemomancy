package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPuppetLeashRules.Order;
import static org.junit.jupiter.api.Assertions.*;

final class VesperPuppetLeashRulesTest {
	@Test
	void puppetWhoseConductorIsGoneUnspoolsAfterAGrace() {
		assertEquals(Order.HOLD, VesperPuppetLeashRules.order(false, VesperPuppetLeashRules.ORPHAN_GRACE_TICKS - 1, 0.0D));
		assertEquals(Order.UNSPOOL, VesperPuppetLeashRules.order(false, VesperPuppetLeashRules.ORPHAN_GRACE_TICKS, 0.0D));
	}

	@Test
	void puppetStrayingPastTheArenaTurnsBack() {
		assertEquals(Order.HOLD, VesperPuppetLeashRules.order(true, 0, 10.0D));
		assertEquals(Order.RETURN, VesperPuppetLeashRules.order(true, 0, VesperPuppetLeashRules.LEASH_RADIUS + 1.0D));
		assertTrue(VesperPuppetLeashRules.LEASH_RADIUS > VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT);
	}

	@Test
	void ordealOwnerIsTheMarkWhileInsideTheArena() {
		assertTrue(VesperPuppetLeashRules.prefersOwner(true, 10.0D));
		assertFalse(VesperPuppetLeashRules.prefersOwner(false, 10.0D));
		assertFalse(VesperPuppetLeashRules.prefersOwner(true, VesperPuppetLeashRules.LEASH_RADIUS + 1.0D));
	}
}
