package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeEndingRulesTest {
    @Test void theBeatsRunInOrder() {
        assertEquals(SeraphaeEndingRules.Beat.FLARE, SeraphaeEndingRules.beat(0));
        assertEquals(SeraphaeEndingRules.Beat.REEL, SeraphaeEndingRules.beat(20));
        assertEquals(SeraphaeEndingRules.Beat.STILL, SeraphaeEndingRules.beat(75));
        assertEquals(SeraphaeEndingRules.Beat.SINK, SeraphaeEndingRules.beat(95));
        assertEquals(SeraphaeEndingRules.Beat.RESTORE, SeraphaeEndingRules.beat(110));
        assertEquals(SeraphaeEndingRules.Beat.FADE, SeraphaeEndingRules.beat(130));
    }

    @Test void endTrialRunsOnceAtOneHundredAndSheGoesAtOneForty() {
        SaintEnding.Clock clock = new SaintEnding.Clock(SeraphaeEndingRules.SPEC);
        int ended = 0, discarded = -1;
        for (int tick = 0; tick <= SeraphaeEndingRules.LENGTH; tick++) {
            SaintEnding.Step step = clock.advance(false);
            if (step == SaintEnding.Step.END_TRIAL || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
                ended++;
                assertEquals(SeraphaeEndingRules.END_TRIAL, clock.tick());
            }
            if (step == SaintEnding.Step.DISCARD || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
                discarded = clock.tick();
                break;
            }
        }
        assertEquals(1, ended);
        assertEquals(SeraphaeEndingRules.LENGTH, discarded);
        assertEquals(SaintArena.RestoreOrder.OUTERMOST_FIRST, SeraphaeEndingRules.SPEC.restore(), "walls first, inward");
        assertEquals(20, SeraphaeEndingRules.SPEC.restoreTicks());
    }

    @Test void aReloadResumesFromAnySavedTick() {
        assertTrue(SeraphaeEndingRules.SPEC.resumeOnLoad(), "resolution B-10");
        for (int saved : new int[]{0, 40, 99, 100, 120}) {
            SaintEnding.Clock clock = new SaintEnding.Clock(SeraphaeEndingRules.SPEC);
            for (int i = 0; i <= saved; i++) clock.advance(false);
            SaintEnding.Clock loaded = SaintEnding.Clock.load(clock.save());
            int ended = clock.trialEnded() ? 1 : 0;
            for (int i = saved + 1; i <= SeraphaeEndingRules.LENGTH; i++) {
                SaintEnding.Step step = loaded.advance(false);
                if (step == SaintEnding.Step.END_TRIAL || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) ended++;
            }
            assertEquals(1, ended, "saved at " + saved);
        }
    }

    @Test void herLightRetreatsAndSheSinksAfterTheReel() {
        assertEquals(0.0F, SeraphaeEndingRules.reel(20), 1.0E-6F);
        assertEquals(1.0F, SeraphaeEndingRules.reel(60), 1.0E-6F);
        assertEquals(1.0F, SeraphaeEndingRules.light(60), 1.0E-6F);
        assertEquals(0.0F, SeraphaeEndingRules.light(90), 1.0E-6F);
        assertEquals(0.0F, SeraphaeEndingRules.sink(90), 1.0E-6F);
        assertEquals(1.0F, SeraphaeEndingRules.sink(100), 1.0E-6F);
        assertTrue(SeraphaeEndingRules.flaresAt(0, 0) && SeraphaeEndingRules.flaresAt(15, 3));
        assertFalse(SeraphaeEndingRules.flaresAt(20, 4), "the flares end at tick 20");
        double[] home = SeraphaeEndingRules.reelPoint(0, 0, 0, 10, 2, 0, 60);
        assertEquals(10.0D, home[0], 1.0E-6D);
    }
}
