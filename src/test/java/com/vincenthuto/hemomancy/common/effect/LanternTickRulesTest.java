package com.vincenthuto.hemomancy.common.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanternTickRulesTest {
	@Test
	void latchAndLureNumbers() {
		assertEquals(10, LanternTickRules.HELMET_LIGHT_LEVEL, "helmet light level");
		assertEquals(160, LanternTickRules.LATCH_DRAIN_INTERVAL_TICKS, "latch drain interval");
		assertEquals(15.0D, LanternTickRules.LURE_PLAYER_DISTANCE, "lure player distance");
		assertEquals(3.0D, LanternTickRules.LATCH_DISTANCE, "latch distance");
		assertEquals(1.4D, LanternTickRules.LATCH_LEAP_STRENGTH, "latch leap strength");
		assertEquals(1.0F, LanternTickRules.FALLBACK_DAMAGE_PER_BITE, "fallback bite damage");
	}

	@Test
	void aLatchedTickDrainsTwentyFiveMillilitresABiteAndOutrunsRegeneration() {
		assertEquals(25.0D, LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE, "blood drain per bite (NQ-06-4)");
		double perSecond = LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE / (LanternTickRules.LATCH_DRAIN_INTERVAL_TICKS / 20.0D);
		assertTrue(perSecond > 1.0D, "a latched tick must cost more than the 1 mL a second plain regeneration returns");
	}
}
