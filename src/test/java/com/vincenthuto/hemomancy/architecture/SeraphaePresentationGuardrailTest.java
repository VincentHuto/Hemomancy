package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class SeraphaePresentationGuardrailTest {
    @Test
    void radianceUsesTheSharedSchoolBatchBeforeTheGenericGeometry() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("SeraphaeRadianceGeometry.collect(world, poses, FLOWS.vertices(LuxUmbraBatch.Material.LUX)"));
        assertTrue(renderer.indexOf("if(SeraphaeRadianceGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java")
                .contains("if(SeraphaeRadianceGeometry.handles(form))return;"));
    }

    @Test
    void bossFragmentsAndAnchorsNeverEmitVanillaParticles() throws Exception {
        try (var files = Files.list(RepoPaths.mainJava("common/entity/boss/saint/seraphae"))) {
            for (var file : files.filter(p -> p.toString().endsWith(".java")).toList())
                assertFalse(SourceAssertions.read(file).contains("ParticleTypes"), file.toString());
        }
    }
}
