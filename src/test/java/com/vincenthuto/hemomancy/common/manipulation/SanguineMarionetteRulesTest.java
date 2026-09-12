package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SanguineMarionetteRulesTest {
    @Test void eightSecondsHasEightPaidPulsesIncludingAcquisition() {
        assertTrue(SanguineMarionetteRules.maintain(0, 0, 16));
        assertTrue(SanguineMarionetteRules.maintain(159, 0, 16));
        assertFalse(SanguineMarionetteRules.maintain(160, 0, 16));
        assertFalse(SanguineMarionetteRules.maintain(1, 0, 16.01));
        assertEquals(400, SanguineMarionetteRules.MAX_TICKS / 20 * SanguineMarionetteRules.PULSE_COST);
        assertEquals(120, SanguineMarionetteRules.COOLDOWN);
    }
}
