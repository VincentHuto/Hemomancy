package com.vincenthuto.hemomancy.common.network;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethonticCurrentPacketTest {
    @Test void currentPreservesJumpFlightAndFallVelocity() {
        var packet = new PhlegethonticCurrentPacket(.014, 0);
        for (double vertical : new double[]{.42, .15, -.08, -2}) {
            Vec3 result = packet.apply(new Vec3(.1, vertical, 0));
            assertEquals(vertical, result.y);
            assertEquals(.114, result.x, 1e-12);
        }
    }

    @Test void currentCapsHorizontalSpeedWithoutReducingVerticalSpeed() {
        Vec3 result = new PhlegethonticCurrentPacket(.014, .014).apply(new Vec3(.299, .42, 0));
        assertEquals(.3, result.horizontalDistance(), 1e-12);
        assertEquals(.42, result.y);
    }

    @Test void currentDoesNotSlowAnAlreadyFasterPlayer() {
        Vec3 velocity = new Vec3(.4, .15, .1);
        assertEquals(velocity, new PhlegethonticCurrentPacket(.014, 0).apply(velocity));
    }
}
