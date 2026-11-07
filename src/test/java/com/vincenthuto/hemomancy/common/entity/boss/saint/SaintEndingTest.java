package com.vincenthuto.hemomancy.common.entity.boss.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The ending window: endTrial once at its tick, finishing early, the discard at its length, resuming or finishing on load. */
final class SaintEndingTest {
	private static final SaintEnding.Spec WINDOW = new SaintEnding.Spec(30, 60, false, true,
			SaintArena.RestoreOrder.OUTERMOST_FIRST, 20);

	@Test
	void endTrialRunsOnceAtItsTickAndTheSaintGoesAtTheLength() {
		SaintEnding.Clock clock = new SaintEnding.Clock(WINDOW);
		int ends = 0, discards = 0, endTick = -1, discardTick = -1;
		for (int i = 0; i < 80; i++) {
			SaintEnding.Step step = clock.advance(false);
			if (step == SaintEnding.Step.END_TRIAL || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
				ends++;
				endTick = clock.tick();
			}
			if (step == SaintEnding.Step.DISCARD || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
				discards++;
				discardTick = clock.tick();
				break;
			}
		}
		assertEquals(1, ends);
		assertEquals(30, endTick);
		assertEquals(1, discards);
		assertEquals(60, discardTick);
	}

	@Test
	void finishEarlyEndsWhenTheSaintsConditionHolds() {
		SaintEnding.Clock clock = new SaintEnding.Clock(new SaintEnding.Spec(200, 240, true, true,
				SaintArena.RestoreOrder.IMMEDIATE, 0));
		for (int i = 0; i < 10; i++) assertEquals(SaintEnding.Step.NONE, clock.advance(false));
		assertEquals(SaintEnding.Step.END_TRIAL_AND_DISCARD, clock.advance(true), "the faithful are home");
	}

	@Test
	void anImmediateEndingSettlesInTwoTicks() {
		SaintEnding.Clock clock = new SaintEnding.Clock(SaintEnding.Spec.IMMEDIATE);
		assertEquals(SaintEnding.Step.END_TRIAL, clock.advance(false));
		assertEquals(SaintEnding.Step.DISCARD, clock.advance(false));
	}

	@Test
	void aReloadResumesOrFinishes() {
		SaintEnding.Clock clock = new SaintEnding.Clock(WINDOW);
		for (int i = 0; i < 10; i++) clock.advance(false);
		SaintEnding.Clock resumed = SaintEnding.Clock.load(clock.save());
		assertEquals(9, resumed.tick());
		assertEquals(SaintEnding.Step.NONE, resumed.advance(false), "resumeOnLoad carries on");
		SaintEnding.Clock finishing = SaintEnding.Clock.load(new SaintEnding.Clock(new SaintEnding.Spec(30, 60, false,
				false, SaintArena.RestoreOrder.IMMEDIATE, 0)).save());
		assertEquals(SaintEnding.Step.END_TRIAL_AND_DISCARD, finishing.advance(false),
				"without resumeOnLoad the ending finishes on the first tick");
		SaintEnding.Clock ended = new SaintEnding.Clock(new SaintEnding.Spec(2, 60, false, false,
				SaintArena.RestoreOrder.IMMEDIATE, 0));
		for (int i = 0; i < 5; i++) ended.advance(false);
		SaintEnding.Clock reloaded = SaintEnding.Clock.load(ended.save());
		assertTrue(reloaded.trialEnded());
		assertEquals(SaintEnding.Step.DISCARD, reloaded.advance(false), "endTrial never runs twice");
		assertEquals(WINDOW, SaintEnding.Spec.load(WINDOW.save()));
	}
}
