package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BloodShotGeometryTest {
    private static final Vec3 EAST = new Vec3(1, 0, 0);

    @Test void flightStaysFiniteQuadAlignedAndWithinBudget() {
        for (int form = 0; form <= 3; form++)
            for (float age : new float[] { 0, 1, 2.5f, 8, 40 })
                for (boolean flying : new boolean[] { true, false }) {
                    var mesh = flight(form, age, flying);
                    assertFalse(mesh.points.isEmpty(), "form " + form);
                    assertEquals(0, mesh.points.size() % 4, "form " + form);
                    assertTrue(mesh.points.size() <= 900, "form " + form + " exceeded budget: " + mesh.points.size());
                    assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())), "form " + form);
                }
    }

    @Test void lodgedShotsDropTheirBodyButKeepTheWake() {
        assertTrue(flight(0, 8, false).points.size() < flight(0, 8, true).points.size());
    }

    @Test void splashStaysFiniteQuadAlignedAndBounded() {
        for (int variant : new int[] { BloodShotGeometry.ENTITY, BloodShotGeometry.BLOCK, BloodShotGeometry.MORTAR })
            for (float age = 0; age < BloodShotGeometry.SPLASH_TICKS; age += .7f) {
                double radius = variant == BloodShotGeometry.MORTAR ? 2 : BloodShotGeometry.splashRadius(.13);
                var mesh = splash(variant, (float) radius, age);
                assertEquals(0, mesh.points.size() % 4);
                assertTrue(mesh.points.size() <= 1200, "variant " + variant + " exceeded budget: " + mesh.points.size());
                // Spray scales with the radius; the fall is gravity in blocks and does not.
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < radius * 2.5 + 4),
                        "variant " + variant + " escaped its radius at age " + age);
            }
        assertFalse(splash(BloodShotGeometry.ENTITY, 1, 1).points.isEmpty());
    }

    @Test void entityHitsSprayThroughAndBlockHitsSplashBack() {
        int through = 0, back = 0;
        for (int i = 0; i < 9; i++) {
            if (BloodShotGeometry.droplet(BloodShotGeometry.ENTITY, i, 17, 4, EAST, 1).x > 0) through++;
            if (BloodShotGeometry.droplet(BloodShotGeometry.BLOCK, i, 17, 4, EAST, 1).x < 0) back++;
        }
        assertEquals(9, through, "an entity hit should carry on through the body");
        assertEquals(9, back, "a block hit should come back out of the surface");
    }

    @Test void mortarBurstsUpwardThenEverythingFalls() {
        for (int i = 0; i < 18; i++) {
            Vec3 early = BloodShotGeometry.droplet(BloodShotGeometry.MORTAR, i, 5, 3, new Vec3(0, -1, 0), 2);
            Vec3 late = BloodShotGeometry.droplet(BloodShotGeometry.MORTAR, i, 5, 15, new Vec3(0, -1, 0), 2);
            assertTrue(early.y > 0, "mortar droplet " + i + " should rise first");
            assertTrue(late.y < early.y, "mortar droplet " + i + " should fall");
        }
    }

    @Test void drawnWakesStopAFewBlocksBehindTheHead() {
        List<Vec3> path = new ArrayList<>();
        for (int i = 0; i < 10; i++) path.add(new Vec3(i * 6, 0, 0));
        List<Vec3> wake = BloodProjectileEffects.trim(path, 2);
        assertEquals(path.getLast(), wake.getLast(), "the head must stay on the projectile");
        double length = 0;
        for (int i = 1; i < wake.size(); i++) length += wake.get(i).distanceTo(wake.get(i - 1));
        assertEquals(2, length, 1e-9, "a fast needle must not drag its whole flight behind it");
        assertEquals(path, BloodProjectileEffects.trim(path, 1000));
    }

    @Test void onlyTheSplashIsClaimed() {
        assertTrue(BloodShotGeometry.handles(Form.BLOOD_SPLASH));
        assertFalse(BloodShotGeometry.handles(Form.RUPTURE));
        assertFalse(AnimusMortemGeometry.handles(Form.BLOOD_SPLASH));
        assertFalse(HematicRiposteGeometry.handles(Form.BLOOD_SPLASH));
        assertEquals(ManipulationMaterials.ANIMUS, ManipulationMaterials.forForm(Form.BLOOD_SPLASH));
    }

    private static HematicRiposteGeometryTest.Capture flight(int form, float age, boolean flying) {
        var capture = new HematicRiposteGeometryTest.Capture();
        List<Vec3> path = new ArrayList<>();
        for (int i = 0; i < 10; i++) path.add(new Vec3(i * .5, 2 + Math.sin(i * .4) * .2, i * .1));
        Vec3 tether = form == 3 ? new Vec3(2, 1.2, -1) : null;
        BloodShotGeometry.shot(new PoseStack(), capture, path, flying ? EAST.scale(2) : Vec3.ZERO, new Vec3(3, 4, -6),
                new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100 + age, age, form, 23, 1, 1, flying, tether);
        return capture;
    }

    private static HematicRiposteGeometryTest.Capture splash(int variant, float radius, float age) {
        var capture = new HematicRiposteGeometryTest.Capture();
        Vec3 from = new Vec3(10, 64, -3);
        BloodShotGeometry.splash(new ManipulationVisualPacket(Form.BLOOD_SPLASH, -1, from, from.add(EAST), radius,
                BloodShotGeometry.SPLASH_TICKS, variant), new PoseStack(), capture, new Vec3(3, 2, -5), new Vec3(1, 0, 0),
                new Vec3(0, 1, 0), 100 + age, age, 1);
        return capture;
    }
}
