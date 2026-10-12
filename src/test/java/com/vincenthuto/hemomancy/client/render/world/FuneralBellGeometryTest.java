package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuneralBellGeometryTest {
    @Test void chargeAndTollStayFiniteQuadAlignedBoundedAndWithinBudget() {
        for (float progress : new float[] { .05f, .5f, 1 })
            check(draw(new ManipulationVisualPacket(Form.BELL_CHARGE, 4, Vec3.ZERO, new Vec3(0, 0, 9), progress, 80, 1), 0), 8);
        for (float age = 0; age < FuneralBellGeometry.TOLL_TICKS - 1; age += 1.5f)
            check(draw(new ManipulationVisualPacket(Form.BELL, -1, new Vec3(3, 64, 3), new Vec3(3, 64, 3), 10, 40, 1), age), 15);
    }

    @Test void theBellStrikesHardThenRingsDown() {
        double peak = 0;
        for (double a = 0; a < 6; a += .25) peak = Math.max(peak, Math.abs(FuneralBellGeometry.swing(a)));
        double late = 0;
        for (double a = 30; a < 40; a += .25) late = Math.max(late, Math.abs(FuneralBellGeometry.swing(a)));
        assertTrue(peak > .3, "the toll should swing the bell hard");
        assertTrue(late < peak * .1, "the swing should ring down");
    }

    @Test void threeEchoesRollOutInTurnToTheBellsReach() {
        assertTrue(FuneralBellGeometry.echo(1, 2, 8) < 0, "the second echo waits for the first");
        assertTrue(FuneralBellGeometry.echo(0, 6, 8) > FuneralBellGeometry.echo(1, 6, 8));
        for (int ring = 0; ring < 3; ring++)
            assertEquals(8, FuneralBellGeometry.echo(ring, ring * FuneralBellGeometry.ECHO_SPACING + FuneralBellGeometry.ECHO_LIFE, 8), 1e-9);
    }

    @Test void spectralBellIsHollowAndVisibleFromTheSide() {
        // Migrated from AnimusMortemGeometryTest: thin crossed sheets, not lathed inner and outer walls.
        var charge = draw(new ManipulationVisualPacket(Form.BELL_CHARGE, 4, Vec3.ZERO, new Vec3(0, 0, 6), .5f, 80, 1), 0);
        assertTrue(charge.points.size() < 220, "the forming bell should stay a few thin sheets: " + charge.points.size());
        var toll = draw(new ManipulationVisualPacket(Form.BELL, 4, Vec3.ZERO, Vec3.ZERO, 6, 40, 1), 12);
        assertTrue(toll.points.stream().anyMatch(p -> Math.abs(p.z) > .2), "the bell must read from the side");
    }

    @Test void bellLeftTheSharedTierAndRotationPreservesLength() {
        assertTrue(FuneralBellGeometry.handles(Form.BELL) && FuneralBellGeometry.handles(Form.BELL_CHARGE));
        assertFalse(AnimusMortemGeometry.handles(Form.BELL) || AnimusMortemGeometry.handles(Form.BELL_CHARGE));
        Vec3 rotated = FuneralBellGeometry.rotate(new Vec3(0, -1.7, 0), new Vec3(1, 0, 0), .4);
        assertEquals(1.7, rotated.length(), 1e-9);
    }

    private static HematicRiposteGeometryTest.Capture draw(ManipulationVisualPacket packet, float age) {
        var mesh = new HematicRiposteGeometryTest.Capture();
        FuneralBellGeometry.draw(packet, new PoseStack(), mesh, new Vec3(3, 2, -7), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 600 + age, age, 1);
        return mesh;
    }

    private static void check(HematicRiposteGeometryTest.Capture mesh, double bound) {
        assertFalse(mesh.points.isEmpty());
        assertEquals(0, mesh.points.size() % 4);
        assertTrue(mesh.points.size() <= 600, "exceeded budget: " + mesh.points.size());
        assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < bound));
    }
}
