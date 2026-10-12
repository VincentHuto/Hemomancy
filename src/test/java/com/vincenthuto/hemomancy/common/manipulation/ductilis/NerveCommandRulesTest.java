package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import static com.vincenthuto.hemomancy.common.manipulation.ductilis.NerveCommandRules.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NerveCommandRulesTest {
    @Test void modesAreDistinct() {
        assertTrue(rebuke(REBUKE));
        assertFalse(rebuke(IMPRESS));
    }

    @Test void impulseLandsThenFades() {
        assertEquals(0, impulse(0), 1e-9);
        assertEquals(1, impulse(IMPULSE_TICKS - 2), 1e-9);
        assertEquals(1, impulseFade(1), 1e-9);
        assertEquals(0, impulseFade(IMPULSE_TICKS), 1e-9);
    }

    @Test void rebukeRecoilsThenTremblesWhileImpressmentStaysCalm() {
        assertTrue(jitter(IMPULSE_TICKS - 2, true) > jitter(40, true), "the recoil settles");
        assertTrue(jitter(40, true) > jitter(40, false), "a rebuked body keeps trembling");
        double rebukeStep = spinePulse(1, true, 0) - spinePulse(0, true, 0);
        double impressStep = spinePulse(1, false, 0) - spinePulse(0, false, 0);
        assertTrue(rebukeStep > impressStep, "fearful pulses run faster");
        for (double t = 0; t < 100; t += 3.7) {
            double phase = leashPulse(t, 9);
            assertTrue(phase >= 0 && phase < 1);
        }
    }
}
