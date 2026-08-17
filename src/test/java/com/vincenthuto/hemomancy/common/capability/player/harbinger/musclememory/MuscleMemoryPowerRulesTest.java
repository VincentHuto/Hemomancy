package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MuscleMemoryPowerRulesTest {
    @Test void laboringArmsRequiresRealHardCorrectToolWork() {
        assertTrue(MuscleMemoryPowerRules.laboringEligible(false, 2F, true));
        assertFalse(MuscleMemoryPowerRules.laboringEligible(false, .9F, true));
        assertFalse(MuscleMemoryPowerRules.laboringEligible(false, 2F, false));
        assertFalse(MuscleMemoryPowerRules.laboringEligible(true, 2F, true));
    }

    @Test void coursingLegsCarriesRemainderAcrossTwelveBlockTriggers() {
        var result = MuscleMemoryPowerRules.coursingDistance(10.5, 14.0);
        assertEquals(2, result.triggers());
        assertEquals(.5, result.remainder(), .0001);
    }

    @Test void secondPulseOnlyReducesTheThresholdCrossingHitAndCapsSixDamage() {
        assertEquals(6F, MuscleMemoryPowerRules.secondPulseReduction(10F, 20F, 20F), .0001);
        assertEquals(0F, MuscleMemoryPowerRules.secondPulseReduction(5F, 20F, 1F), .0001);
        assertEquals(1.4F, MuscleMemoryPowerRules.secondPulseReduction(7F, 20F, 4F), .0001);
    }

    @Test void quietMovementNeedsDarknessAndNoSprint() {
        assertTrue(MuscleMemoryPowerRules.hushedEligible(4, false, true));
        assertFalse(MuscleMemoryPowerRules.hushedEligible(5, false, true));
        assertFalse(MuscleMemoryPowerRules.hushedEligible(2, true, true));
    }

    @Test void enduringVisceraOnlyRestoresExhaustionDrivenFoodLossAtZeroSaturation() {
        assertTrue(MuscleMemoryPowerRules.enduringEligible(10, 9, 0F, 0F));
        assertFalse(MuscleMemoryPowerRules.enduringEligible(10, 9, 1F, 0F));
        assertFalse(MuscleMemoryPowerRules.enduringEligible(10, 8, 0F, 0F));
    }
}
