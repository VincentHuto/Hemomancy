package com.vincenthuto.hemomancy.common.enchanting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScriptoriumBalanceTest {
    @Test
    void enzymeCostsScaleToNaturalCapAndFitReservoirs() {
        assertEquals(1, ScriptoriumBalance.enzymeCost(1, 5));
        assertEquals(2, ScriptoriumBalance.enzymeCost(3, 5));
        assertEquals(3, ScriptoriumBalance.enzymeCost(5, 5));
        assertEquals(1, ScriptoriumBalance.enzymeCost(1, 3));
        assertEquals(2, ScriptoriumBalance.enzymeCost(2, 3));
        assertEquals(3, ScriptoriumBalance.enzymeCost(3, 3));
        assertEquals(3, ScriptoriumBalance.enzymeCost(7, 5));
        assertEquals(3, ScriptoriumBalance.enzymeCost(1, 1));
    }

    @Test
    void affinityOnlyChangesKnownMatchesAndCapsAtThreeAndAHalf() {
        assertEquals(10.0, ScriptoriumBalance.weight(10, 0, 0));
        assertEquals(17.5, ScriptoriumBalance.weight(10, 1, 0));
        assertEquals(13.5, ScriptoriumBalance.weight(10, 0, 1));
        assertEquals(35.0, ScriptoriumBalance.weight(10, 3, 3));
    }

    @Test
    void curseChanceUsesSelectedBiasAndActualResult() {
        assertEquals(5, ScriptoriumBalance.normalStrain(1, 1, 0));
        assertEquals(17, ScriptoriumBalance.normalStrain(2, 2, 1));
        assertEquals(45, ScriptoriumBalance.normalStrain(3, 8, 8));
        assertEquals(95, ScriptoriumBalance.totalStrain(3, 8, 8, 6));
    }

    @Test
    void overexertionCurvesFollowFirstBalancePass() {
        assertEquals(18, ScriptoriumBalance.excessStrain(1));
        assertEquals(40, ScriptoriumBalance.excessStrain(2));
        assertEquals(90, ScriptoriumBalance.excessStrain(6));
        assertEquals(12, ScriptoriumBalance.useBloodCost(6));
        assertEquals(3.0, ScriptoriumBalance.wearMultiplier(4));
    }
}
