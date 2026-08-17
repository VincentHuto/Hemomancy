package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

public final class MuscleMemoryActivationRules {
    private MuscleMemoryActivationRules() {
    }

    public static Result evaluate(boolean primed, boolean cooldownReady, double availableBlood,
            double baseCost, float baseStrain, MuscleMemoryResonanceRules.Resonance resonance) {
        if (!primed || !cooldownReady) return Result.REJECTED;
        double cost = baseCost * resonance.costMultiplier();
        float strain = (float) (baseStrain * resonance.strainMultiplier());
        if (availableBlood + 0.000001 < cost) return Result.REJECTED;
        return new Result(true, cost, strain, resonance.signature());
    }

    public static boolean shouldConsumeArmedUse(boolean armed, boolean paymentCommitted) {
        return armed && paymentCommitted;
    }

    public record Result(boolean accepted, double bloodCost, float strain, boolean signature) {
        public static final Result REJECTED = new Result(false, 0.0, 0.0F, false);
    }
}
