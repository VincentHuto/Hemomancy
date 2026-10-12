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

/** Each cold manipulation keeps a structure of its own instead of the same scatter of clots. */
class CongeatioStructureTest {
    @Test void coldStructuresStayFiniteQuadAlignedAndBounded() {
        for (Form form : new Form[] { Form.ICE, Form.CRYOGENIC_PULSE, Form.STILLNESS, Form.RIMEBOUND, Form.HOUR, Form.HOUR_BREAK })
            for (float age : new float[] { 2, 10, 30 }) {
                var mesh = draw(form, age);
                assertFalse(mesh.points.isEmpty(), form + " vanished at " + age);
                assertEquals(0, mesh.points.size() % 4, form.name());
                assertTrue(mesh.points.size() <= 6000, form + " exceeded budget: " + mesh.points.size());
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 10), form.name());
            }
    }

    @Test void eachColdSpellHasItsOwnSilhouette() {
        int grasp = draw(Form.ICE, 10).points.size(), still = draw(Form.STILLNESS, 10).points.size();
        int shackles = draw(Form.RIMEBOUND, 10).points.size(), hour = draw(Form.HOUR, 10).points.size();
        assertEquals(4, java.util.Set.of(grasp, still, shackles, hour).size(), "cold spells should no longer share one scatter");
    }

    @Test void structuresAreWiredIntoTheThermalTier() throws IOException {
        String thermal = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/ThermalGeometry.java"));
        assertTrue(thermal.contains("case RIMEBOUND -> shackles("));
        assertTrue(thermal.contains("if(still)lattice(") && thermal.contains("else frostFingers("));
        assertTrue(thermal.contains("A clock of frozen blood"));
    }

    private static HematicRiposteGeometryTest.Capture draw(Form form, float age) {
        var packet = new ManipulationVisualPacket(form, 17, Vec3.ZERO, new Vec3(0, 1, 4), 3, 70, 63);
        var fragments = new ThermalFragments();
        fragments.update(packet, age);
        var batch = new LuxUmbraBatch();
        var mesh = new HematicRiposteGeometryTest.Capture();
        ThermalGeometry.draw(packet, new PoseStack(), batch, mesh, Vec3.ZERO, new Vec3(1, 1, -5), new Vec3(1, 0, 0),
                new Vec3(0, 1, 0), 1000 + age, age, 1, 1, new ThermalSurface(), fragments, .5f, false);
        batch.drawMaterials(material -> mesh);
        return mesh;
    }
}
