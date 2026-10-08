package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

/** Per-animal movement heading and tilt for upright-bodied swimmers. */
public final class PelagicSwimmingPose {
    private float previousHeading, heading, previousLean, lean;

    public void tick(double dx, double dy, double dz, boolean swimming) {
        previousHeading = heading;
        previousLean = lean;
        double horizontal = Math.hypot(dx, dz);
        float targetLean = 0;
        if (swimming && Math.hypot(horizontal, dy) > .0025) {
            if (horizontal > .00001) {
                float targetHeading = (float) Math.toDegrees(Math.atan2(-dx, dz));
                heading = wrap(heading + clamp(wrap(targetHeading-heading), -12, 12));
            }
            // These rigs face upward at rest, so horizontal travel needs a 90-degree lean.
            targetLean = (float) Math.toDegrees(Math.atan2(horizontal, dy));
        }
        lean += clamp(targetLean-lean, -8, 8);
    }

    public float headingDegrees(float partialTick) {
        return previousHeading + wrap(heading-previousHeading) * clamp(partialTick, 0, 1);
    }

    public float leanRadians(float partialTick) {
        return (float) Math.toRadians(previousLean + (lean-previousLean) * clamp(partialTick, 0, 1));
    }

    private static float wrap(float degrees) { return (degrees%360+540)%360-180; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
}
