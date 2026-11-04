package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThelemicMemoryPowerRulesTest {
    @Test void laboringArmsRequiresRealHardCorrectToolWork() {
        assertTrue(ThelemicMemoryPowerRules.laboringEligible(false, 2F, true));
        assertFalse(ThelemicMemoryPowerRules.laboringEligible(false, .9F, true));
        assertFalse(ThelemicMemoryPowerRules.laboringEligible(false, 2F, false));
        assertFalse(ThelemicMemoryPowerRules.laboringEligible(true, 2F, true));
    }

    @Test void coursingLegsCarriesRemainderAcrossTwelveBlockTriggers() {
        var result = ThelemicMemoryPowerRules.coursingDistance(10.5, 14.0);
        assertEquals(2, result.triggers());
        assertEquals(.5, result.remainder(), .0001);
    }

    @Test void secondPulseOnlyReducesTheThresholdCrossingHitAndCapsSixDamage() {
        assertEquals(6F, ThelemicMemoryPowerRules.secondPulseReduction(10F, 20F, 20F), .0001);
        assertEquals(0F, ThelemicMemoryPowerRules.secondPulseReduction(5F, 20F, 1F), .0001);
        assertEquals(1.4F, ThelemicMemoryPowerRules.secondPulseReduction(7F, 20F, 4F), .0001);
    }

    @Test void quietMovementNeedsDarknessAndNoSprint() {
        assertTrue(ThelemicMemoryPowerRules.hushedEligible(4, false, true));
        assertFalse(ThelemicMemoryPowerRules.hushedEligible(5, false, true));
        assertFalse(ThelemicMemoryPowerRules.hushedEligible(2, true, true));
    }

    @Test void enduringVisceraOnlyRestoresExhaustionDrivenFoodLossAtZeroSaturation() {
        assertTrue(ThelemicMemoryPowerRules.enduringEligible(10, 9, 0F, 0F));
        assertFalse(ThelemicMemoryPowerRules.enduringEligible(10, 9, 1F, 0F));
        assertFalse(ThelemicMemoryPowerRules.enduringEligible(10, 8, 0F, 0F));
    }
}
