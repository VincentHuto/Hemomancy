package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HemorathPresentationGuardrailTest {
    @Test void dedicatedGeometryUsesTheSharedFerricAndBloodPassesBeforeGenericGeometry() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("HemorathReliquaryGeometry.collect(world, poses, FLOWS"));
        assertTrue(renderer.indexOf("if(HemorathReliquaryGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java")
                .contains("if(HemorathReliquaryGeometry.handles(form))return;"));
        assertTrue(SourceAssertions.readMainJava("common/entity/boss/goal/EmptyPulseGoal.java")
                .contains("HEMOPHAGY_DURATION_TICKS, 0, false, false, true"));
    }
    @Test void bossAndBothAttackGoalsNeverEmitVanillaParticles() {
        for (String source : new String[]{"common/entity/boss/saint/hemorath/HemorathEntity.java",
                "common/entity/boss/goal/HematicCollapseGoal.java", "common/entity/boss/goal/EmptyPulseGoal.java"})
            assertFalse(SourceAssertions.readMainJava(source).contains("ParticleTypes"), source);
    }
}
