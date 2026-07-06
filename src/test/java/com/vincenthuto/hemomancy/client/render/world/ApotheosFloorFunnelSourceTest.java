package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApotheosFloorFunnelSourceTest {
	private static final Path APOTHEOS_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ApotheosChamberEffects.java");
	private static final Path REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_floor_funnel.json");
	private static final Path SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_floor_funnel.vsh");
	private static final Path SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_floor_funnel.fsh");
	private static final Path HAZE_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_haze.json");
	private static final Path HAZE_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_haze.vsh");
	private static final Path HAZE_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_haze.fsh");
	private static final Path GLOW_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_glow.json");
	private static final Path GLOW_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_glow.vsh");
	private static final Path GLOW_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_portal_glow.fsh");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private ApotheosFloorFunnelSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertFileExists("apotheos effects class", APOTHEOS_EFFECTS);
		assertFileExists("apotheos floor shader json", SHADER_JSON);
		assertFileExists("apotheos floor vertex shader", SHADER_VERTEX);
		assertFileExists("apotheos floor fragment shader", SHADER_FRAGMENT);
		assertFileExists("apotheos portal haze shader json", HAZE_SHADER_JSON);
		assertFileExists("apotheos portal haze vertex shader", HAZE_SHADER_VERTEX);
		assertFileExists("apotheos portal haze fragment shader", HAZE_SHADER_FRAGMENT);
		assertFileExists("apotheos portal glow shader json", GLOW_SHADER_JSON);
		assertFileExists("apotheos portal glow vertex shader", GLOW_SHADER_VERTEX);
		assertFileExists("apotheos portal glow fragment shader", GLOW_SHADER_FRAGMENT);

		String apotheosEffects = read(APOTHEOS_EFFECTS);
		String registry = read(REGISTRY);
		String shaderInit = read(SHADER_INIT);
		String renderTypes = read(RENDER_TYPES);
		String shaderJson = read(SHADER_JSON);
		String vertexShader = read(SHADER_VERTEX);
		String fragmentShader = read(SHADER_FRAGMENT);
		String hazeShaderJson = read(HAZE_SHADER_JSON);
		String hazeVertexShader = read(HAZE_SHADER_VERTEX);
		String hazeFragmentShader = read(HAZE_SHADER_FRAGMENT);
		String glowShaderJson = read(GLOW_SHADER_JSON);
		String glowVertexShader = read(GLOW_SHADER_VERTEX);
		String glowFragmentShader = read(GLOW_SHADER_FRAGMENT);
		String reference = read(REFERENCE);

		assertContains("registry registers dedicated apotheos effects", registry,
				"new ApotheosChamberEffects(apotheos)");
		assertContains("registry keeps apotheos shader motion alive", registry,
				".motion(1.0F)\n\t\t\t\t.layers(0, 0, 0, 0)");
		assertNotContains("registry should not keep apotheos blank-slate renderer", registry,
				"new BlankChamberThemeEffects(apotheos)");

		assertContains("shader init declares apotheos floor shader", shaderInit,
				"APOTHEOS_FLOOR_FUNNEL");
		assertContains("shader init declares apotheos portal haze shader", shaderInit,
				"APOTHEOS_PORTAL_HAZE");
		assertContains("shader init declares apotheos portal glow shader", shaderInit,
				"APOTHEOS_PORTAL_GLOW");
		assertContains("shader init uses world apotheos shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_floor_funnel\")");
		assertContains("shader init uses world apotheos portal haze shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_portal_haze\")");
		assertContains("shader init uses world apotheos portal glow shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_portal_glow\")");
		assertContains("shader init registers apotheos floor shader", shaderInit,
				"registerShader(event, APOTHEOS_FLOOR_FUNNEL.createInstance(provider));");
		assertContains("shader init registers apotheos portal haze shader", shaderInit,
				"registerShader(event, APOTHEOS_PORTAL_HAZE.createInstance(provider));");
		assertContains("shader init registers apotheos portal glow shader", shaderInit,
				"registerShader(event, APOTHEOS_PORTAL_GLOW.createInstance(provider));");
		assertContains("shader init requests ring rise uniform", shaderInit,
				"\"RingRise\"");
		assertContains("shader init requests center void uniform", shaderInit,
				"\"CenterVoidRadius\"");

		assertContains("render type method exists", renderTypes,
				"public static RenderType apotheosFloorFunnel(");
		assertContains("portal haze render type method exists", renderTypes,
				"public static RenderType apotheosPortalHaze(");
		assertContains("portal glow render type method exists", renderTypes,
				"public static RenderType apotheosPortalGlow(");
		assertContains("render type uses apotheos shader shard", renderTypes,
				"ShaderInit.APOTHEOS_FLOOR_FUNNEL.getShard()");
		assertContains("portal haze render type uses apotheos haze shader shard", renderTypes,
				"ShaderInit.APOTHEOS_PORTAL_HAZE.getShard()");
		assertContains("portal glow render type uses apotheos glow shader shard", renderTypes,
				"ShaderInit.APOTHEOS_PORTAL_GLOW.getShard()");
		assertContains("render type uploads ring speed", renderTypes,
				"setUniform(shader, \"RingSpeed\", ringSpeed);");
		assertContains("render type uploads center void radius", renderTypes,
				"setUniform(shader, \"CenterVoidRadius\", centerVoidRadius);");
		assertContains("render type disables depth test for skybox-space floor funnel", renderTypes,
				"RenderType.NO_DEPTH_TEST");
		assertContains("render type uses color-only write mask", renderTypes,
				"RenderType.COLOR_WRITE");

		assertContains("apotheos effects extend shared chamber effects", apotheosEffects,
				"extends AbstractChamberThemeEffects");
		assertContains("apotheos effects render in the chamber sky pass", apotheosEffects,
				"protected void renderBeforeSharedLayers(ChamberThemeRenderContext context)");
		assertContains("apotheos effects draw the floor funnel before future wall and ceiling passes", apotheosEffects,
				"renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance())");
		assertContains("apotheos effects draw the portal glow before the floor funnel", apotheosEffects,
				"renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosFloorFunnel");
		assertContains("apotheos effects draw a second portal haze layer", apotheosEffects,
				"renderApotheosPortalHaze");
		assertContains("apotheos effects draw a separate third portal glow layer", apotheosEffects,
				"renderApotheosPortalGlow");
		assertContains("apotheos effects extends portal glow toward the green rings", apotheosEffects,
				"APOTHEOS_PORTAL_GLOW_RADIUS = 0.52F");
		assertContains("apotheos effects keep a true central aperture", apotheosEffects,
				"APOTHEOS_FLOOR_INNER_RADIUS_SCALE = 0.018F");
		assertContains("apotheos effects shrink the shader center void", apotheosEffects,
				"APOTHEOS_FLOOR_CENTER_VOID_RADIUS = 0.095F");
		assertContains("apotheos effects lower the whole floor farther below the platform", apotheosEffects,
				"APOTHEOS_FLOOR_Y_SCALE = -0.30F");
		assertContains("apotheos effects deepen the central funnel drop for a steeper descent", apotheosEffects,
				"APOTHEOS_FLOOR_DROP_SCALE = 0.56F");
		assertContains("apotheos effects raise outer rings relative to the lower floor", apotheosEffects,
				"APOTHEOS_FLOOR_RISE_SCALE = 0.095F");
		assertContains("apotheos effects build an annular funnel mesh", apotheosEffects,
				"emitApotheosFunnelMesh");
		assertContains("apotheos effects use radial subdivisions for concentric rings", apotheosEffects,
				"APOTHEOS_FLOOR_RADIAL_SEGMENTS");
		assertContains("apotheos effects use angular subdivisions for a circular aperture", apotheosEffects,
				"APOTHEOS_FLOOR_RING_SEGMENTS");
		assertContains("apotheos effects send shader time for outward ring travel", apotheosEffects,
				"time * APOTHEOS_FLOOR_SHADER_TIME_SCALE");
		assertContains("apotheos effects scale from chamber sky distance", apotheosEffects,
				"skyDistance *");
		assertNotContains("apotheos floor should not be physical chamber terrain", apotheosEffects,
				"ChamberOfWillManager.FLOOR_Y");
		assertNotContains("apotheos floor should not depend on synced room radius", apotheosEffects,
				"ChamberOfWillClientData.radius()");
		assertNotContains("apotheos floor should not be a render-level event hook", apotheosEffects,
				"RenderLevelStageEvent");

		assertContains("shader json points to vertex program", shaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_floor_funnel\"");
		assertContains("shader json exposes ring speed", shaderJson,
				"\"name\": \"RingSpeed\"");
		assertContains("shader json exposes meat noise scale", shaderJson,
				"\"name\": \"MeatNoiseScale\"");
		assertContains("shader json exposes center void radius", shaderJson,
				"\"name\": \"CenterVoidRadius\"");
		assertContains("shader json defaults to a tighter center void", shaderJson,
				"\"name\": \"CenterVoidRadius\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.095 ]");
		assertContains("portal haze shader json points to vertex program", hazeShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_portal_haze\"");
		assertContains("portal haze shader json exposes outward speed", hazeShaderJson,
				"\"name\": \"OutwardSpeed\"");
		assertContains("portal haze shader json exposes center void radius", hazeShaderJson,
				"\"name\": \"CenterVoidRadius\"");
		assertContains("portal glow shader json points to vertex program", glowShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_portal_glow\"");
		assertContains("portal glow shader json exposes glow intensity", glowShaderJson,
				"\"name\": \"GlowIntensity\"");
		assertContains("portal glow shader json exposes glow radius", glowShaderJson,
				"\"name\": \"GlowRadius\"");
		assertContains("portal glow shader json exposes center void radius", glowShaderJson,
				"\"name\": \"CenterVoidRadius\"");

		assertContains("vertex shader lowers the center into a funnel", vertexShader,
				"funnelDrop");
		assertContains("vertex shader steepens the lower funnel curve", vertexShader,
				"pow(1.0 - radial, 1.72)");
		assertContains("vertex shader raises outer bands gradually", vertexShader,
				"outerRise");
		assertContains("vertex shader passes funnel depth to fragment shader", vertexShader,
				"funnelDepth =");
		assertContains("vertex shader keeps the center aperture dark and open", vertexShader,
				"centerAperture =");
		assertNotContains("vertex shader should not use Java float suffixes", vertexShader,
				"2f");
		assertContains("portal haze vertex shader passes radial distance", hazeVertexShader,
				"radialDistance =");
		assertContains("portal haze vertex shader keeps the center aperture dark and open", hazeVertexShader,
				"centerAperture =");
		assertContains("portal glow vertex shader passes radial distance", glowVertexShader,
				"radialDistance =");
		assertContains("portal glow vertex shader keeps the center aperture dark and open", glowVertexShader,
				"centerAperture =");

		assertContains("fragment shader computes radial distance", fragmentShader,
				"radialDistance");
		assertContains("fragment shader converts angular uv to seam-safe circle coordinates", fragmentShader,
				"unitCircle");
		assertNotContains("fragment shader should not drive meat noise from non-wrapping raw angle", fragmentShader,
				"angle * 0.74");
		assertNotContains("fragment shader should not drive fiber noise from non-wrapping raw angle", fragmentShader,
				"angle * 1.91");
		assertNotContains("fragment shader should not drive vein noise from non-wrapping raw angle", fragmentShader,
				"angle * 3.7");
		assertContains("fragment shader animates expanding concentric rings", fragmentShader,
				"expandingRing");
		assertContains("fragment shader uses dense base concentric ring spacing", fragmentShader,
				"float ringDensity = 34.0");
		assertContains("fragment shader widens primary bands", fragmentShader,
				"float denseWideRing");
		assertContains("fragment shader tightens bands near the portal start", fragmentShader,
				"portalStartTightness");
		assertContains("fragment shader eases bands wider toward the outer edge", fragmentShader,
				"outwardBandWidening");
		assertContains("fragment shader varies ring width with soft procedural noise", fragmentShader,
				"ringWidthNoise");
		assertContains("fragment shader adds extra narrow striations near rainbow color changes", fragmentShader,
				"colorChangeStriations");
		assertContains("fragment shader softens rainbow color-change striations", fragmentShader,
				"softColorChangeStriations");
		assertContains("fragment shader keeps an empty black center", fragmentShader,
				"centerVoid");
		assertContains("fragment shader breaks bands with meaty noise", fragmentShader,
				"meatNoise");
		assertContains("fragment shader creates mostly dark clotted red material", fragmentShader,
				"clottedRed");
		assertContains("fragment shader keeps black gaps between rings", fragmentShader,
				"blackGap");
		assertContains("fragment shader narrows the perceived black gaps", fragmentShader,
				"narrowGapDarkening");
		assertContains("fragment shader computes inter-ring space for denser filled gaps", fragmentShader,
				"interRingSpace");
		assertContains("fragment shader broadens colored fill across gaps", fragmentShader,
				"broadGapFill");
		assertContains("fragment shader fills dark gaps between rings with haze", fragmentShader,
				"interRingHaze");
		assertContains("fragment shader creates soft drifting fog between rings", fragmentShader,
				"driftingInterRingFog");
		assertNotContains("fragment shader should not create discrete particle speckles", fragmentShader,
				"particleSpeckle");
		assertNotContains("fragment shader should not name the soft fog as particles", fragmentShader,
				"interRingParticles");
		assertContains("fragment shader adds rare arterial highlights", fragmentShader,
				"arterialHighlight");
		assertContains("fragment shader adds occasional white-hot ring cuts", fragmentShader,
				"whiteHotSheen");
		assertContains("fragment shader confines rainbow to the portal edge", fragmentShader,
				"portalRainbowMask");
		assertContains("fragment shader maps rainbow across the start of the rings", fragmentShader,
				"portalRainbowProgress");
		assertContains("fragment shader defines prism red", fragmentShader,
				"prismRed");
		assertContains("fragment shader defines prism magenta", fragmentShader,
				"prismMagenta");
		assertContains("fragment shader defines prism purple", fragmentShader,
				"prismPurple");
		assertContains("fragment shader defines prism lilac", fragmentShader,
				"prismLilac");
		assertContains("fragment shader defines prism light blue", fragmentShader,
				"prismLightBlue");
		assertContains("fragment shader defines prism cyan", fragmentShader,
				"prismCyan");
		assertContains("fragment shader defines prism green", fragmentShader,
				"prismGreen");
		assertContains("fragment shader defines prism yellow", fragmentShader,
				"prismYellow");
		assertContains("fragment shader defines prism orange", fragmentShader,
				"prismOrange");
		assertContains("fragment shader returns portal rainbow to red", fragmentShader,
				"mix(prismOrange, prismRed");
		assertNotContains("fragment shader should not require a static texture sampler", fragmentShader,
				"sampler2D");
		assertNotContains("fragment shader should not use Java float suffixes", fragmentShader,
				"2f");
		assertContains("portal haze fragment shader creates pink-purple haze flecks", hazeFragmentShader,
				"pinkPurpleFlecks");
		assertContains("portal haze fragment shader launches flecks out of the portal", hazeFragmentShader,
				"outwardPortalFlow");
		assertContains("portal haze fragment shader keeps flecks strongest near the portal rim", hazeFragmentShader,
				"portalRimEmitter");
		assertContains("portal haze fragment shader uses soft haze instead of hard particles", hazeFragmentShader,
				"softFleckHaze");
		assertContains("portal haze fragment shader makes flecks smaller", hazeFragmentShader,
				"smallerHazeFlecks");
		assertContains("portal haze fragment shader makes flecks brighter", hazeFragmentShader,
				"brighterHazeCore");
		assertContains("portal haze fragment shader slows outward fleck travel", hazeFragmentShader,
				"slowerOutwardFlow");
		assertContains("portal haze fragment shader increases fleck count", hazeFragmentShader,
				"numerousHazeFlecks");
		assertContains("portal haze fragment shader keeps motes visible independent of base rings", hazeFragmentShader,
				"standaloneMoteVisibility");
		assertContains("portal haze fragment shader adds self-lit mote emission", hazeFragmentShader,
				"selfLitMoteEmission");
		assertContains("portal haze fragment shader gives motes their own opacity floor", hazeFragmentShader,
				"moteOpacityFloor");
		assertContains("portal haze fragment shader builds a discrete dust mote field", hazeFragmentShader,
				"dustMoteField");
		assertContains("portal haze fragment shader renders individual dust motes instead of clouds", hazeFragmentShader,
				"individualDustMotes");
		assertContains("portal haze fragment shader ejects motes from the void rim", hazeFragmentShader,
				"voidEjectedDustMotes");
		assertContains("portal haze fragment shader keeps dust motes visible long enough to read", hazeFragmentShader,
				"longLivedDustMotes");
		assertContains("portal haze fragment shader concentrates dust motes at the black-to-red rim", hazeFragmentShader,
				"voidRimConcentration");
		assertContains("portal haze fragment shader fades motes before the magenta-to-cyan bands", hazeFragmentShader,
				"magentaCyanFalloff");
		assertContains("portal haze fragment shader limits mote field to the first few rings", hazeFragmentShader,
				"firstFewRingMoteField");
		assertContains("portal haze fragment shader boosts mote density at the inner portal edge", hazeFragmentShader,
				"portalEdgeDensityBoost");
		assertContains("portal haze fragment shader tapers the visible mote field at its edge", hazeFragmentShader,
				"visibleFieldEdgeTaper");
		assertContains("portal haze fragment shader suppresses motes by the cyan-magenta area", hazeFragmentShader,
				"cyanMagentaMoteSuppression");
		assertNotContains("portal haze fragment shader should not require a static texture sampler", hazeFragmentShader,
				"sampler2D");
		assertContains("portal glow fragment shader creates a subtle back glow", glowFragmentShader,
				"subtlePortalBackGlow");
		assertContains("portal glow fragment shader keeps the portal center pitch black", glowFragmentShader,
				"pitchBlackCenter");
		assertContains("portal glow fragment shader fades by roughly one third of the visible portal", glowFragmentShader,
				"oneThirdGlowFalloff");
		assertContains("portal glow fragment shader extends luminance toward green layers", glowFragmentShader,
				"extendedGreenLayerGlowReach");
		assertContains("portal glow fragment shader feathers the outer glow edge softly", glowFragmentShader,
				"softOuterGlowFeather");
		assertContains("portal glow fragment shader adds ring luminance without replacing rings", glowFragmentShader,
				"ringLuminanceBacklight");
		assertContains("portal glow fragment shader has enough opacity headroom to survive later ring compositing",
				glowFragmentShader, "glowOpacityHeadroom");
		assertContains("portal glow fragment shader boosts behind-ring visibility after later layers blend over it",
				glowFragmentShader, "behindRingGlowSurvival");
		assertNotContains("portal glow fragment shader should not require a static texture sampler", glowFragmentShader,
				"sampler2D");

		assertContains("reference documents apotheos floor funnel", reference,
				"APOTHEOS floor funnel");
		assertContains("reference documents renderer-only floor treatment", reference,
				"renderer-only");
		assertContains("reference documents pending wall and ceiling passes", reference,
				"walls and ceiling remain pending");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
