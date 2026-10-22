package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Pure maths for the Stained Priestess's tendril wings: pose tables, pose blending and the curvature-wave
 * integrator that turns a heading into a living centre line. Units are model pixels in body space
 * (+Y down, -Z front, +X her left); the right wing mirrors X.
 */
public final class PriestessTendrilRules {
    public enum Pose { FOLDED, SPREAD, SHIELD }

    /** Heading in degrees: azimuth from +X toward +Z (her back), elevation upward. */
    public record Heading(float azimuth, float elevation) {}

    /** Travelling-wave writhe; amplitude 0 lays a straight line along the heading. */
    public record Wave(float amplitude, float speed, float phaseA, float phaseB, float droop) {}

    public static final float RENDER_SCALE = 1.2F;
    public static final int SEGMENTS = 18;
    public static final int FINGERS = 6;
    public static final float ARM_LENGTH = 26.0F;
    /** The wound between her shoulder blades each wing tears out of. */
    public static final float[] WING_ROOT = {2.0F, 2.5F, 2.4F};
    private static final float BLEND_TICKS = 6.0F;
    private static final float[] FINGER_LENGTH = {38.0F, 44.0F, 48.0F, 48.0F, 46.0F, 42.0F};
    private static final float[] FINGER_ROOT = {0.3F, 0.45F, 0.6F, 0.74F, 0.87F, 1.0F};

    // Per pose: arm azimuth, arm elevation, first and last finger azimuth, first and last finger elevation, reach.
    private static final float[][] POSES = {
            {80.0F, -30.0F, 100.0F, 85.0F, -80.0F, -55.0F, 0.7F},
            {15.0F, 25.0F, 55.0F, 0.0F, -65.0F, 15.0F, 1.0F},
            {-35.0F, 5.0F, -110.0F, -170.0F, -40.0F, 0.0F, 0.55F},
    };

    private PriestessTendrilRules() {}

    public static float[] weightsFor(Pose pose) {
        float[] weights = new float[3];
        weights[pose.ordinal()] = 1.0F;
        return weights;
    }

    /** Moves weight toward the target pose, never more than dt/6 per call, keeping the sum at one. */
    public static float[] stepWeights(float[] weights, Pose target, float dtTicks) {
        float[] next = weights.clone();
        int t = target.ordinal();
        float amount = Math.min(Math.max(0.0F, dtTicks) / BLEND_TICKS, 1.0F - next[t]);
        float others = 1.0F - next[t];
        if (amount <= 0.0F || others <= 0.0F) return next;
        for (int i = 0; i < next.length; i++) {
            if (i != t) next[i] -= amount * next[i] / others;
        }
        next[t] += amount;
        return next;
    }

    private static float blend(float[] weights, int column) {
        float value = 0.0F;
        for (int pose = 0; pose < POSES.length; pose++) value += weights[pose] * POSES[pose][column];
        return value;
    }

    public static Heading armHeading(float[] weights, float time, float beat) {
        return new Heading(blend(weights, 0), blend(weights, 1) + 16.0F * (float) Math.sin(time * 0.25F) * beat);
    }

    /** Fingers fan from the arm's root (down and back) to its tip; in a beat they lag the arm. */
    public static Heading fingerHeading(int finger, float[] weights, float time, float beat) {
        float f = finger / (FINGERS - 1.0F);
        float azimuth = HemoEasing.lerp(blend(weights, 2), blend(weights, 3), f);
        float elevation = HemoEasing.lerp(blend(weights, 4), blend(weights, 5), f);
        elevation += 16.0F * (float) Math.sin(time * 0.25F - 0.8F - 0.12F * finger) * beat;
        return new Heading(azimuth, elevation);
    }

    public static float fingerLength(int finger, float[] weights) {
        return FINGER_LENGTH[finger] * blend(weights, 6);
    }

    public static float fingerRoot(int finger) {
        return FINGER_ROOT[finger];
    }

    /** The centre-line point nearest the given progress (0 root, 1 tip). */
    public static float[] pointAt(float[] line, float progress) {
        int stations = line.length / 3;
        int index = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * (stations - 1)) * 3;
        return new float[]{line[index], line[index + 1], line[index + 2]};
    }

    /**
     * Walks out from the root, bending by travelling curvature waves whose strength grows toward the tip, so
     * the root stays pinned while the end whips. Droop bends the line toward the ground.
     */
    public static float[] integrate(float[] root, Heading base, float length, int segments, Wave wave, float time) {
        float[] out = new float[(segments + 1) * 3];
        float x = root[0], y = root[1], z = root[2];
        out[0] = x;
        out[1] = y;
        out[2] = z;
        double azimuth = Math.toRadians(base.azimuth());
        double elevation = Math.toRadians(base.elevation());
        float step = length / segments;
        float flow = time * wave.speed();
        for (int segment = 0; segment < segments; segment++) {
            float s = (segment + 0.5F) / segments;
            float envelope = 0.35F + 1.3F * s;
            double bendE = Math.sin(TAU * 1.3F * s - flow + wave.phaseA())
                    + 0.45D * Math.sin(TAU * 2.1F * s + flow * 0.7F + wave.phaseB());
            double bendA = Math.sin(TAU * 1.1F * s - flow * 0.8F + wave.phaseB())
                    + 0.4D * Math.cos(TAU * 2.4F * s + flow * 0.55F + wave.phaseA());
            elevation += (envelope * wave.amplitude() * 2.6D * bendE - wave.droop()) / segments;
            // Gravity can pull a tendril straight down but never hook it back on itself.
            elevation = Math.max(-HALF_PI, Math.min(HALF_PI, elevation));
            azimuth += envelope * wave.amplitude() * 2.2D * bendA / segments;
            double horizontal = Math.cos(elevation);
            x += (float) (horizontal * Math.cos(azimuth) * step);
            y += (float) (-Math.sin(elevation) * step);
            z += (float) (horizontal * Math.sin(azimuth) * step);
            out[(segment + 1) * 3] = x;
            out[(segment + 1) * 3 + 1] = y;
            out[(segment + 1) * 3 + 2] = z;
        }
        return out;
    }

    private static final float TAU = (float) (Math.PI * 2.0D);
    private static final double HALF_PI = Math.PI / 2.0D;
}
