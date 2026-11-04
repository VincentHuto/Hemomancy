package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThelemicMemoryOverexertionRulesTest {
    @Test
    void armedTriggerUsesEnhancedPaymentWhenAvailable() {
        var result = ThelemicMemoryOverexertionRules.resolve(true, 20.0D, 3.0D, .2F);

        assertTrue(result.overexerted());
        assertEquals(6.0D, result.bloodCost(), .0001D);
        assertEquals(.5F, result.strain(), .0001F);
    }

    @Test
    void armedTriggerFallsBackToNormalPaymentWhenEnhancedCostCannotBePaid() {
        var result = ThelemicMemoryOverexertionRules.resolve(true, 5.0D, 3.0D, .2F);

        assertFalse(result.overexerted());
        assertTrue(result.fellBack());
        assertEquals(3.0D, result.bloodCost(), .0001D);
        assertEquals(.2F, result.strain(), .0001F);
    }

    @Test
    void unarmedTriggerNeverReportsFallback() {
        var result = ThelemicMemoryOverexertionRules.resolve(false, 20.0D, 3.0D, .2F);

        assertFalse(result.overexerted());
        assertFalse(result.fellBack());
    }
}
