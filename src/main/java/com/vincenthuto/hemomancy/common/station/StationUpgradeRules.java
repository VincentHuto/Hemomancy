package com.vincenthuto.hemomancy.common.station;

public final class StationUpgradeRules {
    public enum ClaimState { READY, CLAIMED, INACTIVE_BLOOD, DEGREE_TOO_LOW, PREVIOUS_TIER_INELIGIBLE, USAGE_INCOMPLETE }

    private StationUpgradeRules() {}

    public static ClaimState claimState(int degree, boolean activeBlood, boolean alreadyClaimed,
            boolean previousTierEligible, boolean usageComplete, int requiredDegree) {
        if (alreadyClaimed) return ClaimState.CLAIMED;
        if (!activeBlood) return ClaimState.INACTIVE_BLOOD;
        if (degree < requiredDegree) return ClaimState.DEGREE_TOO_LOW;
        if (!previousTierEligible) return ClaimState.PREVIOUS_TIER_INELIGIBLE;
        if (!usageComplete) return ClaimState.USAGE_INCOMPLETE;
        return ClaimState.READY;
    }

    public static boolean isNextTier(int currentTier, int targetTier) {
        return targetTier == currentTier + 1 && targetTier <= 2;
    }
}
