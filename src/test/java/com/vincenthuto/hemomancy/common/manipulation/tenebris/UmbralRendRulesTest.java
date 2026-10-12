package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import static com.vincenthuto.hemomancy.common.manipulation.tenebris.UmbralRendRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UmbralRendRulesTest {
    @Test
    void seedRoundTripsTiltAndMirrorAndStaysPositive() {
        for (int tilt = 0; tilt < TILTS; tilt++)
            for (boolean mirrored : new boolean[] {false, true}) {
                int count = encode(tilt, mirrored);
                assertTrue(count >= 1, "count must never read as an empty cue");
                assertEquals(tilt, tilt(count));
                assertEquals(mirrored, mirrored(count));
            }
    }

    @Test
    void slantStaysASteepDiagonalAndMirrors() {
        for (int tilt = 0; tilt < TILTS; tilt++) {
            double up = Math.toDegrees(slant(encode(tilt, false)));
            assertTrue(up >= 35 - 1e-9 && up <= 65 + 1e-9, "slant " + up);
            assertEquals(-up, Math.toDegrees(slant(encode(tilt, true))), 1e-9);
        }
    }

    @Test
    void phasesRunInOrder() {
        assertEquals(0, gather(0), 1e-9);
        assertEquals(1, gather(GATHER_END), 1e-9);
        assertEquals(0, tear(GATHER_END, 0), 1e-9);
        assertEquals(1, tear(REND_END, 0), 1e-9);
        assertTrue(tear(GATHER_END + 1, 1) < tear(GATHER_END + 1, 0), "later claws trail the first");
        assertEquals(0, gape(REND_END - 1), 1e-9);
        assertTrue(gape(GAPE_END) > 0.9, "wounds stand open after the tear");
        assertEquals(0, fray(GAPE_END), 1e-9);
        assertEquals(1, fray(VISUAL_TICKS), 1e-9);
        assertTrue(gape(VISUAL_TICKS) < gape(GAPE_END), "wounds close as the lips fray");
    }
}
