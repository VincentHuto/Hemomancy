package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

public final class ThelemicMemoryResonanceRules {
    public enum Tier { NONE, SECONDARY, PREFERRED }

    public record Resonance(Tier tier, double costMultiplier, double strainMultiplier, boolean signature) {
        public static final Resonance NONE = new Resonance(Tier.NONE, 1.0, 1.0, false);
    }

    private ThelemicMemoryResonanceRules() {
    }

    public static Resonance resolve(ThelemicMemory memory, EnumBloodTemper preferred,
            EnumBloodTemper secondary, boolean signature) {
        if (preferred == memory.primaryTemper()) {
            return new Resonance(Tier.PREFERRED, 2.0 / 3.0, 2.0 / 3.0, signature);
        }
        boolean crossMatch = preferred == memory.secondaryTemper()
                || secondary == memory.primaryTemper()
                || secondary == memory.secondaryTemper();
        return crossMatch
                ? new Resonance(Tier.SECONDARY, 0.8, 0.8, signature)
                : new Resonance(Tier.NONE, 1.0, 1.0, signature);
    }
}
