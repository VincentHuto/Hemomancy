package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Seraphae's presentation: her radiance rides the shared school batch, nothing of hers emits vanilla particles or the
 * hit glow (cursors and traces are fine glow motes), gold reaches her geometry only through the Ductilis material, her
 * renderer draws the white vessel layer and the gold nerve layer apart, her model's tracks read the kit's synced move
 * clock, and her generator paints no gilt.
 */
class SeraphaePresentationGuardrailTest {
    private static final List<String> CLIENT_FILES = List.of(
            "client/render/world/SeraphaeRadianceGeometry.java", "client/render/world/SeraphaeTelegraphPass.java",
            "client/render/world/SeraphaeTissueGeometry.java", "client/render/world/SeraphaeBoneGeometry.java",
            "client/render/world/SeraphaeLightGeometry.java", "client/render/world/SeraphaeImpactGeometry.java",
            "client/render/world/SeraphaeOvergrowthRules.java",
            "client/render/world/SeraphaeTelegraphStyle.java", "client/render/layer/mob/SeraphaeVesselLayer.java",
            "client/render/layer/mob/SeraphaeNerveLayer.java",
            "client/render/entity/boss/seraphae/SeraphaeRenderer.java",
            "client/model/entity/boss/seraphae/SeraphaeModel.java",
            "client/screen/overlay/boss/frame/SeraphaeMeterStyle.java");

    @Test
    void radianceUsesTheSharedSchoolBatchBeforeTheGenericGeometry() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("SeraphaeRadianceGeometry.collect(world, poses, FLOWS"));
        assertTrue(renderer.indexOf("if(SeraphaeRadianceGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java")
                .contains("if(SeraphaeRadianceGeometry.handles(form))return;"));
    }

    @Test
    void nothingOfHersEmitsVanillaParticlesOrTheHitGlow() throws Exception {
        List<Path> files = new ArrayList<>();
        try (var listed = Files.list(RepoPaths.mainJava("common/entity/boss/saint/seraphae"))) {
            files.addAll(listed.filter(p -> p.toString().endsWith(".java")).toList());
        }
        for (String client : CLIENT_FILES) files.add(RepoPaths.mainJava(client));
        for (Path file : files) {
            String text = SourceAssertions.read(file);
            assertFalse(text.contains("ParticleTypes"), file.toString());
            assertFalse(text.contains("hitGlow"), file + ": traces are fine glow motes, never the hit glow");
        }
    }

    @Test
    void goldOnlyEverRidesTheDuctilisMaterial() {
        for (String geometry : List.of("client/render/world/SeraphaeRadianceGeometry.java",
                "client/render/world/SeraphaeTelegraphPass.java", "client/render/world/SeraphaeLightGeometry.java",
                "client/render/world/SeraphaeImpactGeometry.java", "client/render/world/SeraphaeTissueGeometry.java",
                "client/render/world/SeraphaeBoneGeometry.java")) {
            String text = SourceAssertions.readMainJava(geometry);
            assertFalse(text.contains("0xFFD563"), geometry + ": the old gold ribbon on the Lux batch is gone");
            for (String statement : text.split(";")) {
                if (!statement.contains("NERVE,") && !statement.contains("NERVE)")) continue;
                if (statement.contains("static final")) continue;
                assertTrue(statement.contains("gold"),
                        geometry + ": the nerve colour draws on the gold consumer: " + statement.strip());
                assertFalse(statement.contains("(p, lux,") || statement.contains("(p, v,"),
                        geometry + ": gold never rides the Lux consumer: " + statement.strip());
            }
        }
        assertFalse(SourceAssertions.readMainJava("client/render/entity/boss/seraphae/SeraphaeRenderer.java")
                .contains("SeraphaeWingGlowLayer"));
    }

    /**
     * Visual pass 2: her own pass draws every footprint and pad in both of the manipulation visuals' passes, her style
     * only declines the kit's flat painter, her two materials are her own shaders registered once, and gold never
     * enters her tissue or bone shaders (it is a nerve, on the Ductilis material).
     */
    @Test
    void herOwnPassDrawsHerTelegraphsInHerOwnMaterials() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("SeraphaeTelegraphPass.collect(world, poses, FLOWS, camera, right, up, time, partial, solids);"),
                "her pass runs in the solid and the translucent pass");
        String style = SourceAssertions.readMainJava("client/render/world/SeraphaeTelegraphStyle.java");
        assertFalse(style.contains("SaintTelegraphPainter"), "her style paints none of the kit's flat tints");
        String shaders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
        assertTrue(shaders.contains("registerShader(event, SERAPHAE_TISSUE.createInstance(provider));"));
        assertTrue(shaders.contains("registerShader(event, SERAPHAE_BONE.createInstance(provider));"));
        for (String shader : List.of("seraphae_tissue", "seraphae_bone")) {
            String fragment = SourceAssertions.read(RepoPaths.ASSETS.resolve("shaders/core/world/" + shader + ".fsh"));
            assertFalse(fragment.contains("GOLD") || fragment.contains("NERVE_GOLD"), shader + " carries no gold colour");
        }
        String types = SourceAssertions.readMainJava("client/render/world/SeraphaeRenderTypes.java");
        assertTrue(types.contains("static final RenderType TISSUE") && types.contains("static final RenderType BONE"),
                "her render types are built once");
    }

    @Test
    void herModelReadsTheKitsSyncedMoveClock() {
        String generator = SourceAssertions.read("tools/model_export/seraphae_anim.java");
        SourceAssertions.assertContains("tracks replay the synced move", generator, "view.moveTime(partial)");
        SourceAssertions.assertContains("tracks blend from the last pose", generator, "state.blend(");
        assertFalse(generator.contains("tickCount -"), "no private tick clock");
    }

    @Test
    void herGeneratorPaintsNoGilt() {
        String generator = SourceAssertions.read("tools/model_export/seraphae.py");
        assertFalse(generator.contains("'gilt'"), "gold on her is only ever nerve light");
        assertFalse(generator.contains("(100, 72, 39)"), "the copper-reading iron texel is gone");
    }
}
