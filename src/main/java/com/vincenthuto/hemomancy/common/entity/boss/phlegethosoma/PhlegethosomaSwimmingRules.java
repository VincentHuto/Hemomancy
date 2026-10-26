package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

/** Travelling lateral muscle wave for the long, soft-bodied ichor swimmer. */
public final class PhlegethosomaSwimmingRules {
    public static final int SEGMENTS = 12;
    private PhlegethosomaSwimmingRules() {}

    public static float segmentYaw(int segment, float time, float movement) {
        float strength = Math.clamp(movement, 0, 1);
        double phase = time * (.12 + .36 * strength) - segment * .62;
        return (float) Math.sin(phase) * (.018F + .15F * strength) * (.45F + segment / 18F);
    }

    public static double speed(int phase) {
        return phase >= 3 ? .5 : .38;
    }
}
