package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbhorrentGazeRulesTest {
	@Test
	void theGazeFindsOnlyAPossessingWillInSight() {
		assertTrue(AbhorrentGazeRules.gazes(true, false, 9 * 9, true));
		assertFalse(AbhorrentGazeRules.gazes(false, false, 4, true), "an ordinary player is not drained");
		assertFalse(AbhorrentGazeRules.gazes(true, false, 11 * 11, true), "beyond ten blocks");
		assertFalse(AbhorrentGazeRules.gazes(true, false, 4, false), "out of sight");
		assertFalse(AbhorrentGazeRules.gazes(true, true, 4, true), "a creative or spectator possessor is not drained");
	}

	@Test
	void aGrabBreaksHarderThanAnEmptiedMind() {
		assertEquals(12000, AbhorrentGazeRules.GRAB_MIND_BROKEN_TICKS);
		assertTrue(AbhorrentGazeRules.GRAB_MIND_BROKEN_TICKS > PossessionRules.MIND_BROKEN_TICKS);
		assertEquals(20, AbhorrentGazeRules.GAZE_TICKS);
		assertEquals(3, PossessionRules.ADIPOCERE_BONUS);
	}

	@Test
	void aGrabRespectsTheTotemLikeAnyBreak() {
		assertEquals(AbhorrentGazeRules.Grab.TOTEM, AbhorrentGazeRules.grab(true));
		assertEquals(AbhorrentGazeRules.Grab.EJECT, AbhorrentGazeRules.grab(false));
	}
}
