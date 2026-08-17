package com.vincenthuto.hemomancy.common.mission;

public final class NoeticDiscoveryRules {
    private NoeticDiscoveryRules() {
    }

    public static boolean canRecognizeConductiveMark(int degree, boolean hasDuctilisRecord,
            boolean completedFirstWeave) {
        return degree >= 3 && (hasDuctilisRecord || completedFirstWeave);
    }
}
