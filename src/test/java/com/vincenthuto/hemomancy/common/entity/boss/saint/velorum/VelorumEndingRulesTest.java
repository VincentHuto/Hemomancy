package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumEndingRulesTest {
	@Test
	void theBeatsInOrder() {
		assertEquals(VelorumEndingRules.Beat.HAND, VelorumEndingRules.beat(0));
		assertEquals(VelorumEndingRules.Beat.HOOD, VelorumEndingRules.beat(16));
		assertEquals(VelorumEndingRules.Beat.FROST, VelorumEndingRules.beat(40));
		assertEquals(VelorumEndingRules.Beat.TURN, VelorumEndingRules.beat(64));
		assertEquals(VelorumEndingRules.Beat.WALK, VelorumEndingRules.beat(80));
		assertEquals(VelorumEndingRules.Beat.END, VelorumEndingRules.beat(200));
	}

	@Test
	void theMetersDrainBetweenFortyAndEighty() {
		assertEquals(1.0F, VelorumEndingRules.meterLeft(40));
		assertEquals(0.5F, VelorumEndingRules.meterLeft(60));
		assertEquals(0.0F, VelorumEndingRules.meterLeft(80));
	}

	@Test
	void earlyOnlyOnceTheWalkHasBegunAndEveryoneIsHome() {
		assertFalse(VelorumEndingRules.done(10, true));
		assertTrue(VelorumEndingRules.done(80, true));
		assertFalse(VelorumEndingRules.done(150, false));
	}

	@Test
	void theWindowCapsAtTwoHundredAndResumes() {
		SaintEnding.Spec spec = VelorumEndingRules.SPEC;
		assertEquals(200, spec.endTrialTick());
		assertTrue(spec.finishEarly() && spec.resumeOnLoad());
		SaintEnding.Clock clock = new SaintEnding.Clock(spec);
		SaintEnding.Step step = SaintEnding.Step.NONE;
		int ticks = 0;
		while (step == SaintEnding.Step.NONE && ticks < 400) {
			step = clock.advance(VelorumEndingRules.done(clock.tick() + 1, false));
			ticks++;
		}
		assertEquals(SaintEnding.Step.END_TRIAL_AND_DISCARD, step);
		assertEquals(201, ticks);
		SaintEnding.Clock early = new SaintEnding.Clock(spec);
		SaintEnding.Step first = SaintEnding.Step.NONE;
		for (int i = 0; i <= 80 && first == SaintEnding.Step.NONE; i++) first = early.advance(VelorumEndingRules.done(i, true));
		assertEquals(SaintEnding.Step.END_TRIAL_AND_DISCARD, first);
		assertEquals(80, early.tick());
	}
}
