package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import static org.junit.jupiter.api.Assertions.*;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import org.junit.jupiter.api.Test;

class SanguineWardRulesTest {
    @Test void oneStrandPerPointOfPoolAndTheCrownNeedsAFullPool() {
        assertEquals(SanguineWardRules.STRANDS, (int) ManipulationReactiveEvents.SANGUINE_WARD_POOL,
                "the lattice must be able to show the whole pool");
        assertEquals(0, SanguineWardRules.live(0));
        assertEquals(3, SanguineWardRules.live(3));
        assertEquals(SanguineWardRules.STRANDS, SanguineWardRules.live(99));
        assertTrue(SanguineWardRules.crowned((int) Math.ceil(ManipulationReactiveEvents.SANGUINE_WARD_POOL)));
        assertFalse(SanguineWardRules.crowned(SanguineWardRules.STRANDS - 1));
    }

    @Test void crossingsSitWhereTheTwoFamiliesMeet() {
        int found = 0;
        for (double time : new double[] { 0, 41.5, 300 })
            for (int up = 0; up < SanguineWardRules.STRANDS; up++)
                for (int down = 0; down < SanguineWardRules.STRANDS; down++) {
                    double t = SanguineWardRules.crossing(up, down, time);
                    if (t < 0) continue;
                    found++;
                    double a = SanguineWardRules.angle(up, 1, t, time), b = SanguineWardRules.angle(down, -1, t, time);
                    assertEquals(0, Math.IEEEremainder(a - b, Math.PI * 2), 1e-9, "strands " + up + "/" + down + " at " + time);
                }
        assertTrue(found > 20, "the lattice should cross many times: " + found);
    }

    @Test void theLatticeRaisesOnceAndHolds() {
        assertEquals(0, SanguineWardRules.raise(0), 1e-9);
        assertEquals(1, SanguineWardRules.raise(SanguineWardRules.RAISE_TICKS), 1e-9);
        assertEquals(1, SanguineWardRules.raise(400), 1e-9);
    }
}
