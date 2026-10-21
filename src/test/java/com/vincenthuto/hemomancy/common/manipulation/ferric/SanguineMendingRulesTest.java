package com.vincenthuto.hemomancy.common.manipulation.ferric;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SanguineMendingRulesTest {
    @Test void aPulseFitsBetweenChannelPulses() {
        assertTrue(SanguineMendingRules.VISUAL_TICKS < 20, "each 20-tick channel pulse must start a fresh cue");
    }

    @Test void cracksCloseOneAfterAnotherBeforeTheBloodRecedes() {
        for (int crack = 0; crack < SanguineMendingRules.CRACKS; crack++) {
            assertEquals(0, SanguineMendingRules.seal(SanguineMendingRules.DRAW_END, crack), 1e-9);
            assertEquals(1, SanguineMendingRules.seal(SanguineMendingRules.SEAL_END + 1, crack), 1e-9, "crack " + crack);
            if (crack > 0) assertTrue(SanguineMendingRules.seal(8, crack) < SanguineMendingRules.seal(8, crack - 1));
            double at = SanguineMendingRules.crackAt(crack);
            assertTrue(at > 0 && at < 1);
        }
        assertEquals(0, SanguineMendingRules.recede(SanguineMendingRules.SEAL_END), 1e-9);
        assertEquals(1, SanguineMendingRules.recede(SanguineMendingRules.VISUAL_TICKS), 1e-9);
    }
}
