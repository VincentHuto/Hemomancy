package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public final class MuscleMemoryResonanceRules {
    public enum Tier { NONE, SECONDARY, PREFERRED }

    public record Resonance(Tier tier, double costMultiplier, double strainMultiplier, boolean signature) {
        public static final Resonance NONE = new Resonance(Tier.NONE, 1.0, 1.0, false);
    }

    private MuscleMemoryResonanceRules() {
    }

    public static Resonance resolve(MuscleMemory memory, EnumBloodTendency preferred,
            EnumBloodTendency secondary, boolean signature) {
        if (preferred == memory.primaryTendency()) {
            return new Resonance(Tier.PREFERRED, 2.0 / 3.0, 2.0 / 3.0, signature);
        }
        boolean crossMatch = preferred == memory.secondaryTendency()
                || secondary == memory.primaryTendency()
                || secondary == memory.secondaryTendency();
        return crossMatch
                ? new Resonance(Tier.SECONDARY, 0.8, 0.8, signature)
                : new Resonance(Tier.NONE, 1.0, 1.0, signature);
    }
}
