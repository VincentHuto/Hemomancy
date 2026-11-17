package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhlegethosomaSummonRulesTest {
    private static int ticksToSummon(int breakAt, int breakTicks) {
        double ml = 0.0D;
        long gap = 1L;
        for (int tick = 1; tick < 2_000; tick++) {
            ml = PhlegethosomaSummonRules.pour(ml, gap, PhlegethosomaSummonRules.ML_PER_TICK);
            gap = tick == breakAt ? breakTicks + 1L : 1L;
            if (PhlegethosomaSummonRules.reached(ml)) return tick;
        }
        return -1;
    }

    @Test
    void oneSteadyPourOfAboutFourteenSecondsReachesFiveHundredMillilitres() {
        int ticks = ticksToSummon(-1, 0);
        assertTrue(ticks > 250 && ticks < 300, "a held pour took " + ticks + " ticks");
    }

    @Test
    void aShortBreakDoesNotResetTheCount() {
        int steady = ticksToSummon(-1, 0);
        int broken = ticksToSummon(100, 40);
        assertTrue(broken > steady && broken < steady + 20, "a two-second break cost " + (broken - steady) + " ticks");
    }

    @Test
    void theCountFadesByHalfInFortyFiveSeconds() {
        assertEquals(200.0D, PhlegethosomaSummonRules.fade(400.0D, 900L), 1.0E-6D);
        assertEquals(0.0D, PhlegethosomaSummonRules.fade(2.0D, 9_000L), 1.0E-6D);
    }

    @Test
    void theCountStopsAtFiveHundred() {
        assertEquals(500.0D, PhlegethosomaSummonRules.pour(499.5D, 0L, 2.0D), 1.0E-6D);
        assertTrue(PhlegethosomaSummonRules.reached(500.0D));
        assertFalse(PhlegethosomaSummonRules.reached(498.0D));
    }

    @Test
    void aTargetlessSummonDivesAfterSixtySeconds() {
        assertFalse(PhlegethosomaSummonRules.shouldDive(1_199));
        assertTrue(PhlegethosomaSummonRules.shouldDive(1_200));
    }
}
