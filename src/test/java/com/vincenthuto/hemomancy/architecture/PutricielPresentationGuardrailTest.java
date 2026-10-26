package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PutricielPresentationGuardrailTest {
    @Test void novaReleaseHasItsOwnSyncedChannelSoHitsCannotReplaceIt() {
        String boss = SourceAssertions.readMainJava("common/entity/boss/saint/putriciel/PutricielEntity.java");
        assertTrue(boss.contains("DATA_NOVA_RELEASE"));
        assertTrue(boss.contains("getVisualState() != VISUAL_ABSOLUTION_HIT"));
    }
    @Test void bespokeGeometryUsesTheSharedDispatcherBeforeGenericGeometry() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("PutricielOfferingGeometry.collect(world, poses, FLOWS"));
        assertTrue(renderer.indexOf("if(PutricielOfferingGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java").contains("if(PutricielOfferingGeometry.handles(form))return;"));
    }
    @Test void bossNeverEmitsVanillaParticlesOrPotionSwirls() {
        String boss = SourceAssertions.readMainJava("common/entity/boss/saint/putriciel/PutricielEntity.java");
        assertFalse(boss.contains("ParticleTypes"));
        assertTrue(boss.contains("80, witherAmp, false, false, true"));
    }
}
