package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeNerveFlashRulesTest {
    @Test void aFrontRunsRootToTipInSixTicksAndLeavesAFourTickAfterglow() {
        assertEquals(1.0F, SeraphaeNerveFlashRules.front(0, 0), 1.0E-6F);
        assertEquals(0.0F, SeraphaeNerveFlashRules.front(0, 3), 1.0E-6F, "the tip is still dark at the root's flash");
        assertEquals(1.0F, SeraphaeNerveFlashRules.front(6, 3), 1.0E-6F, "the tip lights at tick six");
        assertEquals(0.5F, SeraphaeNerveFlashRules.front(2, 0), 1.0E-6F);
        assertEquals(0.0F, SeraphaeNerveFlashRules.front(4, 0), 1.0E-6F);
        assertEquals(0.0F, SeraphaeNerveFlashRules.front(-1, 0), 1.0E-6F);
    }

    @Test void idleFiringComesEverySeventyToOneHundredTenTicks() {
        for (int seed : new int[]{1, 7, 99, 12345}) {
            for (int n = 1; n < 40; n++) {
                int gap = SeraphaeNerveFlashRules.idleFireTick(seed, n) - SeraphaeNerveFlashRules.idleFireTick(seed, n - 1);
                assertTrue(gap >= 70 && gap <= 110, "gap " + gap);
                int trunk = SeraphaeNerveFlashRules.idleTrunk(seed, n);
                assertTrue(trunk >= 0 && trunk < SeraphaeNerveFlashRules.TRUNKS);
            }
        }
    }

    @Test void theFiredTrunkLightsAndTheOthersStayDark() {
        int seed = 7, n = 5;
        int at = SeraphaeNerveFlashRules.idleFireTick(seed, n);
        int trunk = SeraphaeNerveFlashRules.idleTrunk(seed, n);
        assertEquals(1.0F, SeraphaeNerveFlashRules.idle(seed, trunk, 0, at), 1.0E-6F);
        int other = (trunk + 1) % SeraphaeNerveFlashRules.TRUNKS;
        if (SeraphaeNerveFlashRules.idleTrunk(seed, n - 1) != other) {
            assertEquals(0.0F, SeraphaeNerveFlashRules.idle(seed, other, 0, at), 1.0E-6F);
        }
    }

    @Test void theConvulsionStuttersThreeTimes() {
        int lit = 0;
        for (int tick = 0; tick < 24; tick++) if (SeraphaeNerveFlashRules.stutter(tick) >= 0.99F) lit++;
        assertEquals(3, lit);
        assertEquals(0.0F, SeraphaeNerveFlashRules.stutter(9), 1.0E-6F);
    }

    @Test void trunksIndexLeftThenRight() {
        assertEquals(0, SeraphaeNerveFlashRules.trunk(true, 0));
        assertEquals(5, SeraphaeNerveFlashRules.trunk(false, 2));
    }
}
