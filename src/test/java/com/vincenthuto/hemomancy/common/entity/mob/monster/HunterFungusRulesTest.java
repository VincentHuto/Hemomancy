package com.vincenthuto.hemomancy.common.entity.mob.monster;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HunterFungusRulesTest {
	@Test
	void nearHuntersPounceFarOnesConvergeBusyOnesIgnore() {
		assertEquals(HunterFungusRules.Response.TARGET, HunterFungusRules.respond(false, 15 * 15));
		assertEquals(HunterFungusRules.Response.CONVERGE, HunterFungusRules.respond(false, 40 * 40));
		assertEquals(HunterFungusRules.Response.IGNORE, HunterFungusRules.respond(false, 65 * 65), "beyond 64 blocks");
		assertEquals(HunterFungusRules.Response.IGNORE, HunterFungusRules.respond(true, 4), "already hunting");
		assertEquals(600, HunterFungusRules.MEMORY_TICKS);
	}

	@Test
	void aConvergingHunterRepathsOnASteadyBeatNotEveryTick() {
		assertTrue(HunterFungusRules.shouldRepath(0));
		assertFalse(HunterFungusRules.shouldRepath(1), "a failed path must not be retried the next tick");
		assertFalse(HunterFungusRules.shouldRepath(19));
		assertTrue(HunterFungusRules.shouldRepath(20));
	}

	@Test
	void huntersNeverHuntTheOrganismsOwn() {
		assertTrue(HunterFungusRules.mayHunt(false, false));
		assertFalse(HunterFungusRules.mayHunt(true, false));
		assertFalse(HunterFungusRules.mayHunt(false, true));
	}
}
