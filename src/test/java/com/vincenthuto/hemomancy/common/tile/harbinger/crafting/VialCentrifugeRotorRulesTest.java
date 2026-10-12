package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VialCentrifugeRotorRulesTest {
    @Test void driveWindsUpHoldsAndCutsOutBeforeTheBatchLands() {
        int total = VialCentrifugeYieldRules.duration(0);
        assertEquals(0, VialCentrifugeRotorRules.drive(0, total));
        assertEquals(0, VialCentrifugeRotorRules.drive(10, 0));
        assertTrue(VialCentrifugeRotorRules.drive(total, total) > 0, "a fresh spin starts turning at once");
        assertTrue(VialCentrifugeRotorRules.drive(total - 10, total) < 1);
        assertEquals(1, VialCentrifugeRotorRules.drive(total / 2, total));
        assertTrue(VialCentrifugeRotorRules.drive(5, total) < .2F);
        assertEquals(VialCentrifugeRotorRules.drive(total, total), VialCentrifugeRotorRules.drive(total * 2, total),
                "stale overlong progress clamps to the start of the spin");
    }

    @Test void upgradedRotorsRunFasterAndStagesClamp() {
        assertTrue(VialCentrifugeRotorRules.topSpeed(1) > VialCentrifugeRotorRules.topSpeed(0));
        assertTrue(VialCentrifugeRotorRules.topSpeed(2) > VialCentrifugeRotorRules.topSpeed(1));
        assertEquals(VialCentrifugeRotorRules.topSpeed(2), VialCentrifugeRotorRules.topSpeed(9));
        assertEquals(VialCentrifugeRotorRules.topSpeed(0), VialCentrifugeRotorRules.topSpeed(-1));
    }

    @Test void inertiaSpinsUpFasterThanItCoastsAndSettlesAtRest() {
        float up = VialCentrifugeRotorRules.approach(0, 30);
        float down = 30 - VialCentrifugeRotorRules.approach(30, 0);
        assertTrue(up > down);
        float speed = 30;
        int ticks = 0;
        while (speed > 0 && ticks++ < 1000) speed = VialCentrifugeRotorRules.approach(speed, 0);
        assertEquals(0, speed, "the rotor comes fully to rest");
        assertTrue(ticks > 40, "the rotor coasts visibly instead of stopping dead");
    }

    @Test void bucketsGovernorAndNeedleFollowSpeed() {
        assertEquals(0, VialCentrifugeRotorRules.swing(0));
        assertEquals(VialCentrifugeRotorRules.MAX_SWING, VialCentrifugeRotorRules.swing(.7F), 1e-4);
        assertEquals(VialCentrifugeRotorRules.MAX_SWING, VialCentrifugeRotorRules.swing(1.5F), 1e-4);
        assertTrue(VialCentrifugeRotorRules.swing(.3F) < VialCentrifugeRotorRules.swing(.5F));
        assertEquals(0, VialCentrifugeRotorRules.governorLift(0));
        assertEquals(VialCentrifugeRotorRules.MAX_GOVERNOR_LIFT, VialCentrifugeRotorRules.governorLift(1), 1e-4);
        assertEquals(-VialCentrifugeRotorRules.NEEDLE_SWEEP, VialCentrifugeRotorRules.needle(0));
        assertEquals(VialCentrifugeRotorRules.NEEDLE_SWEEP, VialCentrifugeRotorRules.needle(2));
        assertEquals(1, VialCentrifugeRotorRules.speedFraction(1, VialCentrifugeRotorRules.topSpeed(1)));
    }

    @Test void balancedPairsSitOppositeOnTheRotor() {
        int[][] pairs = {{2, 6}, {3, 7}, {4, 8}, {9, 5}};
        for (int[] pair : pairs)
            assertEquals(180, Math.abs(VialCentrifugeRotorRules.slotAngle(pair[0]) - VialCentrifugeRotorRules.slotAngle(pair[1])));
    }
}
