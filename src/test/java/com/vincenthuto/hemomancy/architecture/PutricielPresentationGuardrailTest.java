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
    /** Track 10: his fire is the Flammeus shader fire the Iron Brazier burns, with Hemomancy particles only. */
    @Test void pyresBurnTheFlammeusShaderFireWithHemomancyParticles() {
        String flames = SourceAssertions.readMainJava("client/render/world/PyreFlames.java");
        assertTrue(flames.contains("ThermalRenderTypes.STEADY_FLAME"));
        assertTrue(flames.contains("LuxUmbraGeometry.card("));
        assertTrue(flames.contains("ThermalParticles.lick("));
        assertTrue(SourceAssertions.readMainJava("client/render/tile/harbinger/decoration/RotPyreRenderer.java")
                .contains("PyreFlames.render("));
        String pylon = SourceAssertions.readMainJava("client/render/tile/harbinger/puzzle/PulsePylonRenderer.java");
        assertTrue(pylon.contains("PyreFlames.render(") && pylon.contains("PyreFlames.purge("));
        for (String source : new String[]{"client/render/world/PyreFlames.java",
                "common/block/harbinger/decoration/RotPyreBlock.java",
                "common/block/harbinger/decoration/ReliquarySconceBlock.java",
                "common/block/harbinger/puzzle/SpawnMarkBlock.java",
                "common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java"}) {
            assertFalse(SourceAssertions.readMainJava(source).contains("ParticleTypes."), source);
        }
    }
    @Test void bossNeverEmitsVanillaParticlesOrPotionSwirls() {
        String boss = SourceAssertions.readMainJava("common/entity/boss/saint/putriciel/PutricielEntity.java");
        assertFalse(boss.contains("ParticleTypes"));
        assertTrue(boss.contains("80, witherAmp, false, false, true"));
    }
}
