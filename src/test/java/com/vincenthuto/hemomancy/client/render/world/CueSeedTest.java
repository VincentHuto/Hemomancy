package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CueSeedTest {
    private static final Vec3 FROM = new Vec3(10, 70, -4);

    @Test void zeroSaltKeepsTheSourceSeed() {
        assertEquals(42, CueSeed.of(new ManipulationVisualPacket(Form.TELEPORT, 42, FROM, FROM, 1, 20, 1), 0));
        assertEquals(FROM.hashCode(), CueSeed.of(new ManipulationVisualPacket(Form.TELEPORT, -1, FROM, FROM, 1, 20, 1), 0));
    }

    @Test void differentSaltsGiveDifferentSeedsForOnePacket() {
        for (int entityId : new int[] { 42, -1 }) {
            var packet = new ManipulationVisualPacket(Form.TELEPORT, entityId, FROM, FROM, 1, 20, 1);
            Set<Integer> seeds = new HashSet<>();
            for (long salt = 1; salt <= 64; salt++) seeds.add(CueSeed.of(packet, salt));
            assertEquals(64, seeds.size());
            assertNotEquals(CueSeed.of(packet, 0), CueSeed.of(packet, 1));
        }
    }
}
