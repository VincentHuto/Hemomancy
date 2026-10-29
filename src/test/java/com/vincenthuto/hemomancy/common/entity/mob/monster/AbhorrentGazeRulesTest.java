package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbhorrentGazeRulesTest {
	@Test
	void theGazeFindsOnlyAPossessingWillInSight() {
		assertTrue(AbhorrentGazeRules.gazes(true, 9 * 9, true));
		assertFalse(AbhorrentGazeRules.gazes(false, 4, true), "an ordinary player is not drained");
		assertFalse(AbhorrentGazeRules.gazes(true, 11 * 11, true), "beyond ten blocks");
		assertFalse(AbhorrentGazeRules.gazes(true, 4, false), "out of sight");
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
