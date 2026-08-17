package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

public final class MuscleMemoryPowerRules {
    private static final double COURSING_STRIDE = 12D;

    private MuscleMemoryPowerRules() {}

    public static boolean laboringEligible(boolean creative, float hardness, boolean correctTool) {
        return !creative && hardness >= 1F && correctTool;
    }

    public static DistanceResult coursingDistance(double stored, double traveled) {
        double total = Math.max(0D, stored) + Math.max(0D, traveled);
        int triggers = (int) Math.floor(total / COURSING_STRIDE);
        return new DistanceResult(triggers, total - triggers * COURSING_STRIDE);
    }

    public static boolean hushedEligible(int brightness, boolean sprinting, boolean moving) {
        return moving && !sprinting && brightness <= 4;
    }

    public static float secondPulseReduction(float health, float maxHealth, float incomingDamage) {
        if (maxHealth <= 0F || health <= maxHealth * .3F || health - incomingDamage > maxHealth * .3F) return 0F;
        return Math.min(6F, incomingDamage * .35F);
    }

    public static boolean enduringEligible(int beforeFood, int afterFood, float beforeSaturation, float afterSaturation) {
        return beforeFood - afterFood == 1 && beforeSaturation <= 0F && afterSaturation <= 0F;
    }

    public record DistanceResult(int triggers, double remainder) {}
}
