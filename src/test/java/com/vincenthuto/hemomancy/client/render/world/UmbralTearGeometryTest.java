package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UmbralTearGeometryTest {
    @Test void tearsStayFiniteQuadAlignedBoundedAndWithinBudget() {
        for (Form form : new Form[] { Form.TELEPORT, Form.UMBRA_ARRIVAL })
            for (float age = 0; age < 22; age += 1.1f) {
                var mesh = new HematicRiposteGeometryTest.Capture();
                Vec3 at = new Vec3(10, 70, -4);
                UmbralTearGeometry.draw(new ManipulationVisualPacket(form, -1, at, at, 1, 20, 1), new PoseStack(), mesh,
                        new Vec3(3, 1.5, -5), new Vec3(.8, 0, .6), new Vec3(0, 1, 0), 500 + age, age, 1);
                assertEquals(0, mesh.points.size() % 4);
                assertTrue(mesh.points.size() <= 700, form + " exceeded budget: " + mesh.points.size());
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 4), form.name());
            }
    }

    @Test void departureCollapsesIntoTheSeamWhileArrivalReforms() {
        // Migrated from LuxUmbraGeometryTest: the traveller leaves through the tear and comes back out of the next one.
        assertTrue(UmbralTearGeometry.presence(Form.TELEPORT, 10) < UmbralTearGeometry.presence(Form.TELEPORT, 1));
        assertTrue(UmbralTearGeometry.presence(Form.UMBRA_ARRIVAL, 10) > UmbralTearGeometry.presence(Form.UMBRA_ARRIVAL, 1));
    }

    @Test void theSeamOpensThenSeals() {
        for (Form form : new Form[] { Form.TELEPORT, Form.UMBRA_ARRIVAL }) {
            assertEquals(0, UmbralTearGeometry.opening(form, 0), 1e-9);
            assertTrue(UmbralTearGeometry.opening(form, 6) > .9, form + " should gape while the body passes");
            assertEquals(0, UmbralTearGeometry.opening(form, 20), 1e-9, form + " should seal");
        }
    }

    @Test void travelFormsLeftTheSharedFlowTier() {
        for (Form form : new Form[] { Form.TELEPORT, Form.UMBRA_ARRIVAL }) {
            assertTrue(UmbralTearGeometry.handles(form));
            assertFalse(LuxUmbraGeometry.handles(form));
            assertEquals(ManipulationMaterials.TENEBRIS, ManipulationMaterials.forForm(form));
        }
        assertTrue(LuxUmbraGeometry.handles(Form.VEIL), "the veil stays on the shared tier");
    }
}
