package com.vincenthuto.hemomancy.common.mission.alchemist;

public record PhlegethonticCommissionProgress(boolean sampleProof, int scyphusCount, boolean reported) {
    public boolean ready() {
        return sampleProof && scyphusCount >= 5 && !reported;
    }

    public boolean complete() {
        return reported;
    }
}
