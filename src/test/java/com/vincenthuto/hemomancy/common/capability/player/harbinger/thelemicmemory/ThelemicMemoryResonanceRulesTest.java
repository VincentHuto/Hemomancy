package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThelemicMemoryResonanceRulesTest {
    @Test
    void preferredPrimaryAffinityUsesStrongestEfficiency() {
        var result = ThelemicMemoryResonanceRules.resolve(ThelemicMemory.LABORING_ARMS,
                EnumBloodTendency.FERRIC, EnumBloodTendency.MORTEM, false);

        assertEquals(ThelemicMemoryResonanceRules.Tier.PREFERRED, result.tier());
        assertEquals(2.0 / 3.0, result.costMultiplier(), 0.0001);
        assertEquals(2.0 / 3.0, result.strainMultiplier(), 0.0001);
    }

    @Test
    void crossAffinityUsesSecondaryEfficiencyAndDoesNotStack() {
        var result = ThelemicMemoryResonanceRules.resolve(ThelemicMemory.COURSING_LEGS,
                EnumBloodTendency.FLAMMEUS, EnumBloodTendency.DUCTILIS, true);

        assertEquals(ThelemicMemoryResonanceRules.Tier.SECONDARY, result.tier());
        assertEquals(0.8, result.costMultiplier(), 0.0001);
        assertTrue(result.signature());
    }

    @Test
    void unrelatedMorphlingProvidesNoEfficiency() {
        var result = ThelemicMemoryResonanceRules.resolve(ThelemicMemory.PREDATORY_EYES,
                EnumBloodTendency.FERRIC, EnumBloodTendency.CONGEATIO, false);

        assertEquals(ThelemicMemoryResonanceRules.Tier.NONE, result.tier());
        assertEquals(1.0, result.costMultiplier());
    }
}
