package com.vincenthuto.hemomancy.common.world.ground;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop round two P8: the field of dark's numbers. The light taken sets its time (a dim light still gives a little),
 * a dry pull hangs a shorter field but never none, the time is capped, the radius runs from 3 to 8 blocks as the time
 * left falls, a snuff inside the field adds to it, and the fog thins to nothing across the soft edge.
 */
class DarkFieldRulesTest {
	@Test
	void theLightTakenSetsTheTime() {
		assertEquals(60, DarkFieldRules.charge(3), "one candle");
		assertEquals(240, DarkFieldRules.charge(12), "four candles");
		assertEquals(300, DarkFieldRules.charge(15), "a campfire");
		assertEquals(260, DarkFieldRules.charge(13), "a lit light run");
		assertEquals(DarkFieldRules.MIN_CHARGE, DarkFieldRules.charge(0), "a dark-lit run still gives the least");
		assertEquals(DarkFieldRules.MIN_CHARGE, DarkFieldRules.charge(2));
	}

	@Test
	void aDryPullHangsAShorterFieldNeverNone() {
		assertEquals(240, DarkFieldRules.fieldTicks(240, 1.0D));
		assertEquals(180, DarkFieldRules.fieldTicks(240, 0.75D), "a dry Sculpting pulse works at three quarters");
		assertEquals(60, DarkFieldRules.fieldTicks(240, 0.0D), "the dry floor keeps a quarter");
		assertTrue(DarkFieldRules.fieldTicks(1, 0.0D) >= 1, "anything taken hangs something");
		assertEquals(0, DarkFieldRules.fieldTicks(0, 1.0D), "nothing taken, no field");
		assertEquals(360, DarkFieldRules.fieldTicks(240, 1.5D), "the Staff's rate skills lengthen it");
	}

	@Test
	void theTimeIsCapped() {
		assertEquals(DarkFieldRules.MAX_TICKS, DarkFieldRules.extended(2_300L, 300));
		assertEquals(500L, DarkFieldRules.extended(200L, 300));
		assertEquals(300L, DarkFieldRules.extended(-5L, 300), "a lifted field starts from nothing");
	}

	@Test
	void theRadiusRunsFromThreeToEightAsTheTimeFalls() {
		assertEquals(3.0D, DarkFieldRules.radius(0L), 1.0E-9);
		assertEquals(3.6D, DarkFieldRules.radius(240L), 1.0E-9);
		assertEquals(8.0D, DarkFieldRules.radius(DarkFieldRules.MAX_TICKS), 1.0E-9);
		assertEquals(8.0D, DarkFieldRules.radius(100_000L), 1.0E-9);
		assertTrue(DarkFieldRules.radius(600L) > DarkFieldRules.radius(300L), "a longer field is wider");
	}

	@Test
	void aSnuffInsideAddsToTheFieldAndOneOutsideStartsAnother() {
		assertTrue(DarkFieldRules.joins(2.0D, 3.6D));
		assertTrue(DarkFieldRules.joins(3.6D, 3.6D), "the edge is inside");
		assertFalse(DarkFieldRules.joins(3.7D, 3.6D));
	}

	@Test
	void theFogThinsAcrossTheSoftEdge() {
		assertEquals(1.0F, DarkFieldRules.depth(0.0D, 5.0D), 1.0E-6F);
		assertEquals(1.0F, DarkFieldRules.depth(5.0D - DarkFieldRules.SOFT_EDGE, 5.0D), 1.0E-6F);
		assertEquals(0.0F, DarkFieldRules.depth(5.0D, 5.0D), 1.0E-6F);
		assertEquals(0.0F, DarkFieldRules.depth(9.0D, 5.0D), 1.0E-6F);
		float half = DarkFieldRules.depth(5.0D - DarkFieldRules.SOFT_EDGE / 2.0D, 5.0D);
		assertTrue(half > 0.3F && half < 0.7F, "half way across the edge the fog is about half: " + half);
		assertEquals(0.0F, DarkFieldRules.depth(1.0D, 0.0D), 1.0E-6F, "no field, no fog");
		assertEquals(0.0F, DarkFieldRules.depth(Double.NaN, 5.0D), 1.0E-6F);
	}
}
