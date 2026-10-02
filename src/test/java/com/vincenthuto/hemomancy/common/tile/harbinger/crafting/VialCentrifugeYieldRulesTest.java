package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VialCentrifugeYieldRulesTest {
    @Test void smallYieldsReceiveFractionalBonuses() {
        assertEquals(2, VialCentrifugeYieldRules.quantity(1, 1, .249));
        assertEquals(1, VialCentrifugeYieldRules.quantity(1, 1, .25));
        assertEquals(2, VialCentrifugeYieldRules.quantity(1, 2, .499));
        assertEquals(1, VialCentrifugeYieldRules.quantity(1, 2, .5));
        assertEquals(5, VialCentrifugeYieldRules.quantity(4, 1, .99));
        assertEquals(6, VialCentrifugeYieldRules.quantity(4, 2, .99));
    }

    @Test void recoveryAveragesMatchTheUpgradeAndDoNotStack() {
        int calibrated = 0;
        int fractionating = 0;
        for (double roll : new double[]{.125, .375, .625, .875}) {
            calibrated += VialCentrifugeYieldRules.quantity(3, 1, roll);
            fractionating += VialCentrifugeYieldRules.quantity(3, 2, roll);
            assertEquals(3, VialCentrifugeYieldRules.quantity(3, 0, roll));
        }
        assertEquals(15, calibrated);
        assertEquals(18, fractionating);
    }

    @Test void powderAndSecondFractionHaveSeparateEligibility() {
        assertTrue(VialCentrifugeYieldRules.powder(0, .499));
        assertFalse(VialCentrifugeYieldRules.powder(0, .5));
        assertTrue(VialCentrifugeYieldRules.powder(1, .749));
        assertFalse(VialCentrifugeYieldRules.powder(1, .75));
        assertTrue(VialCentrifugeYieldRules.powder(2, .999));
        assertTrue(VialCentrifugeYieldRules.secondFraction(2, 2, .499));
        assertFalse(VialCentrifugeYieldRules.secondFraction(2, 2, .5));
        assertFalse(VialCentrifugeYieldRules.secondFraction(2, 1, 0));
        assertFalse(VialCentrifugeYieldRules.secondFraction(1, 3, 0));
    }
}
