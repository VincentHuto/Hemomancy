package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrimsonCoronationGeometryTest {
    @Test void crownAndChargeStayFiniteQuadAlignedAndWithinBudget() {
        for (int count : new int[] { 0, 1, 4, 8 })
            for (float formation : new float[] { .1f, .3f, 1 }) {
                var mesh = draw(new ManipulationVisualPacket(Form.CROWN, 6, Vec3.ZERO, Vec3.ZERO, 1.15f, 400, count), formation);
                check(mesh);
            }
        for (float progress : new float[] { .02f, .5f, 1 })
            check(draw(new ManipulationVisualPacket(Form.CROWN_CHARGE, 6, Vec3.ZERO, new Vec3(0, 0, 9), progress, 40, 1), 1));
    }

    @Test void swordsCondenseInTurnAndOnlyArmedSlotsFill() {
        for (int slot = 1; slot < 8; slot++)
            assertTrue(CrimsonCoronationGeometry.swordFormed(.5, slot, 8) <= CrimsonCoronationGeometry.swordFormed(.5, slot - 1, 8));
        for (int slot = 0; slot < 3; slot++) assertEquals(1, CrimsonCoronationGeometry.swordFormed(1, slot, 3), 1e-9);
        assertEquals(0, CrimsonCoronationGeometry.swordFormed(1, 3, 3), 1e-9, "an unarmed slot holds no sword");
    }

    @Test void spikesTurnWithTheSwordsTheyHold() {
        for (int slot = 0; slot < 8; slot++) {
            Vec3 spike = CrimsonCoronationGeometry.spike(120, slot), sword = ManipulationVisuals.swordOffset(120, slot);
            double angle = Math.atan2(spike.z, spike.x) - Math.atan2(sword.z, sword.x);
            assertEquals(0, Math.sin(angle), 1e-9, "spike " + slot + " should sit under its sword");
        }
        assertTrue(CrimsonCoronationGeometry.handles(Form.CROWN) && !AnimusMortemGeometry.handles(Form.CROWN));
        assertFalse(AnimusMortemGeometry.handles(Form.CROWN_CHARGE));
    }

    private static HematicRiposteGeometryTest.Capture draw(ManipulationVisualPacket packet, float formation) {
        var mesh = new HematicRiposteGeometryTest.Capture();
        CrimsonCoronationGeometry.draw(packet, new PoseStack(), mesh, new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0),
                250, formation, 1);
        return mesh;
    }

    private static void check(HematicRiposteGeometryTest.Capture mesh) {
        assertFalse(mesh.points.isEmpty());
        assertEquals(0, mesh.points.size() % 4);
        // The sword mesh is unchanged and dominates; the crown, spikes and tethers must stay a small addition.
        var sword = new HematicRiposteGeometryTest.Capture();
        VisceralMesh.sword(new PoseStack(), new BloodSurfaceVertices(sword), 1, 0);
        assertTrue(mesh.points.size() <= 8 * sword.points.size() + 1500, "exceeded budget: " + mesh.points.size());
        assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 6));
    }
}
