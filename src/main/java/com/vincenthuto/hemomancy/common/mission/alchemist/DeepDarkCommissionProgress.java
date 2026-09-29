package com.vincenthuto.hemomancy.common.mission.alchemist;

public record DeepDarkCommissionProgress(boolean sampleProof, boolean reported) {
    public boolean ready() {
        return sampleProof && !reported;
    }
}
