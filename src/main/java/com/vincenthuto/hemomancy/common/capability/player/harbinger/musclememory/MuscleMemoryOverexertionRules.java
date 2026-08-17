package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

public final class MuscleMemoryOverexertionRules {
    private MuscleMemoryOverexertionRules() {}

    public static Payment resolve(boolean armed, double availableBlood, double normalBloodCost,
            float normalStrain) {
        if (!armed) return new Payment(normalBloodCost, normalStrain, false, false);
        double enhancedCost = normalBloodCost * MuscleMemoryPrimingRules.OVEREXERT_BLOOD_MULTIPLIER;
        if (availableBlood + .000001D >= enhancedCost) {
            return new Payment(enhancedCost,
                    normalStrain * MuscleMemoryPrimingRules.OVEREXERT_STRAIN_MULTIPLIER,
                    true, false);
        }
        return new Payment(normalBloodCost, normalStrain, false, true);
    }

    public record Payment(double bloodCost, float strain, boolean overexerted, boolean fellBack) {}
}
