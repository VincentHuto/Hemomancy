package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VelorumPresentationGuardrailTest {
    @Test void bespokeRimeAndShadowUseTheSharedWorldPipeline() {
        String renderer=SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("VelorumRimeGeometry.collect(world, poses, FLOWS"));
        assertTrue(renderer.indexOf("if(VelorumRimeGeometry.handles")<renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java").contains("if(VelorumRimeGeometry.handles(form))return;"));
        assertTrue(SourceAssertions.readMainJava("client/render/shader/ShaderInit.java").contains("registerShader(event, MANIPULATION_RIME.createInstance(provider))"));
    }
    @Test void fightNeverEmitsVanillaParticlesOrVisiblePotionSwirls() {
        String source = SourceAssertions.readMainJava("common/entity/boss/saint/velorum/VelorumEntity.java");
        assertFalse(source.contains("ParticleTypes"));
        assertTrue(source.contains("100, 2, false, false, true"));
        assertTrue(source.contains("60, 0, false, false, true"));
    }
}
