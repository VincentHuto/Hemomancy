package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothBloomBlackHoleResourceTest {
    @Test
    void treeApexUsesTheLensedAccretionShaderAsACameraFacingDisk() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/world/QliphothBloomRenderer.java"));

        assertTrue(renderer.contains("renderAccretionBlackHole("));
        assertTrue(renderer.contains("HemoRenderTypes.qliphothBlackHole("));
        assertTrue(renderer.contains("cameraOrientation()"));
        assertTrue(renderer.substring(renderer.indexOf("private static void drawFacetedApex"),
                renderer.indexOf("private static void drawVeins")).contains("apexLoops("));
    }

    @Test
    void finalBlackHoleShaderBuildsDiskShadowAndBothLensedImages() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertTrue(shader.contains("float accretionDisk"));
        assertTrue(shader.contains("float upperLens"));
        assertTrue(shader.contains("float lowerLens"));
        assertTrue(shader.contains("float directDisk"));
        assertTrue(shader.contains("float doppler"));
        assertTrue(shader.contains("if (FinalHole > 0.5)"));
    }

    @Test
    void lensedDiskReachesTheEventHorizonWithoutASkyColoredMoat() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertTrue(shader.contains("float rearUpperImage"));
        assertTrue(shader.contains("float rearLowerImage"));
        assertTrue(shader.contains("float lensArch"));
        assertTrue(shader.contains("float horizonContact"));
        assertTrue(shader.contains("redFlow = max(redFlow, horizonContact);"));
    }

    @Test
    void treeBlackHoleLensesACopyOfTheRenderedWorldInScreenSpace() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/world/QliphothBloomRenderer.java"));
        String renderTypes = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java"));
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));
        String shaderJson = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.json"));

        assertTrue(renderer.contains("copyMainRenderTarget("));
        assertTrue(renderer.contains("frameCopyTarget.getColorTextureId()"));
        assertTrue(renderTypes.contains("RenderSystem.setShaderTexture(0, sceneTextureId)"));
        assertTrue(shader.contains("uniform vec2 ScreenSize;"));
        assertTrue(shader.contains("gl_FragCoord.xy / ScreenSize"));
        assertTrue(shader.contains("texture(Sampler0, clamp(lensedSceneUv"));
        assertTrue(shaderJson.contains("\"name\": \"ScreenSize\""));
    }

    @Test
    void veinOrbitsEnterTheSceneCaptureInsteadOfDrawingAcrossTheEventHorizon() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/world/QliphothBloomRenderer.java"));
        String apex = renderer.substring(renderer.indexOf("private static void drawFacetedApex"),
                renderer.indexOf("private static void drawVeins"));

        assertTrue(apex.indexOf("apexLoops(time)") < apex.indexOf("renderAccretionBlackHole("));
        assertTrue(apex.contains("1.72f, 1.72f, true"));
    }

    @Test
    void finalDiskIsOneContinuousEmissiveFlowInsteadOfASeparateBlackBar() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertFalse(shader.contains("float blackBand"));
        assertTrue(shader.contains("float directDisk"));
        assertTrue(shader.contains("float rearUpperImage"));
        assertTrue(shader.contains("float rearLowerImage"));
        assertTrue(shader.contains("float diskFlow = max(directDisk"));
        assertTrue(shader.contains("float shadow = eventHorizon"));
    }

    @Test
    void lensingWarpsTheCapturedSceneInAVisiblePhotonShell() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertTrue(shader.contains("float photonShell"));
        assertTrue(shader.contains("float inverseRadiusDeflection"));
        assertTrue(shader.contains("sceneDirection * inverseRadiusDeflection"));
        assertTrue(shader.contains("alpha *= clamp(eventHorizon"));
        assertTrue(shader.contains("+ photonShell"));
    }

    @Test
    void finalHoleCarriesAnAlienCoronaWithoutTintingTheBlackCore() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertTrue(shader.contains("float alienCorona"));
        assertTrue(shader.contains("float horizonRim"));
        assertTrue(shader.contains("vec3 alienTint"));
        assertTrue(shader.contains("vec3 coronaGlow"));
        assertTrue(shader.contains("coronaGlow *= 1.0 - eventHorizon"));
        assertTrue(shader.contains("+ alienCorona"));
    }

    @Test
    void alienCoronaPulsesWithALayeredNonUniformHeartbeat() throws Exception {
        String shader = Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh"));

        assertTrue(shader.contains("float primaryPulse"));
        assertTrue(shader.contains("float secondaryPulse"));
        assertTrue(shader.contains("float alienPulse"));
        assertTrue(shader.contains("alienCorona *= alienPulse"));
        assertTrue(shader.contains("horizonRim *= mix("));
    }
}
