package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import static com.vincenthuto.hemomancy.common.manipulation.flammeus.SearingLashRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SearingLashRulesTest {
    @Test
    void sideRoundTripsAndCountStaysPositive() {
        assertTrue(encode(false) >= 1 && encode(true) >= 1);
        assertEquals(1, side(encode(false)));
        assertEquals(-1, side(encode(true)));
    }

    @Test
    void whipReachesTheTargetBeforeItCracksAndThenGoesSlack() {
        assertEquals(0, extent(0), 1e-9);
        assertEquals(1, extent(UNCOIL_END), 1e-9);
        assertEquals(0, crack(UNCOIL_END - 1), 1e-9);
        assertTrue(crack(UNCOIL_END + 0.5) > 0.8, "crack peaks as the whip arrives");
        assertEquals(0, crack(CRACK_END + 1), 1e-9);
        assertEquals(0, slack(CRACK_END), 1e-9);
        assertEquals(1, slack(VISUAL_TICKS), 1e-9);
    }

    @Test
    void waveIsPinnedAtBothEndsAndDiesAfterTheCrack() {
        for (double age : new double[] {0, 2, 5, 10}) {
            assertEquals(0, wave(0, age, 1), 1e-9);
            assertEquals(0, wave(1, age, 1), 1e-9);
        }
        assertTrue(Math.abs(wave(0.5, 1, 1)) > Math.abs(wave(0.5, 12, 1)), "the whip straightens once spent");
        assertEquals(-wave(0.4, 1, 1), wave(0.4, 1, -1), 1e-9);
    }
}
