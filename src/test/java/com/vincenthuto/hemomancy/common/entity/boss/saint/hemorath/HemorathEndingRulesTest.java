package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathEndingRulesTest {
	@Test
	void theBeatsRunInOrder() {
		assertEquals(HemorathEndingRules.Beat.HIT_STOP, HemorathEndingRules.beat(0));
		assertEquals(HemorathEndingRules.Beat.BURST, HemorathEndingRules.beat(6));
		assertEquals(HemorathEndingRules.Beat.POUR, HemorathEndingRules.beat(30));
		assertEquals(HemorathEndingRules.Beat.FOLD, HemorathEndingRules.beat(80));
		assertEquals(HemorathEndingRules.Beat.HOME, HemorathEndingRules.beat(110));
		assertEquals(HemorathEndingRules.Beat.CLOSED, HemorathEndingRules.beat(125));
		assertTrue(HemorathEndingRules.starts(30, HemorathEndingRules.Beat.POUR));
		assertFalse(HemorathEndingRules.starts(31, HemorathEndingRules.Beat.POUR));
	}

	@Test
	void endTrialRunsOnceAtOneTwentyFiveAndAReloadResumes() {
		SaintEnding.Clock clock = new SaintEnding.Clock(HemorathEndingRules.SPEC);
		int endTrials = 0, endedAt = -1, discardedAt = -1;
		for (int i = 0; i < 200 && discardedAt < 0; i++) {
			SaintEnding.Step step = clock.advance(false);
			if (step == SaintEnding.Step.END_TRIAL || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
				endTrials++;
				endedAt = clock.tick();
			}
			if (step == SaintEnding.Step.DISCARD || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) discardedAt = clock.tick();
			if (clock.tick() == 60) {
				SaintEnding.Clock loaded = SaintEnding.Clock.load(clock.save());
				assertEquals(60, loaded.tick(), "a reload keeps the ending's tick (resolution B-10)");
				clock = loaded;
			}
		}
		assertEquals(1, endTrials);
		assertEquals(125, endedAt);
		assertEquals(130, discardedAt);
		assertTrue(HemorathEndingRules.SPEC.resumeOnLoad());
	}
}
