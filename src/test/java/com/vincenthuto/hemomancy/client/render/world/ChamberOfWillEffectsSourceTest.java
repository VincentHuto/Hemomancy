package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChamberOfWillEffectsSourceTest {
    private static final Path RENDERER = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/world/ChamberOfWillEffects.java");

    private ChamberOfWillEffectsSourceTest() {
    }

    public static void main(String[] args) throws IOException {
        String source = Files.readString(RENDERER).replace("\r\n", "\n");

        assertContains("renderSky exposes capillary layer count knob", source,
                "int capillaryDepthLayers =");
        assertContains("renderSky exposes blue vein layer count knob", source,
                "int blueVeinDepthLayers =");
        assertContains("renderSky exposes blood vessel layer count knob", source,
                "int bloodVesselDepthLayers =");
        assertContains("renderSky exposes neural layer count knob", source,
                "int neuralDepthLayers =");
        assertContains("capillaries receive layer count", source,
                "renderCapillaryWeb(poseStack, tesselator, f, skyDistance, capillaryDepthLayers");
        assertContains("blue veins receive layer count", source,
                "renderBlueVeins(poseStack, tesselator, f, skyDistance, blueVeinDepthLayers");
        assertContains("blood vessels receive layer count", source,
                "renderBloodVessels(poseStack, tesselator, f, skyDistance, membranePulse, bloodVesselDepthLayers");
        assertContains("neurons receive layer count", source,
                "renderNeuralStructures(poseStack, tesselator, f, skyDistance, neuralDepthLayers");
        assertContains("layer count maps to normalized depth", source,
                "depthLayer(layer, layerCount");
        assertContains("vascular layers should use a farther near-plane than neurons", source,
                "vascularDepthLayer(layer, layerCount");
        assertContains("red vessels should use vascular-specific depth tuning", source,
                "vascularDepthLayer(layer, layerCount, 5600");
        assertContains("blue veins should use vascular-specific depth tuning", source,
                "vascularDepthLayer(layer, layerCount, 5200");
        assertContains("neurons should use neural-specific depth tuning", source,
                "neuralDepthLayer(layer, layerCount, 6000");
        assertContains("far neural layers should be significantly dimmer", source,
                "Mth.lerp(easedT, 0.16F, 1.16F)");
        assertContains("far neural soma cores should not use near-layer brightness", source,
                "neuralLayerAlpha(Mth.lerp(layer.t(), 8.0F, 104.0F), layer)");
        assertContains("far neural axons should start very subtle", source,
                "Mth.lerp(layer.t(), 4.0F, 72.0F)");
        assertContains("far neural colors should be muted before additive blending", source,
                "Mth.floor(Mth.lerp(layer.t(), 72.0F, 148.0F))");
        assertContains("neural brightness should use a perceptual depth falloff", source,
                "private static float neuralLayerVisibility(DepthLayer layer)");
        assertContains("neural alpha should include depth visibility before additive blending", source,
                "return layeredAlpha(alpha * neuralLayerVisibility(layer), layer);");
        assertContains("neural impulse glows should inherit layer visibility", source,
                "renderNeuronImpulse(poseStack, tesselator, centerX, axonDepth - 0.04F, centerZ, somaRadius * 0.55F, angle, length, phase, layerTime, layer.t() > 0.66F ? 2 : 1, true, neuralLayerVisibility(layer));");
        assertContains("neural impulse cores should scale by depth visibility", source,
                "(primary ? 118 : 84) * fade * visibility");
        assertContains("vascular near layer should stop farther from the camera", source,
                "Mth.lerp(easedT, 1.075F, 0.640F)");
        assertContains("vascular near layer should cap width growth below neuron layers", source,
                "Mth.lerp(easedT, 0.24F, 0.68F)");
        assertContains("closest blood vessels should avoid neon additive glare", source,
                "Mth.lerp(layer.t(), 70.0F, 168.0F) + membranePulse * 12.0F");
        assertContains("closest blood vessel alpha should leave room for cell visibility", source,
                "Mth.lerp(layer.t(), 14.0F, edgeLane ? 48.0F : 62.0F) + membranePulse * 6.0F");
        assertContains("single blood vessel layer should keep near-layer detail", source,
                "float vesselDetailT = bloodVesselDetailT(layer);");
        assertContains("single blood vessel layer should not brighten alpha with detail t", source,
                "int alpha = layeredAlpha(Mth.lerp(layer.t(), 14.0F, edgeLane ? 48.0F : 62.0F) + membranePulse * 6.0F, layer);");
        assertContains("blood vessel detail t should promote one-layer density", source,
                "return layer.index() == 0 && Mth.abs(layer.t() - 0.5F) < 0.001F ? 1.0F : layer.t();");
        assertContains("blood vessel count should use detail t", source,
                "int vesselCount = 3 + Mth.floor(vesselDetailT * 3.0F);");
        assertContains("blood vessel width should use detail t", source,
                "float baseWidth = layeredSize(Mth.lerp(vesselDetailT, 0.060F, edgeLane ? 0.46F : 0.62F)");
        assertContains("blood vessel branches should sit behind the cell stream", source,
                "layeredAlpha(Mth.lerp(layer.t(), 16.0F, 48.0F), layer)");
        assertContains("vein branching should render multiple offshoots per parent", source,
                "private static void renderVesselBranches(");
        assertContains("blood vessels should request dense branching", source,
                "renderVesselBranches(poseStack, tesselator, depth - 0.04F, span, laneOffset, angle, wave, phase, layerTime, baseWidth * Mth.lerp(vesselDetailT, 0.42F, 0.62F), 3 + Mth.floor(vesselDetailT * 2.0F)");
        assertContains("blue veins should request visible branching", source,
                "renderVesselBranches(poseStack, tesselator, depth - 0.04F, span, laneOffset, angle, wave, phase, layerTime * 0.72F, baseWidth * 0.54F, 2 + Mth.floor(layer.t() * 2.0F)");
        assertContains("branch roots should stay attached to parent veins", source,
                "float rootInset = 0.0F;");
        assertContains("branch roots should taper in width to avoid a clipped overlap", source,
                "float rootWidth = Mth.clamp(0.22F + t * 4.5F, 0.22F, 1.0F);");
        assertContains("branch roots should fade in softly at the parent vein", source,
                "float rootJoin = Mth.clamp(0.24F + t * 7.5F, 0.0F, 1.0F);");
        assertContains("branch roots should use the parent vein phase for attachment", source,
                "float parentLateral = parentLaneOffset + vesselOffset(parentT, parentWave, parentPhase, time, parentSpan);");
        assertContains("branch wiggle should use a separate phase from parent attachment", source,
                "float branchPhase = parentPhase + branch * 0.73F;");
        assertContains("branch phase should not shift the root off the parent vein", source,
                "renderVesselBranch(poseStack, tesselator, depth - branch * 0.006F, parentSpan, parentLaneOffset, parentAngleDegrees, parentWave, parentPhase, branchPhase");
        assertNotContains("branch phase must not move the parent attachment point", source,
                "parentWave, parentPhase + branch * 0.73F, time");
        assertContains("neural pass should render inter-neuron synapse flare events", source,
                "renderSynapseFlareEvents(poseStack, tesselator, time, skyDistance, face);");
        assertContains("membrane heartbeat should have a stronger broad pulse shell", source,
                "int outerAlpha = (int) Mth.clamp(4.0F + pulse * 54.0F, 0.0F, 56.0F);");
        assertContains("membrane heartbeat should have a tighter inner pulse shell", source,
                "int innerAlpha = (int) Mth.clamp(2.0F + pulse * 38.0F, 0.0F, 32.0F);");
        assertContains("membrane heartbeat should render the broad shell", source,
                "renderMembranePulseFace(poseStack, tesselator, skyDistance * 0.665F, -skyDistance * 0.612F, 46, 2, 16, outerAlpha);");
        assertContains("membrane heartbeat should render the inner contraction shell", source,
                "renderMembranePulseFace(poseStack, tesselator, skyDistance * 0.505F, -skyDistance * 0.488F, 92, 6, 24, innerAlpha);");
        assertContains("membrane heartbeat face should use radial fade segments instead of a flat quad", source,
                "int pulseSegments = 48;");
        assertContains("membrane heartbeat should fade out before cube-face edges", source,
                "float edgeRadius = halfSize * 1.34F;");
        assertContains("membrane heartbeat perimeter should be transparent", source,
                "setColor(red, green, blue, 0)");
        assertNotContains("distance layers should not be hard-gated by static booleans", source,
                "SHOW_ULTRA_FAR_LAYERS");
        assertNotContains("distance layers should not be hard-gated by static booleans", source,
                "SHOW_FAR_LAYERS");
        assertNotContains("distance layers should not be hard-gated by static booleans", source,
                "SHOW_MID_LAYERS");
        assertNotContains("distance layers should not be hard-gated by static booleans", source,
                "SHOW_CLOSE_LAYERS");
        assertNotContains("distance layers should not be hard-gated by static booleans", source,
                "SHOW_ULTRA_CLOSE_LAYERS");
    }

    private static void assertContains(String label, String source, String expected) {
        if (!source.contains(expected)) {
            throw new AssertionError(label + ": missing " + expected);
        }
    }

    private static void assertNotContains(String label, String source, String unexpected) {
        if (source.contains(unexpected)) {
            throw new AssertionError(label + ": still contains " + unexpected);
        }
    }
}
