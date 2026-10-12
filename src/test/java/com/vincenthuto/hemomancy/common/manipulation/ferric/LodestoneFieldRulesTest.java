package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeLodestoneRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LodestoneFieldRulesTest {
	@Test
	void visualLastsExactlyAsLongAsTheGameplayField() {
		assertEquals(LivingAxeLodestoneRules.LIFETIME_TICKS, LodestoneFieldRules.VISUAL_TICKS);
		assertEquals(LivingAxeLodestoneRules.PULSE_INTERVAL_TICKS, LodestoneFieldRules.PULSE);
	}

	@Test
	void lodestonePunchesUpPastFullThenSettlesAndSinks() {
		assertEquals(0, LodestoneFieldRules.rise(0), 1e-9);
		assertTrue(LodestoneFieldRules.rise(LodestoneFieldRules.RISE_END * 0.5) > 1, "The bite should overshoot");
		assertEquals(1, LodestoneFieldRules.rise(LodestoneFieldRules.RISE_END), 1e-9);
		assertEquals(1, LodestoneFieldRules.rise(60), 1e-9);
		assertEquals(0, LodestoneFieldRules.rise(LodestoneFieldRules.VISUAL_TICKS), 1e-9);
	}

	@Test
	void everyGameplayPulseFlaresTheField() {
		for (long tick = 0; tick < LivingAxeLodestoneRules.LIFETIME_TICKS; tick++) {
			boolean gameplay = LivingAxeLodestoneRules.shouldPulse(0, tick);
			assertEquals(gameplay, LodestoneFieldRules.pulse(tick) == 1, "Pulse flare out of step at tick " + tick);
		}
		assertTrue(LodestoneFieldRules.pulse(3) < LodestoneFieldRules.pulse(1), "The flare should decay");
		assertEquals(0, LodestoneFieldRules.pulse(LodestoneFieldRules.RIPPLE_TICKS + 1), 1e-9);
	}

	@Test
	void rippleClosesFromRimToCore() {
		double start = LodestoneFieldRules.ripple(20), late = LodestoneFieldRules.ripple(25);
		assertEquals(1, start, 1e-9);
		assertTrue(late < start && late > 0, "The ripple should contract toward the core");
		assertEquals(-1, LodestoneFieldRules.ripple(30), 1e-9);
		assertEquals(-1, LodestoneFieldRules.ripple(LodestoneFieldRules.DISSOLVE_START + 2), 1e-9);
	}

	@Test
	void filingsCreepInwardAndNeverJumpExceptWhenLoopingToTheRim() {
		for (int i = 0; i < LodestoneFieldRules.FILINGS; i++) {
			double previous = LodestoneFieldRules.filing(0, i);
			for (int step = 1; step <= 1200; step++) {
				double age = step / 10.0, current = LodestoneFieldRules.filing(age, i);
				double moved = current - previous;
				assertTrue(moved >= -1e-9 || current < 0.1, "Filing " + i + " moved outward at " + age);
				assertTrue(moved < 0.06 || current < previous, "Filing " + i + " jumped inward at " + age);
				previous = current;
			}
		}
	}
}
