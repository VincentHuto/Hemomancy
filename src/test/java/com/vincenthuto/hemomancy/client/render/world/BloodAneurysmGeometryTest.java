package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BloodAneurysmGeometryTest {
    @Test void chargeAndRuptureStayFiniteQuadAlignedAndWithinBudget() {
        for (float progress : new float[] { 0, .4f, .8f, 1 }) {
            var mesh = draw(new ManipulationVisualPacket(Form.ANEURYSM_CHARGE, 3, Vec3.ZERO, new Vec3(0, 0, 9), progress, 40, 1), 0);
            check(mesh, 700, 3);
        }
        Vec3 target = new Vec3(4, 65, 2);
        for (float age = 0; age < BloodAneurysmGeometry.RUPTURE_TICKS - 1; age += 1.3f) {
            var mesh = draw(new ManipulationVisualPacket(Form.ANEURYSM_RUPTURE, -1, target, target.add(-6, 0, -5), 4, 28, 1), age);
            check(mesh, 1400, 16);
        }
    }

    @Test void theKnotBeatsFasterAsItNearsBursting() {
        assertTrue(BloodAneurysmGeometry.beatRate(1) > BloodAneurysmGeometry.beatRate(.5));
        assertTrue(BloodAneurysmGeometry.beatRate(.5) > BloodAneurysmGeometry.beatRate(0));
    }

    @Test void sprayHoldsUntilTheTearThenFliesOutAndFalls() {
        for (int i = 0; i < 18; i++) {
            assertEquals(0, BloodAneurysmGeometry.spray(i, 5, 2, 4).length(), 1e-12, "nothing leaves before the sac tears");
            Vec3 early = BloodAneurysmGeometry.spray(i, 5, 8, 4), late = BloodAneurysmGeometry.spray(i, 5, 26, 4);
            assertTrue(early.length() > .3, "droplet " + i + " should fly out");
            assertTrue(late.y < early.y, "droplet " + i + " should fall");
        }
    }

    @Test void aneurysmMovedOffTheSharedTierAndDropsVanillaDust() throws IOException {
        assertTrue(BloodAneurysmGeometry.handles(Form.ANEURYSM_CHARGE) && BloodAneurysmGeometry.handles(Form.ANEURYSM_RUPTURE));
        assertFalse(AnimusMortemGeometry.handles(Form.ANEURYSM_CHARGE));
        assertEquals(ManipulationMaterials.ANIMUS, ManipulationMaterials.forForm(Form.ANEURYSM_RUPTURE));
        String manip = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodAneurysmManip.java"));
        assertFalse(manip.contains("DustParticleOptions"), "the custom rupture replaces the vanilla dust cloud");
        assertTrue(manip.contains("Form.ANEURYSM_RUPTURE, tPos,"));
    }

    private static HematicRiposteGeometryTest.Capture[] draw(ManipulationVisualPacket packet, float age) {
        var blood = new HematicRiposteGeometryTest.Capture();
        var rot = new HematicRiposteGeometryTest.Capture();
        BloodAneurysmGeometry.draw(packet, new PoseStack(), blood, rot, new Vec3(2, 3, -6), new Vec3(1, 0, 0),
                new Vec3(0, 1, 0), 400 + age, age, 1);
        return new HematicRiposteGeometryTest.Capture[] { blood, rot };
    }

    private static void check(HematicRiposteGeometryTest.Capture[] mesh, int budget, double bound) {
        assertFalse(mesh[0].points.isEmpty() && mesh[1].points.isEmpty());
        for (var capture : mesh) {
            assertEquals(0, capture.points.size() % 4);
            assertTrue(capture.points.size() <= budget, "exceeded budget: " + capture.points.size());
            assertTrue(capture.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < bound));
        }
    }
}
