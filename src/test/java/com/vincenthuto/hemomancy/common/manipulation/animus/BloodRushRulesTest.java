package com.vincenthuto.hemomancy.common.manipulation.animus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BloodRushRulesTest {
    @Test void surgeThenLaunchThenFray() {
        assertEquals(1, BloodRushRules.surge(BloodRushRules.SURGE_END), 1e-9);
        assertEquals(0, BloodRushRules.wake(BloodRushRules.SURGE_END - .5), 1e-9);
        assertEquals(1, BloodRushRules.wake(BloodRushRules.LAUNCH_END), 1e-9);
        assertEquals(0, BloodRushRules.fray(BloodRushRules.LAUNCH_END), 1e-9);
        assertEquals(1, BloodRushRules.fray(BloodRushRules.VISUAL_TICKS), 1e-9);
    }

    @Test void heartbeatsTravelDownFromTheChestOneAfterAnother() {
        assertEquals(1, BloodRushRules.beatHeight(0, 0), 1e-9);
        assertTrue(BloodRushRules.beatHeight(1.6, 0) < BloodRushRules.beatHeight(1.6, 1), "later rings trail the first");
        double last = 1;
        for (double age = 0; age <= 2.4; age += .2) {
            double h = BloodRushRules.beatHeight(age, 0);
            assertTrue(h <= last + 1e-12);
            last = h;
        }
        assertTrue(BloodRushRules.beatHeight(10, 2) < 0, "every ring reaches the feet");
    }
}
