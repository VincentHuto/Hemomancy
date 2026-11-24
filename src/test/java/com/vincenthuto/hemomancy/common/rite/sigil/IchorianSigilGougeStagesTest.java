package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeStages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IchorianSigilGougeStagesTest {
	private static final float EPSILON = 1.0e-5F;

	@Test
	void cutInOpensOverItsTicksAndStaysOpen() {
		assertEquals(0.0F, cutIn(0.0F), EPSILON);
		assertEquals(1.0F, cutIn(CUT_IN_TICKS), EPSILON);
		assertEquals(1.0F, cutIn(SETTLED), EPSILON);
		float previous = cutIn(0.0F);
		for (float age = 1.0F; age <= CUT_IN_TICKS; age += 1.0F) {
			float now = cutIn(age);
			assertTrue(now >= previous, "cut-in never closes at age " + age);
			previous = now;
		}
	}

	@Test
	void fillWaitsForTheAwakening() {
		assertEquals(0.0F, fill(-1.0F), EPSILON);
		assertEquals(1.0F, fill(COMPLETION_TICKS), EPSILON);
	}

	@Test
	void glowIsDarkBeforeTheAwakening() {
		assertEquals(0.0F, glow(-1.0F, 0.5F, 1.0F), EPSILON);
	}

	@Test
	void theFirstFrontRunsThePatternInOrder() {
		assertTrue(glow(10.0F, 0.25F, 0.0F) > glow(10.0F, 0.9F, 0.0F), "front near the start early on");
		assertTrue(glow(35.0F, 0.9F, 0.0F) > glow(35.0F, 0.1F, 0.0F), "front near the end late on");
	}

	@Test
	void theSettledGlowPulsesWithinItsBand() {
		for (float along : new float[] {0.0F, 0.5F, 1.0F}) {
			float value = glow(SETTLED, along, 0.0F);
			assertTrue(value >= 0.35F - EPSILON && value <= 0.75F + EPSILON, "settled glow " + value + " at " + along);
		}
		assertTrue(glow(SETTLED, 0.5F, 1.0F) > glow(SETTLED, 0.5F, 0.0F), "the heartbeat brightens the settled glow");
	}

	@Test
	void aSettledSigilStillReRunsItsFront() {
		float lowest = Float.MAX_VALUE;
		float highest = -Float.MAX_VALUE;
		for (float time = 0.0F; time < RERUN_PERIOD_TICKS; time += 1.0F) {
			float age = settledAge(time);
			assertTrue(age >= SETTLED, "a settled age stays long done");
			float value = glow(age, 0.5F, 0.0F);
			lowest = Math.min(lowest, value);
			highest = Math.max(highest, value);
		}
		assertTrue(highest - lowest > 0.3F, "the re-run front crosses a fixed point once a period");
		assertEquals(settledAge(10.0F), settledAge(10.0F + RERUN_PERIOD_TICKS), 0.1F);
	}

	@Test
	void anEndingDrainsTheCutsAndOnlyAViolentOneClosesThem() {
		assertEquals(1.0F, drain(0.0F, false), EPSILON);
		assertEquals(0.0F, drain(DRAIN_TICKS, false), EPSILON);
		assertEquals(0.0F, drain(VIOLENT_DRAIN_TICKS, true), EPSILON);
		assertTrue(drain(10.0F, true) < drain(10.0F, false), "a violent ending drains faster");
		assertEquals(1.0F, close(1000.0F, false), EPSILON);
		assertEquals(1.0F, close(VIOLENT_DRAIN_TICKS, true), EPSILON, "the cut stays open until it has drained");
		assertEquals(0.0F, close(VIOLENT_DRAIN_TICKS + VIOLENT_CLOSE_TICKS, true), EPSILON);
	}

	@Test
	void veinsCreepAndTendrilsRiseAfterTheFront() {
		assertEquals(0.0F, veinReach(0.0F), EPSILON);
		assertEquals(1.0F, veinReach(VEIN_CREEP_TICKS), EPSILON);
		assertEquals(0.0F, tendrilRise(COMPLETION_TICKS), EPSILON);
		assertEquals(1.0F, tendrilRise(SETTLED), EPSILON);
	}
}
