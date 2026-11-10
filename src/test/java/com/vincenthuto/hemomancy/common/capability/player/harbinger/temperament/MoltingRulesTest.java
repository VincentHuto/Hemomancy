package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoltingRulesTest {
	@Test
	void aTenthOfTheTotalIsShed() {
		assertEquals(90, MoltingRules.pool(new float[] {50, 0, 30, 0, 20, 0, 0, 0}));
		assertEquals(8, MoltingRules.pool(new float[] {9.9f, 0, 0, 0, 0, 0, 0, 0}), "whole points only");
		assertEquals(24, MoltingRules.pool(new float[] {30, -3, 0, 0, 0, 0, 0, 0}), "a negative temper counts against");
		assertEquals(0, MoltingRules.pool(new float[] {-5, 0, 0, 0, 0, 0, 0, 0}));
	}

	@Test
	void theScreenStartsFromTheCurrentShape() {
		int[] start = MoltingRules.suggested(new float[] {50, 0, 30, 0, 20, 0, 0, 0}, 90);
		assertArrayEquals(new int[] {45, 0, 27, 0, 18, 0, 0, 0}, start);
		int[] odd = MoltingRules.suggested(new float[] {1, 1, 1, 0, 0, 0, 0, 0}, 2);
		assertEquals(2, odd[0] + odd[1] + odd[2], "leftover points are placed, not lost");
		assertTrue(MoltingRules.placed(odd, 2));
	}

	@Test
	void everyPointMustBePlaced() {
		assertTrue(MoltingRules.placed(new int[] {90, 0, 0, 0, 0, 0, 0, 0}, 90));
		assertFalse(MoltingRules.placed(new int[] {89, 0, 0, 0, 0, 0, 0, 0}, 90));
		assertFalse(MoltingRules.placed(new int[] {91, -1, 0, 0, 0, 0, 0, 0}, 90), "no negative placements");
		assertFalse(MoltingRules.placed(new int[] {90}, 90), "one value per temper");
		assertFalse(MoltingRules.placed(null, 0));
	}

	@Test
	void onlyAnApotheosInTheGardensWithAFullLoadAndAHardenedSkin() {
		assertEquals(MoltingRules.Refusal.NONE, MoltingRules.refusal(true, true, 100f, 1000L, 0L, 10));
		assertEquals(MoltingRules.Refusal.NOT_APOTHEOS, MoltingRules.refusal(false, true, 100f, 1000L, 0L, 10));
		assertEquals(MoltingRules.Refusal.OUTSIDE_GARDENS, MoltingRules.refusal(true, false, 100f, 1000L, 0L, 10));
		assertEquals(MoltingRules.Refusal.SPORES, MoltingRules.refusal(true, true, 99f, 1000L, 0L, 10));
		assertEquals(MoltingRules.Refusal.SOFT, MoltingRules.refusal(true, true, 100f, 1000L, 1001L, 10));
		assertEquals(MoltingRules.Refusal.NOTHING, MoltingRules.refusal(true, true, 100f, 1000L, 0L, 0));
		assertEquals(24_000L, MoltingRules.COOLDOWN_TICKS);
	}
}
