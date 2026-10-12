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

class DeadlyGazeGeometryTest {
    @Test void everyPhaseStaysFiniteQuadAlignedAndWithinBudget() {
        for (boolean firstPerson : new boolean[] { false, true })
            for (float progress : new float[] { 0, .3f, .7f, 1 }) {
                var nerve = new HematicRiposteGeometryTest.Capture();
                var ink = new HematicRiposteGeometryTest.Capture();
                draw(new ManipulationVisualPacket(Form.GAZE_CHARGE, 7, Vec3.ZERO, new Vec3(1, .5, 14), progress, 40, 1),
                        nerve, ink, Vec3.ZERO, 0, firstPerson);
                check(nerve, ink, 900);
            }
        for (int count : new int[] { 1, 2 })
            for (float age = 0; age < DeadlyGazeGeometry.GRIP_TICKS + 2; age += 1.5f) {
                var nerve = new HematicRiposteGeometryTest.Capture();
                var ink = new HematicRiposteGeometryTest.Capture();
                Vec3 feet = new Vec3(5, 64, 9);
                draw(new ManipulationVisualPacket(Form.GAZE_GRIP, 9, feet, feet.add(-6, 1.6, -8), .8f, 14, count),
                        nerve, ink, feet.add(0, age * .1, 0), age, false);
                check(nerve, ink, 900);
            }
        for (float age = 0; age < DeadlyGazeGeometry.SLAM_TICKS; age += 1.5f) {
            var nerve = new HematicRiposteGeometryTest.Capture();
            var ink = new HematicRiposteGeometryTest.Capture();
            Vec3 at = new Vec3(5, 64, 9);
            draw(new ManipulationVisualPacket(Form.GAZE_SLAM, -1, at, at, 1, 16, 1), nerve, ink, at, age, false);
            check(nerve, ink, 600);
        }
    }

    @Test void theEyeOpensWithTheCharge() {
        assertEquals(0, DeadlyGazeGeometry.opening(0), 1e-9);
        assertEquals(1, DeadlyGazeGeometry.opening(1), 1e-9);
        for (double p = .1; p <= 1; p += .1)
            assertTrue(DeadlyGazeGeometry.opening(p) >= DeadlyGazeGeometry.opening(p - .1));
    }

    @Test void tendrilsRiseFromTheFeetToAKnotAboveTheHead() {
        double height = 1.9, width = .6;
        for (int strand = 0; strand < 5; strand++) {
            Vec3 foot = DeadlyGazeGeometry.tendril(strand, 5, 0, height, width, 1, 0);
            Vec3 knot = DeadlyGazeGeometry.tendril(strand, 5, 1, height, width, 1, 0);
            assertEquals(0, foot.y, 1e-9);
            assertTrue(knot.y > height, "the knot should sit above the head");
            assertTrue(Math.hypot(knot.x, knot.z) < Math.hypot(foot.x, foot.z), "tendrils should close in as they rise");
            assertTrue(Math.hypot(foot.x, foot.z) <= width, "tendrils should hug the body");
        }
    }

    @Test void onlyGazeFormsAreClaimedAndTheFlameLiftIsGone() throws IOException {
        for (Form form : new Form[] { Form.GAZE_CHARGE, Form.GAZE_GRIP, Form.GAZE_SLAM }) {
            assertTrue(DeadlyGazeGeometry.handles(form));
            assertEquals(ManipulationMaterials.DUCTILIS, ManipulationMaterials.forForm(form));
        }
        assertFalse(DeadlyGazeGeometry.handles(Form.UPDRAFT));
        String manip = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/DeadlyGazeManip.java"));
        assertFalse(manip.contains("Form.UPDRAFT"), "a Ductilis/Tenebris gaze must not lift on Flammeus flame");
        assertFalse(manip.contains("lightningFX"), "server-side proxy lightning only ever rendered in singleplayer");
        assertTrue(manip.contains("Form.GAZE_GRIP, eyes, charge, 14, slams ? 2 : 1"));
        String reactive = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationReactiveEvents.java"));
        assertTrue(reactive.contains("ManipulationVisuals.Form.GAZE_SLAM,living.position()"));
        String renderer = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationVisualRenderer.java"));
        int gaze = renderer.indexOf("if(DeadlyGazeGeometry.handles(packet.form()))");
        assertTrue(gaze > 0 && gaze < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
    }

    private static void draw(ManipulationVisualPacket packet, HematicRiposteGeometryTest.Capture nerve,
            HematicRiposteGeometryTest.Capture ink, Vec3 origin, float age, boolean firstPerson) {
        DeadlyGazeGeometry.draw(packet, new PoseStack(), nerve, ink, new Vec3(2, 3, -5), origin, new Vec3(1, 0, 0),
                new Vec3(0, 1, 0), 200 + age, age, 1, null, firstPerson);
    }

    private static void check(HematicRiposteGeometryTest.Capture nerve, HematicRiposteGeometryTest.Capture ink, int budget) {
        assertFalse(nerve.points.isEmpty() && ink.points.isEmpty());
        for (var mesh : new HematicRiposteGeometryTest.Capture[] { nerve, ink }) {
            assertEquals(0, mesh.points.size() % 4);
            assertTrue(mesh.points.size() <= budget, "exceeded budget: " + mesh.points.size());
            assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())));
        }
    }
}
