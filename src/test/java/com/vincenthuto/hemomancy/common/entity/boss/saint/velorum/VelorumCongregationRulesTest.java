package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumCongregationRulesTest {
	@Test
	void wakingTakesFortyFedTicksWithAGraceThenAHalfSpeedBleed() {
		float fed = 0.0F;
		for (int i = 0; i < 20; i++) fed = VelorumCongregationRules.feed(fed, true, 0);
		assertEquals(20.0F, fed);
		for (int idle = 1; idle <= VelorumCongregationRules.GRACE_TICKS; idle++) fed = VelorumCongregationRules.feed(fed, false, idle);
		assertEquals(20.0F, fed, "the grace holds");
		fed = VelorumCongregationRules.feed(fed, false, VelorumCongregationRules.GRACE_TICKS + 1);
		assertEquals(19.5F, fed);
		assertEquals(0.0F, VelorumCongregationRules.feed(0.2F, false, 10));
		assertEquals(40, VelorumCongregationRules.wakeTicks(false));
		assertEquals(80, VelorumCongregationRules.wakeTicks(true));
		assertEquals(120.0F, VelorumCongregationRules.WAKE_TICKS * VelorumCongregationRules.WAKE_RATE);
		assertEquals(60.0F, VelorumCongregationRules.PIN_TICKS * VelorumCongregationRules.PIN_RATE);
	}

	@Test
	void theAwakeningDecidesTheSide() {
		assertEquals(SaintCompanion.Allegiance.CHALLENGERS, VelorumCongregationRules.allegiance("PROPER"));
		assertEquals(SaintCompanion.Allegiance.CHALLENGERS, VelorumCongregationRules.allegiance("NONE"));
		assertEquals(SaintCompanion.Allegiance.SAINT, VelorumCongregationRules.allegiance("FORCED"));
	}

	@Test
	void capsRiseWithChallengersAndARecallLocksOut() {
		assertEquals(4, VelorumCongregationRules.cap(1));
		assertEquals(7, VelorumCongregationRules.cap(4));
		assertTrue(VelorumCongregationRules.wakeable(VelorumCongregationRules.State.FROZEN, 0, 0));
		assertTrue(VelorumCongregationRules.wakeable(VelorumCongregationRules.State.STILLED, 300, 300));
		assertFalse(VelorumCongregationRules.wakeable(VelorumCongregationRules.State.STILLED, 299, 300));
		assertFalse(VelorumCongregationRules.wakeable(VelorumCongregationRules.State.PINNED, 1000, 0));
		assertFalse(VelorumCongregationRules.wakeable(VelorumCongregationRules.State.AWAKE, 1000, 0));
	}

	@Test
	void frozenAndStilledFiguresFeedHisVeinsAndNoOthers() {
		assertTrue(VelorumCongregationRules.feedsVeins(VelorumCongregationRules.State.FROZEN));
		assertTrue(VelorumCongregationRules.feedsVeins(VelorumCongregationRules.State.STILLED));
		assertFalse(VelorumCongregationRules.feedsVeins(VelorumCongregationRules.State.PINNED), "the hyphae shut them");
		assertFalse(VelorumCongregationRules.feedsVeins(VelorumCongregationRules.State.AWAKE));
		assertFalse(VelorumCongregationRules.feedsVeins(VelorumCongregationRules.State.HOME));
		assertEquals(4, VelorumCongregationRules.MAX_VEINS);
		assertEquals(6, VelorumCongregationRules.WARM_VEINS, "a woken figure's veins run warm for 6 ticks");
	}

	@Test
	void hostilesGripHarderInTheDark() {
		assertEquals(2.0F, VelorumCongregationRules.gripFill(true));
		assertEquals(VelorumStillnessRules.GRIP_FILL, VelorumCongregationRules.gripFill(false));
		assertEquals(1.25D, VelorumCongregationRules.DARK_SPEED);
	}

	@Test
	void theThawAndTheFloors() {
		assertEquals(30, VelorumCongregationRules.thawTicks(0));
		assertEquals(40, VelorumCongregationRules.thawTicks(VelorumCongregationRules.PROSTRATE));
		assertEquals(2, VelorumCongregationRules.RISES_PER_FLOOR);
		assertEquals(30.0F, VelorumCongregationRules.HEALTH);
	}
}
