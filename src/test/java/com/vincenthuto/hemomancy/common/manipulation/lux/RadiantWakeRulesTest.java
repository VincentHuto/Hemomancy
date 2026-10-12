package com.vincenthuto.hemomancy.common.manipulation.lux;

import static com.vincenthuto.hemomancy.common.manipulation.lux.RadiantWakeRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RadiantWakeRulesTest {
    @Test
    void glideFlagRoundTrips() {
        assertTrue(gliding(encode(true)));
        assertFalse(gliding(encode(false)));
        assertTrue(encode(false) >= 1);
    }

    @Test
    void wingsUnfurlOnceAndGlidingHoldsThemSteadier() {
        assertEquals(0, unfurl(0), 1e-9);
        assertEquals(1, unfurl(UNFURL_TICKS), 1e-9);
        assertEquals(1, unfurl(UNFURL_TICKS * 4), 1e-9);
        double standing = 0, gliding = 0;
        for (double t = 0; t < 200; t += 0.5) {
            standing = Math.max(standing, Math.abs(beat(t, false)));
            gliding = Math.max(gliding, Math.abs(beat(t, true)));
        }
        assertTrue(gliding < standing);
        assertTrue(featherSweep(FEATHERS - 1, 1) > featherSweep(0, 1), "outer feathers trail furthest");
        assertTrue(CUE_TICKS > 20, "a held channel's once-a-second pulse must refresh the cue before it lapses");
    }
}
