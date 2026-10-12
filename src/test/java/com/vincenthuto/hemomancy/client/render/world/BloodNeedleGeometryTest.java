package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BloodNeedleGeometryTest {
    @Test void flightAndLodgedNeedlesStayBounded() {
        for (boolean lance : new boolean[] { false, true })
            for (boolean lodged : new boolean[] { false, true }) {
                var mesh = new HematicRiposteGeometryTest.Capture();
                List<Vec3> path = new ArrayList<>();
                for (int i = 0; i < 10; i++) path.add(new Vec3(i * .6, 3 - i * i * .02, 0));
                BloodNeedleGeometry.flight(new PoseStack(), mesh, path, lodged ? Vec3.ZERO : new Vec3(2, -.3, 0),
                        new Vec3(2, 5, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 120, 6, 31, 1, 1, lance, lodged, lodged ? 5 : 0);
                assertFalse(mesh.points.isEmpty());
                assertEquals(0, mesh.points.size() % 4);
                assertTrue(mesh.points.size() <= 200, "a single needle must stay cheap: " + mesh.points.size());
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())));
            }
    }

    @Test void needlePuncturesSpurtForward() {
        Vec3 east = new Vec3(1, 0, 0);
        for (int i = 0; i < 5; i++) {
            Vec3 at = BloodShotGeometry.droplet(BloodShotGeometry.NEEDLE, i, 9, 3, east, BloodNeedleGeometry.PUNCTURE);
            assertTrue(at.x > Math.abs(at.z), "puncture droplet " + i + " left the jet: " + at);
        }
    }
}
