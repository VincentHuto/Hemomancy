package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.NerveCommandRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class NerveCommandGeometryTest {
    @Test void bothCommandsStayFiniteAndBounded() {
        for (int mode : new int[] { NerveCommandRules.REBUKE, NerveCommandRules.IMPRESS })
            for (float age : new float[] { 0, 2, 5, 12, 40 }) {
                var mesh = new HematicRiposteGeometryTest.Capture();
                NerveCommandGeometry.draw(new ManipulationVisualPacket(Form.NERVE_COMMAND, 7, Vec3.ZERO, new Vec3(0, 1.6, 6), 1.9F, 10, mode),
                        new PoseStack(), mesh, new Vec3(3, 2, -4), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 300 + age, age, 1, null);
                assertFalse(mesh.points.isEmpty());
                assertEquals(0, mesh.points.size() % 4);
                assertTrue(mesh.points.size() <= 600, "command mesh too heavy: " + mesh.points.size());
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 8));
            }
    }

    @Test void rebukeAndImpressmentUseTheNerveFormWhileSovereignKeepsTheCrown() throws IOException {
        String manager = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/HematicCommandManager.java"));
        assertTrue(manager.contains("nerve ? ManipulationVisuals.Form.NERVE_COMMAND : ManipulationVisuals.Form.COMMAND"));
        assertTrue(manager.contains("ManipulationVisuals.attached(mob,ManipulationVisuals.Form.NERVE_COMMAND,0,0,0);"),
                "release must retire the nerve command");
        assertTrue(manager.contains("SchoolStates.apply(caster, mob, SchoolState.DISRUPTED, 10);"));
        String renderer = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationVisualRenderer.java"));
        int hook = renderer.indexOf("if(NerveCommandGeometry.handles(packet.form()))");
        assertTrue(hook > 0 && hook < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form()))"));
        assertTrue(renderer.contains("NerveCommandGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS)"));
    }
}
