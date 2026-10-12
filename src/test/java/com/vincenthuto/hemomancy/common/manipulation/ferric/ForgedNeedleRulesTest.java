package com.vincenthuto.hemomancy.common.manipulation.ferric;

import static com.vincenthuto.hemomancy.common.manipulation.ferric.ForgedNeedleRules.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ForgedNeedleRulesTest {
    @Test void profileSwellsAtTheShoulderAndComesToAPoint() {
        assertEquals(0, profile(1, WIDTH), 1e-12);
        assertTrue(profile(SHOULDER, WIDTH) > profile(0, WIDTH));
        assertTrue(profile(0.95, WIDTH) < profile(SHOULDER, WIDTH));
    }

    @Test void barbsSitBehindTheShoulderAndStayOnTheShaft() {
        for (int i = 0; i < LANCE_BARBS; i++) {
            assertTrue(barbStation(i) < SHOULDER && barbStation(i) > 0, "barb " + i);
            if (i > 0) assertTrue(barbStation(i) < barbStation(i - 1));
        }
    }

    @Test void needlesForgeInTurnAndAllFinish() {
        for (int count : new int[] { 3, 4, 7 }) {
            for (int i = 1; i < count; i++) assertTrue(forged(.4, i, count) <= forged(.4, i - 1, count));
            for (int i = 0; i < count; i++) assertEquals(1, forged(1, i, count), 1e-9);
            assertEquals(0, forged(0, count - 1, count), 1e-9);
        }
    }
}
