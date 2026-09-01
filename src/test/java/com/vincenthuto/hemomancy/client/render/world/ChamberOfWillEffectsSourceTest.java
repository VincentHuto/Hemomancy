package com.vincenthuto.hemomancy.client.render.world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChamberOfWillEffectsSourceTest {
	private static final Path WRAPPER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberOfWillEffects.java");
	private static final Path HELPERS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberOfWillRenderHelpers.java");
	private static final Path ABSTRACT_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/AbstractChamberThemeEffects.java");
	private static final Path THEME_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberThemeEffects.java");
	private static final Path QLIPHOTH_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/QliphothCommunionChamberEffects.java");
	private static final Path SILENT_ARCHON_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/SilentArchonChamberEffects.java");
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
    private static final Path THEME = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyTheme.java");
    private static final Path REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
    private static final Path RENDER_TYPES = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
    private static final Path SHADER_INIT = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
    private static final Path SILENT_ARCHON_FOG_SHADER = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/silent_archon_fog.json");
    private static final Path SILENT_ARCHON_FOG_FRAGMENT = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/silent_archon_fog.fsh");
    private static final Path SILENT_ARCHON_STORM_CLOUD_FRAGMENT = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/silent_archon_storm_cloud.fsh");
	private static final Path SILENT_ARCHON_SKY_TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/silent_archon_sky.png");
	private static final Path SILENT_ARCHON_CLOUD_TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/silent_archon_clouds.png");
	private static final Path QLIPHOTH_SKY_TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/qliphoth_communion_sky.png");
	private static final Path QLIPHOTH_CLOUD_TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/qliphoth_communion_clouds.png");
	private static final Path QLIPHOTH_BLACK_HOLE_SHADER = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.json");
	private static final Path QLIPHOTH_BLACK_HOLE_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/qliphoth_black_hole.fsh");

    private ChamberOfWillEffectsSourceTest() {
    }

    public static void main(String[] args) throws IOException {
        String source = read(WRAPPER) + "\n" + read(THEME_EFFECTS) + "\n" + read(ABSTRACT_EFFECTS) + "\n" + read(HELPERS)
				+ "\n" + read(QLIPHOTH_EFFECTS) + "\n" + read(SILENT_ARCHON_EFFECTS);
		String clientEvents = Files.readString(CLIENT_EVENTS).replace("\r\n", "\n");
        String theme = Files.readString(THEME).replace("\r\n", "\n");
        String registry = Files.readString(REGISTRY).replace("\r\n", "\n");
        String renderTypes = Files.readString(RENDER_TYPES).replace("\r\n", "\n");
        String shaderInit = Files.readString(SHADER_INIT).replace("\r\n", "\n");
        String fogShader = Files.exists(SILENT_ARCHON_FOG_SHADER)
                ? Files.readString(SILENT_ARCHON_FOG_SHADER).replace("\r\n", "\n") : "";
        String fogFragment = Files.exists(SILENT_ARCHON_FOG_FRAGMENT)
                ? Files.readString(SILENT_ARCHON_FOG_FRAGMENT).replace("\r\n", "\n") : "";
		String stormCloudFragment = Files.exists(SILENT_ARCHON_STORM_CLOUD_FRAGMENT)
				? Files.readString(SILENT_ARCHON_STORM_CLOUD_FRAGMENT).replace("\r\n", "\n") : "";
		String qliphothBlackHoleShader = Files.exists(QLIPHOTH_BLACK_HOLE_SHADER)
				? Files.readString(QLIPHOTH_BLACK_HOLE_SHADER).replace("\r\n", "\n") : "";
		String qliphothBlackHoleFragment = Files.exists(QLIPHOTH_BLACK_HOLE_FRAGMENT)
				? Files.readString(QLIPHOTH_BLACK_HOLE_FRAGMENT).replace("\r\n", "\n") : "";
		String nebulaFaceSource = source.substring(source.indexOf("static float renderNebulaFace"),
				source.indexOf("static Vector3f colorVector"));
		String membranePulseSource = source.substring(source.indexOf("static void renderMembranePulseVignette"),
				source.indexOf("static void renderMembranePulseFace"));
		String qliphothSmallHoleSource = source.substring(source.indexOf("static void renderQliphothSmallBlackHole"),
				source.indexOf("static void renderQliphothResidualRing"));
        String silentArchonFogRenderTypeSource = renderTypes.substring(
                renderTypes.indexOf("public static RenderType silentArchonVolumetricFog"),
                renderTypes.indexOf("private static void setUniform"));

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
                "static float neuralLayerVisibility(DepthLayer layer)");
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
                "int vesselCount = 2 + Mth.floor(vesselDetailT * 2.0F);");
        assertContains("blood vessel width should use detail t", source,
                "float baseWidth = layeredSize(Mth.lerp(vesselDetailT, 0.060F, edgeLane ? 0.46F : 0.62F)");
        assertContains("blood vessel branches should sit behind the cell stream", source,
                "layeredAlpha(Mth.lerp(layer.t(), 16.0F, 48.0F), layer)");
        assertContains("vein branching should render multiple offshoots per parent", source,
                "static void renderVesselBranches(");
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

        assertContains("themes expose a monolith pillar toggle", theme,
                "boolean renderMonolithPillars");
        assertContains("themes expose a monolith pillar count", theme,
                "int monolithPillarCount");
        assertContains("theme builder can enable monolith pillars", theme,
                "public Builder monolithPillars(int count)");
        assertContains("silent archon should disable biological overlay layers", registry,
                ".layers(0, 0, 0, 0)");
        assertContains("silent archon should render monolith pillars", registry,
                ".monolithPillars(16)");
		assertContains("silent archon should use a dedicated calm sky texture", registry,
				"SILENT_ARCHON_SKY");
		assertContains("silent archon should use a dedicated soft cloud texture", registry,
				"SILENT_ARCHON_CLOUDS");
		assertContains("qliphoth communion should use a dedicated dark sky texture", registry,
				"QLIPHOTH_COMMUNION_SKY");
		assertContains("qliphoth communion should use a dedicated mist cloud texture", registry,
				"QLIPHOTH_COMMUNION_CLOUDS");
		assertContains("qliphoth communion theme should bind dedicated textures", registry,
				".textures(QLIPHOTH_COMMUNION_SKY, QLIPHOTH_COMMUNION_CLOUDS, DEFAULT_WISP, DEFAULT_NOISE)");
		assertContains("qliphoth communion sigils should replace the normal neural structure layer", registry,
				".layers(2, 1, 3, 0)");
		assertContains("qliphoth communion should render its sigil backdrop before anatomical veins", source,
				"renderQliphothCommunionBackdrop(poseStack, tesselator, f, skyDistance, theme, qliphothPomeCount);");
		assertBefore("qliphoth sigils should render behind blue veins", source,
				"renderBeforeSharedLayers(context);",
				"renderBlueVeins(poseStack, tesselator, f, skyDistance, blueVeinDepthLayers, theme);");
		assertContains("renderer should read synced pome count for qliphoth sky", source,
				"int qliphothPomeCount = ChamberOfWillClientData.qliphothPomesConsumed();");
		assertContains("renderer should draw qliphoth communion sky additions", source,
				"renderQliphothCommunionSky(poseStack, tesselator, f, skyDistance, theme, qliphothPomeCount);");
		assertBefore("qliphoth black holes should still render after blood vessels", source,
				"renderBloodVessels(poseStack, tesselator, f, skyDistance, membranePulse, bloodVesselDepthLayers, theme);",
				"renderAfterSharedLayers(context);");
		assertContains("qliphoth communion should add a faint constellation sigil layer", source,
				"renderQliphothConstellationSigils(poseStack, tesselator, time, skyDistance, pomeCount);");
		assertContains("qliphoth constellation sigils should render on each skybox face", source,
				"renderQliphothConstellationSigilFace(poseStack, tesselator, time, skyDistance, face, pomeCount);");
		assertContains("qliphoth constellation sigils should subtly lens toward migrating small holes", source,
				"qliphothSigilLensPull(time, pomeCount, yaw, pitch)");
		assertContains("qliphoth sigils should render as visible black and red goetic seals", source,
				"addQliphothGoeticSealStroke(buffer, matrix,");
		assertContains("qliphoth sigils should add radial rune bars instead of star-only constellations", source,
				"addQliphothGoeticRuneBar(buffer, matrix,");
		assertContains("qliphoth sigils should use organic curves instead of polygonal outlines", source,
				"addQliphothOrganicSigilCurve(buffer, matrix,");
		assertContains("qliphoth sigils should use curled terminals like ars goetic sigils", source,
				"addQliphothOrganicSigilCurl(buffer, matrix,");
		assertContains("qliphoth sigils should choose a stable randomized seal grammar", source,
				"int variant = random.nextInt(8);");
		assertContains("qliphoth sigils should include a hooked bar variant", source,
				"renderQliphothHookedBarSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a crowned loop variant", source,
				"renderQliphothCrownedLoopSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a forked pendant variant", source,
				"renderQliphothForkedPendantSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a ladder scroll variant", source,
				"renderQliphothLadderScrollSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a crescent hook variant", source,
				"renderQliphothCrescentHookSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a trident knot variant", source,
				"renderQliphothTridentKnotSigil(buffer, matrix,");
		assertContains("qliphoth sigils should include a stepped key variant", source,
				"renderQliphothSteppedKeySigil(buffer, matrix,");
		assertContains("qliphoth sigils should lens individual organic control points", source,
				"qliphothOrganicSigilPoint(centerX, centerZ, radius, rotation,");
		assertContains("qliphoth sigils should use yellow nodes instead of blood-red or ritual-gold stars", source,
				"nodeRadius, 255, 214, 54, nodeAlpha);");
		assertContains("qliphoth sigil strokes should use yellow ink over black shadows", source,
				"0xFFD63A, redAlpha");
		assertContains("qliphoth sky pass should be gated by theme id", source,
				"if (!ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(theme.id()))");
		assertContains("qliphoth sky should render one small hole per first eight pomes", source,
				"int smallHoleCount = Math.min(8, pomeCount);");
		assertContains("qliphoth final state should render residual absorbed rings", source,
				"renderQliphothResidualRing(");
		assertContains("qliphoth final state should render the massive zenith black hole", source,
				"renderQliphothZenithBlackHole(");
		assertContains("qliphoth final black hole should be anchored above the player, not below the world", source,
				"renderQliphothZenithBlackHoleBillboard(");
		assertContains("qliphoth final black hole should render as a flat screen-facing disk", source,
				"Vec3 right = new Vec3(camera.getLeftVector()).scale(-halfSize);");
		assertContains("qliphoth final black hole should use camera up vector for a head-on billboard", source,
				"Vec3 up = new Vec3(camera.getUpVector()).scale(halfSize);");
		assertContains("qliphoth chamber zenith should use the legacy final portal without the tree-apex branch", source,
				"ringIntensity, true, false);");
		assertContains("qliphoth final black hole half size should retain the massive zenith scale", source,
				"return skyDistance * 0.435F * pulse;");
		assertNotContains("qliphoth sky renderer should not use the retired custom tendril mesh pass", source,
				"renderQliphothTendrils(");
		assertNotContains("qliphoth sky tendrils should not use persistent world-render anchors", clientEvents,
				"QliphothSkyTendrilRenderer.render(partialTick);");
		assertContains("qliphoth sky tendrils should render in the sky pass with the black holes", source,
				"renderQliphothSkyTendrils(poseStack, tesselator, time, skyDistance, pomeCount);");
		assertContains("qliphoth sky tendrils should use HutosLib tube geometry without the world-effect queue", source,
				"TendrilGeometry.generate(sourceAtBlackHole, targetTowardCamera, config");
		assertContains("qliphoth tendrils should use point anchors generated from black-hole sky directions",
				source,
				"Vec3 anchor = new Vec3(skyPoint.x, skyPoint.y, skyPoint.z);");
		assertContains("qliphoth small tendrils should share the rendered black-hole seed",
				source,
				"qliphothSmallBlackHoleSeed(hole)");
		assertContains("qliphoth small tendrils should share the rendered black-hole radius",
				source,
				"qliphothSmallBlackHoleHalfSize(time, skyDistance, hole, direction, pomeCount >= 9)");
		assertContains("qliphoth small black holes should migrate slowly from their base sky directions", source,
				"static float[] qliphothMigratedSmallBlackHoleDirection(float time, int hole, float[] direction)");
		assertContains("qliphoth small tendrils should follow the same migrated direction as their black holes",
				source,
				"renderQliphothSkyTendrilCluster(buffer, matrix, time, skyDistance, migratedDirection[0], migratedDirection[1]");
		assertContains("qliphoth small black-hole billboards should render at migrated directions", source,
				"migratedDirection[0], migratedDirection[1], halfSize, 255");
		assertContains("qliphoth residual small holes should also migrate after final communion", source,
				"migratedDirection[0], migratedDirection[1], halfSize, 82");
		assertContains("qliphoth migration should clamp pitch to stay in the visible skybox band", source,
				"Mth.clamp(direction[1] + pitchDrift, -54.0F, 58.0F)");
		assertContains("qliphoth hutoslib tendrils should pull inward toward the camera", source,
				"anchor.subtract(forward.scale(pull))");
		assertContains("qliphoth hutoslib tendrils should explicitly start inside the black-hole rim",
				source,
				"Vec3 sourceAtBlackHole = anchor.add(forward.scale(2.0D)).add(rim.scale(finalHole ? 0.58D : 0.18D));");
		assertContains("qliphoth hutoslib tendrils should explicitly end toward the player",
				source,
				"Vec3 targetTowardCamera = anchor.subtract(forward.scale(pull))");
		assertContains("qliphoth final tendrils should arc around the massive rim instead of crossing the center",
				source,
				".add(rim.scale(finalHole ? 1.12D : 0.46D))");
		assertContains("qliphoth final tendrils should get a controlled lateral endpoint offset", source,
				".add(twist.scale((finalHole ? 18.0D : 2.2D)");
		assertContains("qliphoth small tendrils should be long enough to escape the black-hole lens disk", source,
				"static final double QLIPHOTH_SMALL_TENDRIL_PULL = 58.0D;");
		assertContains("qliphoth final tendrils should be shorter while still escaping the massive lens disk", source,
				"static final double QLIPHOTH_FINAL_TENDRIL_PULL = 68.0D;");
		assertContains("qliphoth tendril pull should use the long lens-escape constants", source,
				"double pull = finalHole ? QLIPHOTH_FINAL_TENDRIL_PULL : QLIPHOTH_SMALL_TENDRIL_PULL;");
		assertContains("qliphoth hutoslib tendrils should be emitted as 3D tube quads in sky coordinates",
				source,
				"TendrilGeometry.createTubeQuads(strand, 1.0F)");
		assertContains("qliphoth final tendrils should use the large flowing final profile", source,
				"qliphothZenithBlackHoleHalfSize(time, skyDistance), 9, true);");
		assertContains("qliphoth hutoslib tendrils should keep final black-hole strand count higher", source,
				"int tendrilCount = finalHole ? 9 : 2;");
		assertContains("qliphoth final tendril tubes should be singular wide strands, not paired rails",
				source,
				".withShape(finalHole ? 36 : 18, 1, finalHole ? 0.155F : 0.052F, finalHole ? 0.18F : 0.10F)");
		assertNotContains("qliphoth final tendrils must not use two HutosLib strands per tendril",
				source,
				"finalHole ? 2 : 1");
		assertContains("qliphoth final tendrils should use high curl so the massive hole does not render straight spokes",
				source,
				"finalHole ? 4.0F : 0.46F");
		assertContains("qliphoth final tendrils should use high writhe amplitude at final-hole scale",
				source,
				"finalHole ? 2.0F : 0.10F");
		assertContains("qliphoth final tendrils should writhe faster now that they are shorter", source,
				"finalHole ? 0.095F : 0.045F");
		assertNotContains("qliphoth sky tendrils should not add live camera position to their anchors", source,
				"anchor = camera.add");
		assertContains("qliphoth tendril anchors should mirror the black-hole billboard transform on X", source,
				"-Mth.sin(yawRadians) * horizontal * distance");
		assertContains("qliphoth tendril anchors should mirror the black-hole billboard transform on Z", source,
				"-Mth.cos(yawRadians) * horizontal * distance");
		assertContains("qliphoth black holes must use shader render type", source,
				"HemoRenderTypes.qliphothBlackHole(");
		assertContains("qliphoth shader should be registered", shaderInit,
				"QLIPHOTH_BLACK_HOLE.createInstance(provider)");
		assertContains("qliphoth render type should use qliphoth shader holder", renderTypes,
				"ShaderInit.QLIPHOTH_BLACK_HOLE.getShard()");
		assertContains("qliphoth black hole shader json should sample the theme sky texture", qliphothBlackHoleShader,
				"\"Sampler0\"");
		assertContains("qliphoth black hole shader should warp sampled sky uvs", qliphothBlackHoleFragment,
				"vec2 lensedUv");
		assertContains("qliphoth black hole shader should use a strong radial lens offset, not only swirl motion",
				qliphothBlackHoleFragment,
				"vec2 lensOffset = lensDirection * lensAmount * mix(0.54, 0.82, FinalHole);");
		assertContains("qliphoth black hole shader should chromatically split lensed sky samples",
				qliphothBlackHoleFragment,
				"texture(Sampler0, fract(lensedUv + lensDirection * chromaticOffset)).r");
		assertContains("qliphoth black hole shader should keep the lensed background visible outside the event horizon",
				qliphothBlackHoleFragment,
				"float lensWindow = (1.0 - smoothstep(coreRadius + 0.30, 0.92, radius)) * smoothstep(coreRadius + 0.020, coreRadius + 0.120, radius);");
		assertContains("qliphoth black hole shader should draw a black event horizon", qliphothBlackHoleFragment,
				"eventHorizon");
		assertNotContains("qliphoth black holes must not be static pngs", registry,
				"black_hole.png");
		assertNotContains("qliphoth black holes must not be static pngs", source,
				"black_hole.png");
		assertContains("silent archon should keep harsh noise out of visible background textures", registry,
				".textures(SILENT_ARCHON_SKY, SILENT_ARCHON_CLOUDS, SILENT_ARCHON_CLOUDS, DEFAULT_NOISE)");
        assertContains("silent archon should restore the brighter grey blue-green skybox", registry,
                ".skybox(0xFF5F7472, 0xFF8CA6A2)");
        assertContains("silent archon should keep a brighter grey blue-green haze", registry,
                ".nebula(0x223031, 0x33484A, 0x55706D)");
        assertContains("renderer gates Silent Archon depth effects by theme id", source,
                "if (ChamberOfWillManager.THEME_SILENT_ARCHON.equals(theme.id()))");
        assertContains("silent archon depth pass should render broad cloud strata", source,
                "renderSilentArchonCloudStrata(poseStack, tesselator, f, skyDistance, theme);");
        assertContains("silent archon depth pass should render a lower cloud deck", source,
                "renderSilentArchonCloudDeck(poseStack, tesselator, f, skyDistance, theme);");
        assertContains("silent archon depth pass should render distant monolith silhouettes", source,
                "renderSilentArchonDistantMonolithSilhouettes(poseStack, tesselator, f, skyDistance);");
        assertContains("cloud strata should use the silent archon cloud texture already on the theme", source,
                "theme.cloudTexture()");
        assertContains("cloud strata should draw tilted planes rather than face-locked cube faces", source,
                "poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));");
        assertContains("cloud strata should drift slowly behind the pillars", source,
                "float drift = time * (0.00055F + layer * 0.00014F);");
        assertContains("cloud deck should use the silent archon cloud texture already on the theme", source,
                "RenderSystem.setShaderTexture(0, theme.cloudTexture());");
        assertContains("cloud deck should sit below the old mist horizon line", source,
                "float deckY = -skyDistance * 0.72F;");
        assertNotContains("silent archon cloud deck should not use vertical mist ring bands", source,
                "renderSilentArchonMistRing(");
        assertNotContains("silent archon cloud deck should not keep the old horizon placement", source,
                "float horizonY = -skyDistance * 0.36F;");
        assertContains("distant silhouettes should use simple color geometry", source,
                "static void renderDistantMonolithSilhouetteGeometry(");
        assertContains("distant silhouettes should avoid the animated monolith shader path", source,
                "BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);");
        assertContains("distant silhouettes should include haze-faded alpha", source,
                "int alpha = (int) Mth.clamp(Mth.lerp(depthT, 26.0F, 82.0F)");
        assertContains("distant silhouettes should be rendered before foreground animated pillars", source,
                "renderAfterNebula(context);\n\t\tRenderSystem.depthMask(true);\n\t\trenderBeforeSharedLayers(context);");
        assertNotContains("dead volumetric fog pass should not stay in the visible Silent Archon foreground stack", source,
                "renderSilentArchonVolumetricFog(poseStack, tesselator, f, skyDistance, theme);");
        assertNotContains("old foreground wisps should not stack with the procedural storm cloud bank", source,
                "renderSilentArchonForegroundCloudWisps(poseStack, tesselator, f, skyDistance, theme);");
        assertContains("foreground storm clouds should render after foreground animated pillars", source,
                "renderSilentArchonMonolithPillars(poseStack, f, skyDistance, theme);\n\t\t}\n\t\trenderSilentArchonForegroundStormClouds(poseStack, f, skyDistance, theme);");
        assertContains("volumetric fog should be Silent Archon gated through the active biome/effects theme", source,
                "if (!isSilentArchonFogTheme())");
        assertContains("Silent Archon dimension fog should tint toward pale blue-green biome fog", source,
                "return new Vec3(0.52D, 0.66D, 0.68D).scale");
        assertContains("Silent Archon dimension fog should not enable vanilla fog on monolith shaders", source,
                "default boolean isFoggyAt(int x, int y) {\n\t\treturn false;");
        assertNotContains("Silent Archon fog must not drive global fogginess because monolith shader uses linear_fog", source,
                "public boolean isFoggyAt(int x, int y) {\n        return isSilentArchonFogTheme();");
        assertContains("volumetric fog should use the Silent Archon fog render type", source,
                "HemoRenderTypes.silentArchonVolumetricFog(theme.cloudTexture(), time * 0.05F");
        assertContains("Silent Archon fog render type should be registered", shaderInit,
                "SILENT_ARCHON_FOG.createInstance(provider)");
        assertContains("Silent Archon fog render type should use the fog shader holder", renderTypes,
                "ShaderInit.SILENT_ARCHON_FOG.getShard()");
        assertContains("foreground fog must draw over monolith bases instead of being rejected by their depth", silentArchonFogRenderTypeSource,
                ".setDepthTestState(RenderType.NO_DEPTH_TEST)");
        assertNotContains("foreground fog must not use lequal depth because monoliths already wrote depth", silentArchonFogRenderTypeSource,
                ".setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)");
        assertContains("Silent Archon fog shader should sample the cloud texture", fogFragment,
                "uniform sampler2D Sampler0;");
        assertContains("Silent Archon fog shader should apply animated fog noise", fogFragment,
                "float volumeNoise = fbm");
        assertContains("volumetric fog should bind the silent archon cloud texture through the render type", renderTypes,
                "RenderStateShard.TextureStateShard(texture, false, false)");
        assertContains("volumetric fog should stack many depth slices through the monolith bases", source,
                "int fogSlices = 18;");
        assertContains("volumetric fog should build upward from the foreground pillar bases", source,
                "float fogFloorY = -skyDistance * 0.74F;");
        assertContains("background volumetric fog should stay subtle behind the storm cloud bank", source,
                "float density = Mth.lerp(t, 0.32F, 0.18F);");
        assertContains("background volumetric fog should use low alpha so it cannot read as flat foreground sprites", source,
                "int alpha = (int) Mth.clamp(Mth.lerp(t, 42.0F, 24.0F), 0.0F, 48.0F);");
        assertContains("volumetric fog should use bright pale vertex tint so it reads as fog, not dark stain", source,
                "int red = Mth.floor(Mth.lerp(t, 174.0F, 150.0F));");
        assertContains("legacy volumetric fog remains an atmospheric sheet helper, not the visible cloud bank", source,
                "renderSilentArchonVolumetricFogSheet(");
        assertContains("volumetric fog sheets should fade around monolith height instead of only on the horizon", source,
                "float verticalFade = Mth.clamp(1.0F - Math.abs(yT - 0.48F) * 1.72F, 0.0F, 1.0F);");
        assertContains("foreground storm clouds should reuse the foreground pillar placement seed", source,
                "Random random = new Random(91077L);");
        assertContains("foreground storm clouds should sit at the visible lower-shaft band, not the underground pillar base", source,
                "float cloudCenterY = -skyDistance * Mth.lerp(distanceT, 0.36F, 0.56F);");
        assertContains("foreground storm clouds should draw layered horizontal cloud banks", source,
                "renderSilentArchonStormCloudBankLayer(");
        assertContains("foreground storm clouds should subdivide banks into organic cells instead of giant panes", source,
                "renderSilentArchonStormCloudBankCell(");
        assertContains("foreground storm clouds should use a fixed cell grid to break up flat card silhouettes", source,
                "int cellColumns = 6;");
        assertNotContains("foreground storm clouds should not use vertical wisp cards that read as panels", source,
                "renderSilentArchonStormCloudWisp(");
        assertNotContains("foreground storm clouds should not use crossed billboard walls", source,
                "renderSilentArchonStormCloudCross(");
        assertContains("foreground storm clouds must not reuse the old theme cloud texture", source,
                "HemoRenderTypes.silentArchonStormCloud(time * 0.05F, stormSeed, density)");
        assertNotContains("foreground storm clouds must not bind theme.cloudTexture into the storm cloud render type", source,
                "silentArchonStormCloud(theme.cloudTexture()");
        assertContains("Silent Archon storm cloud shader should be registered", shaderInit,
                "SILENT_ARCHON_STORM_CLOUD.createInstance(provider)");
        assertContains("Silent Archon storm cloud render type should use the storm cloud shader holder", renderTypes,
                "ShaderInit.SILENT_ARCHON_STORM_CLOUD.getShard()");
        assertContains("storm cloud render type should draw over monolith bases", renderTypes,
                "RenderType.create(\"silent_archon_storm_cloud\"");
        assertContains("storm cloud render type should not depth-test against foreground monoliths", renderTypes,
                ".setDepthTestState(RenderType.NO_DEPTH_TEST)");
        assertNotContains("foreground storm clouds must not sample the reused cloud texture", stormCloudFragment,
                "sampler2D Sampler0");
        assertContains("storm cloud shader should build billows procedurally", stormCloudFragment,
                "float billow = fbm");
        assertContains("storm cloud shader should aggressively fade rectangular card edges", stormCloudFragment,
                "float cardEdge = smoothstep(0.035, 0.24, edgeDistance);");
        assertContains("storm cloud shader should avoid square cards by using a stricter rounded storm mask", stormCloudFragment,
                "float stormMask = smoothstep(0.10, 0.56, domeMask);");
        assertContains("storm cloud shader should discard fully transparent edges", stormCloudFragment,
                "if (alpha < 0.012) {");
        assertAverageBlueGreenSky("silent archon sky texture should be brighter than the near-black pass",
                SILENT_ARCHON_SKY_TEXTURE);
        assertMottledBlueGreenSky("silent archon sky texture should have lighter mottled depth",
                SILENT_ARCHON_SKY_TEXTURE);
        assertRareRedPurpleClouds("silent archon sky texture should include rare red-purple cloud bruising",
                SILENT_ARCHON_SKY_TEXTURE);
        assertRareRedBruises("silent archon sky texture should include rare red cloud bruises",
                SILENT_ARCHON_SKY_TEXTURE);
        assertSkyTextureEdgesBlend("silent archon sky texture should hide skybox face seams",
                SILENT_ARCHON_SKY_TEXTURE);
		assertCloudTextureAvoidsRepeatedBlobIslands("silent archon cloud texture should avoid repeated figure-eight blobs",
				SILENT_ARCHON_CLOUD_TEXTURE);
		assertQliphothSkyPalette("qliphoth sky texture should read as dark red-purple-blue mist",
				QLIPHOTH_SKY_TEXTURE);
		assertSkyTextureEdgesBlend("qliphoth sky texture should hide skybox face seams",
				QLIPHOTH_SKY_TEXTURE);
		assertQliphothCloudMist("qliphoth cloud texture should provide translucent mist",
				QLIPHOTH_CLOUD_TEXTURE);
		assertContains("silent archon should avoid shattered rotated cloud boxes", registry,
				".toggles(true, false, true, false)");
        assertNotContains("silent archon must not use procedural noise as every sky texture", registry,
                ".textures(DEFAULT_NOISE, DEFAULT_NOISE, DEFAULT_NOISE, DEFAULT_NOISE)");
        assertContains("renderer gates monolith pillars by theme", source,
                "if (theme.renderMonolithPillars())");
        assertContains("renderer uses the monolith block-entity shader path", source,
                "HemoRenderTypes.monolithEntitySurface");
        assertContains("renderer draws scattered pillar layer", source,
                "renderSilentArchonMonolithPillars(poseStack, f, skyDistance, theme);");
        assertContains("sky overlay haze should not write depth before monolith pillars", source,
                "RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);\n\t\tRenderSystem.depthMask(false);");
        assertContains("sky overlay depth writes should be restored before monolith pillars", source,
                "RenderSystem.depthMask(true);\n\t\trenderBeforeSharedLayers(context);");
		assertContains("membrane pulse should restore blending after shader render types clear state",
				membranePulseSource,
				"RenderSystem.enableBlend();\n        RenderSystem.disableDepthTest();");
		assertContains("small qliphoth black holes should use opaque quads so foreground roots cannot show through the event horizon",
				qliphothSmallHoleSource,
				"migratedDirection[0], migratedDirection[1], halfSize, 255, qliphothSmallBlackHoleSeed(hole)");
        assertContains("nebula overlays should render on every physical skybox face", source,
                "for (int face = 0; face < 6; face++) {\n            poseStack.pushPose();\n            rotateSkyFace(poseStack, face);");
        assertContains("nebula overlays should seed each skybox face independently", source,
                "Random faceRandom = new Random(layerSeed + face * 72931L);");
        assertContains("nebula overlay planes should use face-local volume clusters", source,
                "nextZoff = Math.max(nextZoff, renderNebulaFaceVolume(poseStack, faceRandom, f, skyDistance, tesselator, scale, zoff + face * 0.002F, wispTexture));");
        assertContains("nebula face volume should use off-center anchor lanes", source,
                "int clusterCount = 8;");
        assertContains("nebula face volume should bias anchors toward the face perimeter", source,
                "float anchorX = nebulaAnchorX(lane, random) * skyDistance * scale;");
        assertContains("nebula face volume should keep 3D depth offsets", source,
                "float depth = zoff + cluster * 0.012F + puff * 0.018F;");
        assertContains("nebula face volume should restore non-planar pitch", nebulaFaceSource,
                "poseStack.mulPose(Axis.XP.rotationDegrees(pitch));");
        assertContains("nebula face volume should restore non-planar roll", nebulaFaceSource,
                "poseStack.mulPose(Axis.ZP.rotationDegrees(roll));");
        assertNotContains("nebula face volume should not flatten into face wash layers", nebulaFaceSource,
                "int hazeLayers = 3;");
        assertNotContains("nebula face volume should not use one full-face haze plane per layer", nebulaFaceSource,
                "1.08F, 0xFF3A3A3A");
        assertContains("pillar placement should be generated as opposite pairs", source,
                "int pillarPairs = count / 2;");
        assertContains("pillar placement should iterate paired samples", source,
                "for (int pair = 0; pair < pillarPairs; pair++)");
        assertContains("pillar placement should render the opposite compass partner", source,
                "angle + Mth.TWO_PI * 0.5F");
        assertNotContains("pillar placement should not be a single unpaired full-circle pass", source,
                "float ringT = pillar / (float) Math.max(1, count);");
        assertContains("pillar geometry should precompute shared seam corners", source,
                "float[] cornerX = new float[sideCount + 1];");
        assertContains("pillar geometry should precompute shared seam heights", source,
                "float[] cornerTop = new float[sideCount + 1];");
        assertContains("pillar geometry should reuse the next shared corner", source,
                "float x1 = cornerX[side + 1];");
        assertContains("pillar geometry should close the final horizontal seam", source,
                "cornerX[sideCount] = cornerX[0];");
        assertContains("pillar geometry should close the final vertical seam", source,
                "cornerTop[sideCount] = cornerTop[0];");
        assertNotContains("pillar faces must not compute independent right-edge radii", source,
                "float r1 = radius * (0.86F + ((seed + side * 23 + 3) % 7) * 0.035F);");
    }

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
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

    private static void assertBefore(String label, String source, String first, String second) {
        int firstIndex = source.indexOf(first);
        int secondIndex = source.indexOf(second);
        if (firstIndex < 0) {
            throw new AssertionError(label + ": missing first marker " + first);
        }
        if (secondIndex < 0) {
            throw new AssertionError(label + ": missing second marker " + second);
        }
        if (firstIndex >= secondIndex) {
            throw new AssertionError(label + ": expected " + first + " before " + second);
        }
    }

    private static void assertAverageBlueGreenSky(String label, Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new AssertionError(label + ": could not read " + path);
        }
        long red = 0;
        long green = 0;
        long blue = 0;
        long pixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 255;
                if (alpha == 0) {
                    continue;
                }
                red += (argb >>> 16) & 255;
                green += (argb >>> 8) & 255;
                blue += argb & 255;
                pixels++;
            }
        }
        int avgRed = Math.round(red / (float) pixels);
        int avgGreen = Math.round(green / (float) pixels);
        int avgBlue = Math.round(blue / (float) pixels);
        int luminance = Math.round(avgRed * 0.2126F + avgGreen * 0.7152F + avgBlue * 0.0722F);
        if (luminance < 86 || luminance > 126 || avgGreen < avgRed + 8 || avgBlue < avgRed + 6) {
            throw new AssertionError(label + ": average rgb " + avgRed + "," + avgGreen + "," + avgBlue
                    + " luminance " + luminance);
        }
    }

    private static void assertMottledBlueGreenSky(String label, Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new AssertionError(label + ": could not read " + path);
        }
        int[] luminance = new int[image.getWidth() * image.getHeight()];
        int pixels = 0;
        long total = 0;
        long totalSquares = 0;
        int brightCloudPixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 255;
                if (alpha == 0) {
                    continue;
                }
                int red = (argb >>> 16) & 255;
                int green = (argb >>> 8) & 255;
                int blue = argb & 255;
                int value = Math.round(red * 0.2126F + green * 0.7152F + blue * 0.0722F);
                luminance[pixels++] = value;
                total += value;
                totalSquares += (long) value * value;
                if (value >= 136) {
                    brightCloudPixels++;
                }
            }
        }
        java.util.Arrays.sort(luminance, 0, pixels);
        float average = total / (float) pixels;
        float variance = totalSquares / (float) pixels - average * average;
        float standardDeviation = (float) Math.sqrt(Math.max(0.0F, variance));
        int p05 = luminance[Math.max(0, Math.round(pixels * 0.05F) - 1)];
        int p95 = luminance[Math.min(pixels - 1, Math.round(pixels * 0.95F) - 1)];
        int p98 = luminance[Math.min(pixels - 1, Math.round(pixels * 0.98F) - 1)];
        float brightCloudCoverage = brightCloudPixels / (float) pixels;
        if (standardDeviation < 10.0F || p95 - p05 < 34 || p95 < 120 || p98 < 142
                || brightCloudCoverage < 0.018F) {
            throw new AssertionError(label + ": luminance sd " + standardDeviation + " p05 " + p05
                    + " p95 " + p95 + " p98 " + p98 + " bright coverage " + brightCloudCoverage);
        }
    }

    private static void assertSkyTextureEdgesBlend(String label, Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new AssertionError(label + ": could not read " + path);
        }
        int width = image.getWidth();
        int height = image.getHeight();
        float horizontalEdgeDifference = 0.0F;
        float verticalEdgeDifference = 0.0F;
        for (int y = 0; y < height; y++) {
            horizontalEdgeDifference += Math.abs(luminance(image.getRGB(0, y))
                    - luminance(image.getRGB(width - 1, y)));
        }
        for (int x = 0; x < width; x++) {
            verticalEdgeDifference += Math.abs(luminance(image.getRGB(x, 0))
                    - luminance(image.getRGB(x, height - 1)));
        }
        horizontalEdgeDifference /= height;
        verticalEdgeDifference /= width;
        float cornerDifference = Math.abs(luminance(image.getRGB(0, 0)) - luminance(image.getRGB(width - 1, 0)))
                + Math.abs(luminance(image.getRGB(0, height - 1)) - luminance(image.getRGB(width - 1, height - 1)))
                + Math.abs(luminance(image.getRGB(0, 0)) - luminance(image.getRGB(0, height - 1)))
                + Math.abs(luminance(image.getRGB(width - 1, 0)) - luminance(image.getRGB(width - 1, height - 1)));
        if (horizontalEdgeDifference > 3.5F || verticalEdgeDifference > 3.5F || cornerDifference > 16.0F) {
            throw new AssertionError(label + ": edge diffs horizontal " + horizontalEdgeDifference
                    + " vertical " + verticalEdgeDifference + " corners " + cornerDifference);
        }
    }

    private static void assertRareRedPurpleClouds(String label, Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new AssertionError(label + ": could not read " + path);
        }
        int bruisedPixels = 0;
        int pixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 255;
                if (alpha == 0) {
                    continue;
                }
                int red = (argb >>> 16) & 255;
                int green = (argb >>> 8) & 255;
                int blue = argb & 255;
                pixels++;
                if (red >= 92 && blue >= 104 && red > green - 10 && blue > green - 4) {
                    bruisedPixels++;
                }
            }
        }
        float coverage = bruisedPixels / (float) pixels;
        if (coverage < 0.006F || coverage > 0.260F) {
            throw new AssertionError(label + ": red-purple coverage " + coverage);
        }
    }

	private static void assertRareRedBruises(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": could not read " + path);
		}
        int redBruisePixels = 0;
        int pixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 255;
                if (alpha == 0) {
                    continue;
                }
                int red = (argb >>> 16) & 255;
                int green = (argb >>> 8) & 255;
                int blue = argb & 255;
                pixels++;
                if (red >= 108 && red > green + 8 && red > blue + 4) {
                    redBruisePixels++;
                }
            }
        }
        float coverage = redBruisePixels / (float) pixels;
        if (coverage < 0.0001F || coverage > 0.045F) {
            throw new AssertionError(label + ": red bruise coverage " + coverage);
		}
	}

	private static void assertQliphothSkyPalette(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": could not read " + path);
		}
		long red = 0;
		long green = 0;
		long blue = 0;
		long luminanceTotal = 0;
		int purpleBluePixels = 0;
		int bloodMistPixels = 0;
		int brightMistPixels = 0;
		int pixels = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				int alpha = (argb >>> 24) & 255;
				if (alpha == 0) {
					continue;
				}
				int r = (argb >>> 16) & 255;
				int g = (argb >>> 8) & 255;
				int b = argb & 255;
				int lum = Math.round(r * 0.2126F + g * 0.7152F + b * 0.0722F);
				red += r;
				green += g;
				blue += b;
				luminanceTotal += lum;
				pixels++;
				if (b >= 46 && r >= 34 && b > g + 8 && r > g + 4) {
					purpleBluePixels++;
				}
				if (r >= 66 && r > g + 12 && r >= b - 6) {
					bloodMistPixels++;
				}
				if (lum >= 74 && b > g && r > g - 8) {
					brightMistPixels++;
				}
			}
		}
		int avgRed = Math.round(red / (float) pixels);
		int avgGreen = Math.round(green / (float) pixels);
		int avgBlue = Math.round(blue / (float) pixels);
		int avgLuminance = Math.round(luminanceTotal / (float) pixels);
		float purpleBlueCoverage = purpleBluePixels / (float) pixels;
		float bloodMistCoverage = bloodMistPixels / (float) pixels;
		float brightMistCoverage = brightMistPixels / (float) pixels;
		if (avgLuminance < 28 || avgLuminance > 76 || avgGreen > avgRed - 2 || avgBlue < avgGreen + 8
				|| purpleBlueCoverage < 0.18F || bloodMistCoverage < 0.035F || brightMistCoverage < 0.035F) {
			throw new AssertionError(label + ": average rgb " + avgRed + "," + avgGreen + "," + avgBlue
					+ " luminance " + avgLuminance + " purple-blue coverage " + purpleBlueCoverage
					+ " blood coverage " + bloodMistCoverage + " bright mist coverage " + brightMistCoverage);
		}
	}

	private static void assertQliphothCloudMist(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": could not read " + path);
		}
		int visible = 0;
		int translucent = 0;
		int darkPurple = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				int alpha = (argb >>> 24) & 255;
				if (alpha <= 6) {
					continue;
				}
				int red = (argb >>> 16) & 255;
				int green = (argb >>> 8) & 255;
				int blue = argb & 255;
				visible++;
				if (alpha < 210) {
					translucent++;
				}
				if (red >= 28 && blue >= 34 && green <= Math.max(red, blue) - 2) {
					darkPurple++;
				}
			}
		}
		if (visible == 0) {
			throw new AssertionError(label + ": no visible mist pixels");
		}
		float translucentShare = translucent / (float) visible;
		float darkPurpleShare = darkPurple / (float) visible;
		if (translucentShare < 0.65F || darkPurpleShare < 0.34F) {
			throw new AssertionError(label + ": translucent share " + translucentShare
					+ " dark purple share " + darkPurpleShare);
		}
	}

	private static float luminance(int argb) {
		int red = (argb >>> 16) & 255;
		int green = (argb >>> 8) & 255;
		int blue = argb & 255;
		return red * 0.2126F + green * 0.7152F + blue * 0.0722F;
    }

    private static void assertCloudTextureAvoidsRepeatedBlobIslands(String label, Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new AssertionError(label + ": could not read " + path);
        }
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[] visited = new boolean[width * height];
        int largest = 0;
        int secondLargest = 0;
        int visiblePixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (visited[index]) {
                    continue;
                }
                int alpha = (image.getRGB(x, y) >>> 24) & 255;
                if (alpha < 24) {
                    visited[index] = true;
                    continue;
                }
                int componentSize = floodVisibleCloudComponent(image, visited, x, y);
                visiblePixels += componentSize;
                if (componentSize > largest) {
                    secondLargest = largest;
                    largest = componentSize;
                } else if (componentSize > secondLargest) {
                    secondLargest = componentSize;
                }
            }
        }
        if (visiblePixels == 0) {
            throw new AssertionError(label + ": no visible cloud pixels");
        }
        float largestShare = largest / (float) visiblePixels;
        float secondLargestShare = secondLargest / (float) visiblePixels;
        if (largestShare < 0.74F || secondLargestShare > 0.18F) {
            throw new AssertionError(label + ": largest share " + largestShare + " second share "
                    + secondLargestShare);
        }
    }

    private static int floodVisibleCloudComponent(BufferedImage image, boolean[] visited, int startX, int startY) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] queueX = new int[width * height];
        int[] queueY = new int[width * height];
        int read = 0;
        int write = 0;
        int size = 0;
        queueX[write] = startX;
        queueY[write++] = startY;
        visited[startY * width + startX] = true;
        while (read < write) {
            int x = queueX[read];
            int y = queueY[read++];
            size++;
            for (int direction = 0; direction < 4; direction++) {
                int nextX = x + (direction == 0 ? 1 : direction == 1 ? -1 : 0);
                int nextY = y + (direction == 2 ? 1 : direction == 3 ? -1 : 0);
                if (nextX < 0 || nextY < 0 || nextX >= width || nextY >= height) {
                    continue;
                }
                int index = nextY * width + nextX;
                if (visited[index]) {
                    continue;
                }
                int alpha = (image.getRGB(nextX, nextY) >>> 24) & 255;
                visited[index] = true;
                if (alpha >= 24) {
                    queueX[write] = nextX;
                    queueY[write++] = nextY;
                }
            }
        }
        return size;
    }
}
