package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class HeartyCompassRules {
    private HeartyCompassRules() {
    }

    public static float relativeNeedleDegrees(double playerX, double playerZ, float yawDegrees,
                                              double targetX, double targetZ) {
        double dx = targetX - playerX;
        double dz = targetZ - playerZ;
        double distanceSqr = dx * dx + dz * dz;
        if (distanceSqr < 0.0001D) {
            return 0.0F;
        }

        double length = Math.sqrt(distanceSqr);
        double nx = dx / length;
        double nz = dz / length;
        double yaw = Math.toRadians(yawDegrees);
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double rightX = -forwardZ;
        double rightZ = forwardX;
        double forward = nx * forwardX + nz * forwardZ;
        double right = nx * rightX + nz * rightZ;
        return (float) Math.toDegrees(Math.atan2(right, forward));
    }
}
