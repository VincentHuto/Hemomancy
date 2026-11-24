package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.world.phys.Vec3;

public final class BombardierFlameGeometry {
    public static final float RAISED_ABDOMEN_DEGREES = 50;
    public static final double SWEEP_DEGREES = 50;
    private static final double VALVE_LENGTH = 3.25 / 16;
    private static final double VISUAL_SAMPLE_EXPONENT = 2.2;

    private BombardierFlameGeometry() {}

    public static Vec3 sweepDirection(Vec3 lockedDirection, int flameTick, int flameDuration, double sweepDegrees) {
        return sweepDirection(lockedDirection, (double) flameTick, flameDuration, sweepDegrees);
    }

    public static Vec3 raisedValvePivot(Vec3 feet, float bodyYaw) {
        // Blockbench: abdomen pivot (0, 5, -.75), valve pivot (0, 4.5, 11.25).
        double angle = Math.toRadians(RAISED_ABDOMEN_DEGREES);
        double up = (5 - .5 * Math.cos(angle) + 12 * Math.sin(angle)) / 16 + .001;
        double back = (-.75 + .5 * Math.sin(angle) + 12 * Math.cos(angle)) / 16;
        double yaw = Math.toRadians(bodyYaw);
        return feet.add(Math.sin(yaw) * back, up, -Math.cos(yaw) * back);
    }

    public static Vec3 valveMouth(Vec3 pivot, Vec3 direction) {
        return pivot.add(direction.scale(VALVE_LENGTH));
    }

    public static Vec3 sweepDirection(Vec3 lockedDirection, double flameTick, int flameDuration, double sweepDegrees) {
        if (!finite(lockedDirection) || lockedDirection.lengthSqr() < 1.0E-12 || flameDuration < 2
                || !Double.isFinite(flameTick) || !Double.isFinite(sweepDegrees)) return Vec3.ZERO;
        double progress = Math.max(0, Math.min(flameDuration - 1, flameTick)) / (flameDuration - 1.0);
        double angle = Math.toRadians(-sweepDegrees / 2.0 + sweepDegrees * progress);
        Vec3 normalized = lockedDirection.normalize();
        double sin = Math.sin(angle), cos = Math.cos(angle);
        return new Vec3(normalized.x * cos + normalized.z * sin, normalized.y,
                normalized.z * cos - normalized.x * sin).normalize();
    }

    public static boolean insideCone(Vec3 nozzle, Vec3 direction, Vec3 targetCenter,
                                     double maxRange, double halfAngleDegrees) {
        if (!finite(nozzle) || !finite(direction) || !finite(targetCenter)
                || !Double.isFinite(maxRange) || !Double.isFinite(halfAngleDegrees)
                || maxRange <= 0 || direction.lengthSqr() < 1.0E-12) return false;
        Vec3 offset = targetCenter.subtract(nozzle);
        double distance = offset.lengthSqr();
        if (distance < 1.0E-12 || distance > maxRange * maxRange) return false;
        double minimumDot = Math.cos(Math.toRadians(halfAngleDegrees));
        return offset.normalize().dot(direction.normalize()) >= minimumDot;
    }

    public static int visualSampleCount(double length) {
        if (!Double.isFinite(length) || length <= 0) return 0;
        return Math.max(8, (int) Math.ceil(length * 2.4));
    }

    public static double visualSampleDistance(double length, int index, int count) {
        if (!Double.isFinite(length) || length <= 0 || count <= 0) return 0;
        double progress = Math.max(0.0, Math.min(1.0, (index + .35) / count));
        return length * Math.pow(progress, VISUAL_SAMPLE_EXPONENT);
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }
}
