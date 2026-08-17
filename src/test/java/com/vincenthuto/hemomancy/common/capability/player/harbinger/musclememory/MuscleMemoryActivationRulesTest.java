package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MuscleMemoryActivationRulesTest {
    @Test
    void acceptedTriggerAppliesResonanceToCostAndStrain() {
        var resonance = new MuscleMemoryResonanceRules.Resonance(
                MuscleMemoryResonanceRules.Tier.PREFERRED, 2.0 / 3.0, 2.0 / 3.0, true);

        var result = MuscleMemoryActivationRules.evaluate(true, true, 20.0,
                3.0, 0.3F, resonance);

        assertTrue(result.accepted());
        assertEquals(2.0, result.bloodCost(), 0.0001);
        assertEquals(0.2F, result.strain(), 0.0001);
        assertTrue(result.signature());
    }

    @Test
    void cooldownAndFailedFullPaymentHaveNoSideEffects() {
        var none = MuscleMemoryResonanceRules.Resonance.NONE;

        assertFalse(MuscleMemoryActivationRules.evaluate(true, false, 20.0,
                3.0, 0.25F, none).accepted());
        assertFalse(MuscleMemoryActivationRules.evaluate(true, true, 2.99,
                3.0, 0.25F, none).accepted());
    }

    @Test
    void armedUseCommitsOnlyAfterTheResolvedPaymentSucceeds() {
        assertFalse(MuscleMemoryActivationRules.shouldConsumeArmedUse(true, false));
        assertFalse(MuscleMemoryActivationRules.shouldConsumeArmedUse(false, true));
        assertTrue(MuscleMemoryActivationRules.shouldConsumeArmedUse(true, true));
    }
}
