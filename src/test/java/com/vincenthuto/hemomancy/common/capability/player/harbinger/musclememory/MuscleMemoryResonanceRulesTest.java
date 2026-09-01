package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MuscleMemoryResonanceRulesTest {
    @Test
    void preferredPrimaryAffinityUsesStrongestEfficiency() {
        var result = MuscleMemoryResonanceRules.resolve(MuscleMemory.LABORING_ARMS,
                EnumBloodTendency.FERRIC, EnumBloodTendency.MORTEM, false);

        assertEquals(MuscleMemoryResonanceRules.Tier.PREFERRED, result.tier());
        assertEquals(2.0 / 3.0, result.costMultiplier(), 0.0001);
        assertEquals(2.0 / 3.0, result.strainMultiplier(), 0.0001);
    }

    @Test
    void crossAffinityUsesSecondaryEfficiencyAndDoesNotStack() {
        var result = MuscleMemoryResonanceRules.resolve(MuscleMemory.COURSING_LEGS,
                EnumBloodTendency.FLAMMEUS, EnumBloodTendency.DUCTILIS, true);

        assertEquals(MuscleMemoryResonanceRules.Tier.SECONDARY, result.tier());
        assertEquals(0.8, result.costMultiplier(), 0.0001);
        assertTrue(result.signature());
    }

    @Test
    void unrelatedMorphlingProvidesNoEfficiency() {
        var result = MuscleMemoryResonanceRules.resolve(MuscleMemory.PREDATORY_EYES,
                EnumBloodTendency.FERRIC, EnumBloodTendency.CONGEATIO, false);

        assertEquals(MuscleMemoryResonanceRules.Tier.NONE, result.tier());
        assertEquals(1.0, result.costMultiplier());
    }
}
