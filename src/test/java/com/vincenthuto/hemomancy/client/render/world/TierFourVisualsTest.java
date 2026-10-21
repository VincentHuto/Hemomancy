package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Blood Cloud, Hematic Mortar's charge, Blackhearted and the Umbral veil. */
class TierFourVisualsTest {
    @Test void bloodFormsStayFiniteQuadAlignedAndBounded() {
        for (Form form : new Form[] { Form.CLOUD, Form.MORTAR_CHARGE, Form.BLACK_HEART })
            for (float radius : new float[] { .2f, 1, 3 })
                for (float age : new float[] { 1, 10, 40 }) {
                    var mesh = new HematicRiposteGeometryTest.Capture();
                    AnimusMortemGeometry.draw(new ManipulationVisualPacket(form, 7, Vec3.ZERO, new Vec3(0, 0, 9), radius, 60, 1),
                            new PoseStack(), mesh, new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 200 + age, age, 1, 1);
                    assertFalse(mesh.points.isEmpty(), form + " vanished");
                    assertEquals(0, mesh.points.size() % 4);
                    assertTrue(mesh.points.size() <= 900, form + " exceeded budget: " + mesh.points.size());
                    assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 8), form.name());
                }
    }

    @Test void cloudRainFallsBelowItsUnderbelly() {
        var mesh = new HematicRiposteGeometryTest.Capture();
        AnimusMortemGeometry.draw(new ManipulationVisualPacket(Form.CLOUD, 7, Vec3.ZERO, Vec3.ZERO, 2, 60, 1), new PoseStack(), mesh,
                new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 233, 20, 1, 1);
        assertTrue(mesh.points.stream().anyMatch(p -> p.y < -1), "rain should fall well below the cloud");
    }

    @Test void ductilisAndFerricExtrasDrawWithinBudget() {
        for (Form form : new Form[] { Form.LIGHTNING_CHARGE, Form.NERVE_HIT, Form.FERRIC_CONJURE })
            for (float age : new float[] { 1, 6, 20 }) {
                FerricDuctilisGeometry.begin();
                var iron = new HematicRiposteGeometryTest.Capture();
                var flow = new HematicRiposteGeometryTest.Capture();
                FerricDuctilisGeometry.draw(new ManipulationVisualPacket(form, 4, Vec3.ZERO, new Vec3(0, 0, 4), 1, 60, 3), new PoseStack(),
                        iron, flow, new Vec3(0, 1, -4), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 1000 + age, age, .8f, 1, null);
                assertFalse(flow.points.isEmpty(), form + " lost its current at " + age);
                assertEquals(0, flow.points.size() % 4);
                assertEquals(0, iron.points.size() % 4);
                if (form == Form.FERRIC_CONJURE) assertFalse(iron.points.isEmpty(), "iron should condense at the hand");
            }
    }

    @Test void impactBurstsFlashBeforeTheyScatter() {
        var early = new HematicRiposteGeometryTest.Capture();
        var late = new HematicRiposteGeometryTest.Capture();
        for (var entry : new Object[][] { { early, 1f }, { late, 6f } })
            AnimusMortemGeometry.draw(new ManipulationVisualPacket(Form.ANIMUS_IMPACT, -1, Vec3.ZERO, Vec3.ZERO, 1, 16, 1), new PoseStack(),
                    (HematicRiposteGeometryTest.Capture) entry[0], new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0),
                    100 + (float) entry[1], (float) entry[1], 1, 1);
        assertTrue(early.points.size() > late.points.size(), "the hot core flash belongs to the first ticks");
    }

    @Test void veilWrapsTheBodyAndGlintsViolet() {
        var mesh = new HematicRiposteGeometryTest.Capture();
        LuxUmbraGeometry.draw(new ManipulationVisualPacket(Form.VEIL, 7, Vec3.ZERO, Vec3.ZERO, 1, 40, 1), new PoseStack(), mesh,
                new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 225, 12, 1, 1);
        assertEquals(0, mesh.points.size() % 4);
        assertTrue(mesh.points.size() <= 320, "the veil must stay within the flow budget: " + mesh.points.size());
        assertTrue(mesh.points.stream().anyMatch(p -> p.y > 1.4), "the shroud should wind up past the shoulders");
    }
}
