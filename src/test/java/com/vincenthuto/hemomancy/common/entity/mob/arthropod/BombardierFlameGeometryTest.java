package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BombardierFlameGeometryTest {
    @Test void sweepCoversFiftyDegreesAndCrossesLockedAim() {
        Vec3 forward = new Vec3(0, 0, 1);
        assertEquals(-25.0, yaw(BombardierFlameGeometry.sweepDirection(forward, 0, 16, 50)), 0.001);
        assertEquals(0.0, yaw(BombardierFlameGeometry.sweepDirection(forward, 7.5, 16, 50)), 0.001);
        assertEquals(25.0, yaw(BombardierFlameGeometry.sweepDirection(forward, 15, 16, 50)), 0.001);
    }

    @Test void coneRejectsRearRangeAngleAndInvalidVectors() {
        Vec3 nozzle = Vec3.ZERO;
        Vec3 forward = new Vec3(0, 0, 1);
        assertTrue(BombardierFlameGeometry.insideCone(nozzle, forward, new Vec3(1, 0, 8), 10, 12));
        assertFalse(BombardierFlameGeometry.insideCone(nozzle, forward, new Vec3(0, 0, -2), 10, 12));
        assertFalse(BombardierFlameGeometry.insideCone(nozzle, forward, new Vec3(0, 0, 10.01), 10, 12));
        assertFalse(BombardierFlameGeometry.insideCone(nozzle, forward, new Vec3(3, 0, 8), 10, 12));
        assertFalse(BombardierFlameGeometry.insideCone(nozzle, Vec3.ZERO, forward, 10, 12));
        assertFalse(BombardierFlameGeometry.insideCone(nozzle, forward,
                new Vec3(Double.NaN, 0, 1), 10, 12));
    }

    private static double yaw(Vec3 direction) {
        return Math.toDegrees(Math.atan2(direction.x, direction.z));
    }
}
