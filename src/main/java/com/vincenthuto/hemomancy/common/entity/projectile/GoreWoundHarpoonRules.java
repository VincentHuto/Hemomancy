package com.vincenthuto.hemomancy.common.entity.projectile;

public final class GoreWoundHarpoonRules {
    public static final double HOMING_BLEND = 0.08D;

    private GoreWoundHarpoonRules() {}

    public static boolean shouldReturn(boolean livingCrossbow, boolean creative, boolean multishotSide) {
        return livingCrossbow && !creative && !multishotSide;
    }

    public static boolean shouldDropOnKill(boolean livingCrossbow, float roll) {
        return !livingCrossbow && roll < 0.10F;
    }

    public static boolean canHome(double distance, double directionDot) {
        return distance <= 12.0D && directionDot >= 0.94D;
    }
}
