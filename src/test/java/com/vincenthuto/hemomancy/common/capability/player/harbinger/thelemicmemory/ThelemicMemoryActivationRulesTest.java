package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThelemicMemoryActivationRulesTest {
    @Test
    void acceptedTriggerAppliesResonanceToCostAndStrain() {
        var resonance = new ThelemicMemoryResonanceRules.Resonance(
                ThelemicMemoryResonanceRules.Tier.PREFERRED, 2.0 / 3.0, 2.0 / 3.0, true);

        var result = ThelemicMemoryActivationRules.evaluate(true, true, 20.0,
                3.0, 0.3F, resonance);

        assertTrue(result.accepted());
        assertEquals(2.0, result.bloodCost(), 0.0001);
        assertEquals(0.2F, result.strain(), 0.0001);
        assertTrue(result.signature());
    }

    @Test
    void cooldownAndFailedFullPaymentHaveNoSideEffects() {
        var none = ThelemicMemoryResonanceRules.Resonance.NONE;

        assertFalse(ThelemicMemoryActivationRules.evaluate(true, false, 20.0,
                3.0, 0.25F, none).accepted());
        assertFalse(ThelemicMemoryActivationRules.evaluate(true, true, 2.99,
                3.0, 0.25F, none).accepted());
    }

    @Test
    void armedUseCommitsOnlyAfterTheResolvedPaymentSucceeds() {
        assertFalse(ThelemicMemoryActivationRules.shouldConsumeArmedUse(true, false));
        assertFalse(ThelemicMemoryActivationRules.shouldConsumeArmedUse(false, true));
        assertTrue(ThelemicMemoryActivationRules.shouldConsumeArmedUse(true, true));
    }
}
