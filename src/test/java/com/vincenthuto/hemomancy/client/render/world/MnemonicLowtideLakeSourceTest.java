package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicLowtideLakeSourceTest {
	private static final Path LOWTIDE_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java");
	private static final Path REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json");
	private static final Path SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh");
	private static final Path SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh");
	private static final Path SKYBOX_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox.json");
	private static final Path SKYBOX_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox.vsh");
	private static final Path SKYBOX_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox.fsh");
	private static final Path SKYBOX_BASE_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox_base.json");
	private static final Path SKYBOX_BASE_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox_base.vsh");
	private static final Path SKYBOX_BASE_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_skybox_base.fsh");
	private static final Path SURFACE_FOG_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_surface_fog.json");
	private static final Path SURFACE_FOG_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_surface_fog.vsh");
	private static final Path SURFACE_FOG_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_surface_fog.fsh");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private MnemonicLowtideLakeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertFileExists("lowtide effects class", LOWTIDE_EFFECTS);
		assertFileExists("lowtide shader json", SHADER_JSON);
		assertFileExists("lowtide vertex shader", SHADER_VERTEX);
		assertFileExists("lowtide fragment shader", SHADER_FRAGMENT);
		assertFileExists("lowtide tunnel skybox shader json", SKYBOX_SHADER_JSON);
		assertFileExists("lowtide tunnel skybox vertex shader", SKYBOX_SHADER_VERTEX);
		assertFileExists("lowtide tunnel skybox fragment shader", SKYBOX_SHADER_FRAGMENT);
		assertFileExists("lowtide nodule skybox base shader json", SKYBOX_BASE_SHADER_JSON);
		assertFileExists("lowtide nodule skybox base vertex shader", SKYBOX_BASE_SHADER_VERTEX);
		assertFileExists("lowtide nodule skybox base fragment shader", SKYBOX_BASE_SHADER_FRAGMENT);
		assertFileDoesNotExist("obsolete lowtide surface fog shader json", SURFACE_FOG_SHADER_JSON);
		assertFileDoesNotExist("obsolete lowtide surface fog vertex shader", SURFACE_FOG_SHADER_VERTEX);
		assertFileDoesNotExist("obsolete lowtide surface fog fragment shader", SURFACE_FOG_SHADER_FRAGMENT);

		String lowtideEffects = read(LOWTIDE_EFFECTS);
		String registry = read(REGISTRY);
		String clientEvents = read(CLIENT_EVENTS);
		String shaderInit = read(SHADER_INIT);
		String renderTypes = read(RENDER_TYPES);
		String shaderJson = read(SHADER_JSON);
		String vertexShader = read(SHADER_VERTEX);
		String fragmentShader = read(SHADER_FRAGMENT);
		String skyboxShaderJson = read(SKYBOX_SHADER_JSON);
		String skyboxVertexShader = read(SKYBOX_SHADER_VERTEX);
		String skyboxFragmentShader = read(SKYBOX_SHADER_FRAGMENT);
		String skyboxBaseShaderJson = read(SKYBOX_BASE_SHADER_JSON);
		String skyboxBaseVertexShader = read(SKYBOX_BASE_SHADER_VERTEX);
		String skyboxBaseFragmentShader = read(SKYBOX_BASE_SHADER_FRAGMENT);
		String reference = read(REFERENCE);

		assertContains("registry registers dedicated lowtide effects", registry,
				"new MnemonicLowtideChamberEffects(mnemonicLowtide)");
		assertContains("registry keeps lowtide motion alive for shader animation", registry,
				".motion(1.0F)\n\t\t\t\t.layers(0, 0, 0, 0)");
		assertNotContains("client event should not import lowtide skybox effects", clientEvents,
				"import com.vincenthuto.hemomancy.client.render.world.chamberofwill.MnemonicLowtideChamberEffects;");
		assertNotContains("client event should not render lowtide as a world-stage mesh", clientEvents,
				"MnemonicLowtideChamberEffects.renderLake(event);");

		assertContains("shader init declares lowtide lake shader", shaderInit,
				"MNEMONIC_LOWTIDE_LAKE");
		assertContains("shader init declares lowtide tunnel skybox shader", shaderInit,
				"MNEMONIC_LOWTIDE_SKYBOX");
		assertContains("shader init declares lowtide nodule skybox base shader", shaderInit,
				"MNEMONIC_LOWTIDE_SKYBOX_BASE");
		assertNotContains("shader init should not keep obsolete lowtide surface fog shader", shaderInit,
				"MNEMONIC_LOWTIDE_SURFACE_FOG");
		assertContains("shader init uses world lowtide shader path", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_lake\")");
		assertContains("shader init uses world lowtide skybox shader path", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_skybox\")");
		assertContains("shader init uses world lowtide skybox base shader path", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_skybox_base\")");
		assertContains("shader init registers lowtide lake shader", shaderInit,
				"registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));");
		assertContains("shader init registers lowtide tunnel skybox shader", shaderInit,
				"registerShader(event, MNEMONIC_LOWTIDE_SKYBOX.createInstance(provider));");
		assertContains("shader init registers lowtide nodule skybox base shader", shaderInit,
				"registerShader(event, MNEMONIC_LOWTIDE_SKYBOX_BASE.createInstance(provider));");

		assertContains("render type method exists", renderTypes,
				"public static RenderType mnemonicLowtideLake(");
		assertContains("skybox render type method exists", renderTypes,
				"public static RenderType mnemonicLowtideSkybox(");
		assertContains("skybox base render type method exists", renderTypes,
				"public static RenderType mnemonicLowtideSkyboxBase(");
		assertContains("watery fog uses a simple position-color render type", renderTypes,
				"MNEMONIC_LOWTIDE_WATERY_FOG");
		assertContains("watery fog render type avoids custom shader fragility", renderTypes,
				"DefaultVertexFormat.POSITION_COLOR");
		assertNotContains("render types should not keep obsolete lowtide surface fog shader binding", renderTypes,
				"mnemonicLowtideSurfaceFog(");
		assertContains("render type uses lowtide shader shard", renderTypes,
				"ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard()");
		assertContains("render type uses lowtide skybox shader shard", renderTypes,
				"ShaderInit.MNEMONIC_LOWTIDE_SKYBOX.getShard()");
		assertContains("render type uses lowtide skybox base shader shard", renderTypes,
				"ShaderInit.MNEMONIC_LOWTIDE_SKYBOX_BASE.getShard()");
		assertContains("render type uploads per-face skybox seed", renderTypes,
				"setUniform(shader, \"FaceSeed\", faceSeed);");
		assertContains("render type uploads tunnel skybox coverage", renderTypes,
				"setUniform(shader, \"CoverageBias\", coverageBias);");
		assertContains("render type uploads nodule base scale", renderTypes,
				"setUniform(shader, \"NoduleScale\", noduleScale);");
		assertContains("render type uploads nodule base vein intensity", renderTypes,
				"setUniform(shader, \"VeinIntensity\", veinIntensity);");
		assertContains("render type uploads nodule base intensity", renderTypes,
				"setUniform(shader, \"BaseIntensity\", baseIntensity);");
		assertContains("render type disables depth test for skybox lake", renderTypes,
				"RenderType.NO_DEPTH_TEST");
		assertContains("render type uses color-only write mask", renderTypes,
				"RenderType.COLOR_WRITE");

		assertContains("renderer participates in the chamber skybox pass", lowtideEffects,
				"protected void renderBeforeSharedLayers(ChamberThemeRenderContext context)");
		assertContains("renderer uses skybox distance for unreachable depth", lowtideEffects,
				"context.skyDistance()");
		assertContains("renderer uses a skybox lake helper", lowtideEffects,
				"renderLowtideSkyLake(");
		assertContains("renderer calls watery fog independently of lake rendering", lowtideEffects,
				"renderLowtideWateryFog(context.poseStack(), context.time(), context.skyDistance())");
		assertContains("renderer calls tunnel skybox before lowtide lake", lowtideEffects,
				"renderLowtideTunnelSkybox(context.poseStack(), context.time(), context.skyDistance())");
		assertContains("renderer calls nodule base skybox before tunnel membranes", lowtideEffects,
				"renderLowtideSkyboxBase(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderLowtideTunnelSkybox");
		assertContains("renderer sends per-face nodule base skybox seed", lowtideEffects,
				"LOWTIDE_SKYBOX_BASE_SEED");
		assertContains("renderer uses a moderated nodule base backdrop", lowtideEffects,
				"LOWTIDE_SKYBOX_BASE_INTENSITY = 1.02F");
		assertContains("renderer keeps nodule base behind the membrane skybox", lowtideEffects,
				"HemoRenderTypes.mnemonicLowtideSkyboxBase");
		assertContains("renderer uses stronger tunnel membranes over the nodule base", lowtideEffects,
				"LOWTIDE_SKYBOX_TUNNEL_SCALE = 1.12F");
		assertContains("renderer uses stronger bubble membranes over the nodule base", lowtideEffects,
				"LOWTIDE_SKYBOX_BUBBLE_SCALE = 1.14F");
		assertContains("renderer uses stronger tendril membranes over the nodule base", lowtideEffects,
				"LOWTIDE_SKYBOX_TENDRIL_INTENSITY = 1.22F");
		assertContains("renderer renders all six tunnel skybox faces", lowtideEffects,
				"LOWTIDE_SKYBOX_FACE_COUNT = 6");
		assertContains("renderer rotates tunnel skybox faces with shared chamber helper", lowtideEffects,
				"ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face)");
		assertContains("renderer sends per-face tunnel skybox seed", lowtideEffects,
				"LOWTIDE_SKYBOX_FACE_SEED_STEP");
		assertContains("renderer keeps tunnel skybox behind lake/fog depth writes", lowtideEffects,
				"HemoRenderTypes.mnemonicLowtideSkybox");
		assertContains("renderer scales lake from the chamber skybox", lowtideEffects,
				"skyDistance *");
		assertContains("renderer raises lowtide close to the refuge without becoming physical", lowtideEffects,
				"SKY_LAKE_Y_SCALE =");
		assertContains("renderer uses visible skybox wave amplitude", lowtideEffects,
				"WAVE_STRENGTH =");
		assertContains("renderer exposes wave detail scale separately from texture noise", lowtideEffects,
				"WAVE_DETAIL_SCALE =");
		assertContains("renderer exposes a lake edge fade width", lowtideEffects,
				"private static final float EDGE_FADE =");
		assertContains("renderer uses a visible lake-local rim width below the shader cap", lowtideEffects,
				"EDGE_FADE = 0.42F");
		assertNotContains("renderer should not disable lake edge fading", lowtideEffects,
				"EDGE_FADE = 0F");
		assertContains("renderer sends faster shader time so the lake visibly moves", lowtideEffects,
				"LOWTIDE_SHADER_TIME_SCALE =");
		assertContains("renderer applies the lowtide time scale", lowtideEffects,
				"time * LOWTIDE_SHADER_TIME_SCALE");
		assertContains("renderer draws new robust lowtide watery fog", lowtideEffects,
				"renderLowtideWateryFog(");
		assertContains("watery fog ribbons fade in and out over time", lowtideEffects,
				"fogFadePulse");
		assertContains("watery fog uses intentionally visible ribbon bands", lowtideEffects,
				"LOWTIDE_WATERY_FOG_BAND_COUNT");
		assertContains("watery fog uses enough horizontal samples to soften triangle interpolation",
				lowtideEffects, "LOWTIDE_WATERY_FOG_COLUMNS = 14");
		assertContains("watery fog uses enough vertical samples to soften triangle interpolation",
				lowtideEffects, "LOWTIDE_WATERY_FOG_ROWS = 9");
		assertNotContains("watery fog should not stack crossing ribbons that read as X-shaped artifacts",
				lowtideEffects, "yaw + 82.0F");
		assertContains("watery fog breaks up the lower ribbon silhouette in geometry", lowtideEffects,
				"lowtideWateryFogLowerEdgeOffset");
		assertContains("watery fog confines lower-edge movement near the lake surface", lowtideEffects,
				"lowtideWateryFogEdgeInfluence");
		assertContains("watery fog fades out unevenly at the bottom edge", lowtideEffects,
				"bottomEdgeFade");
		assertContains("watery fog warps side edges as geometry instead of keeping vertical cuts",
				lowtideEffects, "lowtideWateryFogX(");
		assertContains("watery fog drives side-edge motion from a dedicated all-edge influence",
				lowtideEffects, "lowtideWateryFogHorizontalEdgeInfluence");
		assertContains("watery fog drives top and bottom motion from a dedicated all-edge influence",
				lowtideEffects, "lowtideWateryFogVerticalEdgeInfluence");
		assertContains("watery fog fades left and right edges unevenly", lowtideEffects,
				"leftRightEdgeFade");
		assertContains("watery fog fades the top edge unevenly", lowtideEffects,
				"topEdgeFade");
		assertContains("watery fog computes a per-band lifecycle progress", lowtideEffects,
				"lowtideWateryFogCycleProgress(time, band)");
		assertContains("watery fog computes a per-band lifecycle index for respawn placement", lowtideEffects,
				"lowtideWateryFogCycleIndex(time, band)");
		assertContains("watery fog fades each ribbon in and out over its lifecycle", lowtideEffects,
				"lowtideWateryFogCycleFade(cycleProgress)");
		assertContains("watery fog respawns from a deterministic lifecycle seed", lowtideEffects,
				"lowtideWateryFogLifecycleRandom(band, cycleIndex)");
		assertNotContains("watery fog should not keep every cloud at a fixed seeded ring position",
				lowtideEffects, "Random random = new Random(73129L);");
		assertContains("watery fog starts close above the lowtide surface", lowtideEffects,
				"LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE = 0.018F");
		assertContains("watery fog stays near the lowtide surface instead of floating in the sky", lowtideEffects,
				"LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE = 0.045F");
		assertContains("watery fog uses a short mist ribbon height", lowtideEffects,
				"Mth.lerp(random.nextFloat(), 0.030F, 0.070F)");
		assertContains("watery fog emits position-color vertices", lowtideEffects,
				"HemoRenderTypes.MNEMONIC_LOWTIDE_WATERY_FOG");
		assertContains("watery fog keeps a soft color gradient in geometry", lowtideEffects,
				"lowtideWateryFogColor");
		assertNotContains("renderer should not keep obsolete invisible surface fog method", lowtideEffects,
				"renderLowtideSurfaceFogPatches");
		assertNotContains("renderer should not print every lowtide frame", lowtideEffects,
				"System.out.println(");
		assertNotContains("renderer should not be a render-level event hook", lowtideEffects,
				"RenderLevelStageEvent");
		assertNotContains("renderer should not gate against physical chamber dimension", lowtideEffects,
				"level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)");
		assertNotContains("renderer should not use synced chamber radius", lowtideEffects,
				"ChamberOfWillClientData.radius()");
		assertNotContains("renderer should not snap to physical chamber spacing", lowtideEffects,
				"ChamberOfWillManager.CHAMBER_SPACING");
		assertNotContains("renderer should not place the lake below the physical floor", lowtideEffects,
				"ChamberOfWillManager.FLOOR_Y");

		assertContains("shader json points to vertex program", shaderJson,
				"\"vertex\": \"hemomancy:world/mnemonic_lowtide_lake\"");
		assertContains("shader json exposes wave strength", shaderJson,
				"\"name\": \"WaveStrength\"");
		assertContains("shader json exposes wave detail scale", shaderJson,
				"\"name\": \"WaveDetailScale\"");
		assertContains("shader json exposes gloss strength", shaderJson,
				"\"name\": \"GlossStrength\"");
		assertContains("shader json defaults edge fade to the visible lake-local rim width", shaderJson,
				"\"name\": \"EdgeFade\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.42 ]");
		assertNotContains("shader json should not expose fog start for skybox lake", shaderJson,
				"\"name\": \"FogStart\"");
		assertNotContains("shader json should not expose fog end for skybox lake", shaderJson,
				"\"name\": \"FogEnd\"");
		assertNotContains("shader json should not expose fog color for skybox lake", shaderJson,
				"\"name\": \"FogColor\"");
		assertNotContains("shader json should not expose fog shape for skybox lake", shaderJson,
				"\"name\": \"FogShape\"");

		assertContains("vertex shader displaces y", vertexShader,
				"surfacePosition.y += waveLift;");
		assertContains("vertex shader adds fine surface ripple displacement", vertexShader,
				"float fineRipples =");
		assertContains("vertex shader uses tweakable wave detail scale", vertexShader,
				"WaveDetailScale");
		assertContains("vertex shader passes ripple highlight to fragment shader", vertexShader,
				"rippleHighlight =");
		assertContains("vertex shader damps mesh-edge waves independently from horizon fade", vertexShader,
				"meshEdgeWaveDamping");
		assertNotContains("vertex shader should not use horizon edge fade for uv wave damping", vertexShader,
				"smoothstep(0.0, EdgeFade, edge)");
		assertNotContains("vertex shader should not derive lake edge fading from camera projection", vertexShader,
				"projectedLakeY");
		assertNotContains("vertex shader should not couple texture noise scale to animated ripple highlights", vertexShader,
				"NoiseScale");
		assertContains("render type uploads wave detail scale", renderTypes,
				"setUniform(shader, \"WaveDetailScale\", waveDetailScale);");
		assertContains("shader init requests wave detail scale uniform", shaderInit,
				"\"WaveDetailScale\"");
		assertNotContains("vertex shader should not compute world fog for skybox lake", vertexShader,
				"fog_distance");
		assertNotContains("vertex shader should not import fog for skybox lake", vertexShader,
				"#moj_import <fog.glsl>");
		assertContains("fragment shader uses fbm", fragmentShader,
				"float fbm(vec2 value)");
		assertContains("fragment shader applies noise scale to texture coordinates", fragmentShader,
				"centered * NoiseScale");
		assertContains("fragment shader names parchment highlights", fragmentShader,
				"parchmentHighlight");
		assertContains("fragment shader uses glossy highlight", fragmentShader,
				"GlossStrength");
		assertContains("fragment shader uses muted oxblood accent", fragmentShader,
				"vec3 oxblood = vec3(");
		assertContains("fragment shader uses dark grey pink marbling", fragmentShader,
				"vec3 greyPink = vec3(");
		assertContains("fragment shader uses broader tan parchment", fragmentShader,
				"vec3 parchment = vec3(");
		assertContains("fragment shader sharpens beige into veiny highlights", fragmentShader,
				"parchmentVein");
		assertContains("fragment shader uses saturated warm beige highlight color", fragmentShader,
				"vec3 warmBeigeVein = vec3(");
		assertContains("fragment shader gives ripples water-like highlights", fragmentShader,
				"rippleHighlight * 0.18");
		assertContains("fragment shader keeps diffuse parchment from dominating the lake", fragmentShader,
				"parchmentHighlight * 0.08");
		assertContains("fragment shader uses beige as narrow liquid streaks", fragmentShader,
				"beigeStreak");
		assertContains("fragment shader mixes less grey-pink into the liquid body", fragmentShader,
				"greyPinkMarble * 0.24");
		assertContains("fragment shader restores controlled red lowtide undertones", fragmentShader,
				"redStream * 0.48");
		assertContains("fragment shader adds brighter red veins", fragmentShader,
				"brightRedVein");
		assertContains("fragment shader boosts saturation without a moving haze mask", fragmentShader,
				"saturationBoost");
		assertContains("fragment shader restores lake contrast after color mixing", fragmentShader,
				"contrastLift");
		assertContains("fragment shader computes edge distance for rim darkening", fragmentShader,
				"edgeDistance");
		assertNotContains("fragment shader should not derive lake edge fading from camera projection", fragmentShader,
				"projectedLakeY");
		assertContains("fragment shader treats edge fade as a lake-local rim width", fragmentShader,
				"lakeEdgeWidth");
		assertContains("fragment shader clamps lake-local rim width at the largest useful edge-only value",
				fragmentShader, "clamp(EdgeFade, 0.001, 0.49)");
		assertNotContains("fragment shader should not hide large edge fade tuning behind a narrow cap",
				fragmentShader, "clamp(EdgeFade, 0.001, 0.24)");
		assertContains("fragment shader builds the rim from stable uv edge distance", fragmentShader,
				"stableLakeEdgeDistance");
		assertContains("fragment shader breaks up the lake-local rim organically", fragmentShader,
				"lakeEdgeBreakup");
		assertContains("fragment shader suppresses bright lake details near the stable rim", fragmentShader,
				"lakeEdgeDetailFade");
		assertContains("fragment shader darkens the stable lake rim instead of the whole lake",
				fragmentShader, "lakeEdgeRimColor");
		assertContains("fragment shader reduces opacity across the stable lake rim",
				fragmentShader, "lakeEdgeOpacityFade");
		assertContains("fragment shader leaves the central lake opaque while making the widened rim readable",
				fragmentShader, "mix(0.16, 1.0");
		assertContains("fragment shader keeps a narrow terminal lake edge alpha feather", fragmentShader,
				"terminalAlphaFeather");
		assertContains("fragment shader scales the terminal alpha feather with the stable rim width",
				fragmentShader, "min(lakeEdgeWidth * 0.16, 0.085)");
		assertContains("fragment shader applies stable edge opacity falloff to lake alpha",
				fragmentShader, "* lakeEdgeOpacityFade * terminalAlpha");
		assertNotContains("fragment shader should not keep projected horizon distance fading", fragmentShader,
				"horizonDistance");
		assertNotContains("fragment shader should not keep broad horizon opacity fade", fragmentShader,
				"smoothstep(0.0, EdgeFade * 1.85");
		assertNotContains("fragment shader should not use texture uv edge as the primary lake horizon fade",
				fragmentShader, "float edgeDarkenWidth = max(EdgeFade");
		assertContains("fragment shader adds small water-surface breakup", fragmentShader,
				"microBreakup");
		assertContains("fragment shader adds thin glossy glint streaks", fragmentShader,
				"glintStreak");
		assertContains("fragment shader advances visible liquid drift", fragmentShader,
				"float time = HemoTime * 0.10;");
		assertNotContains("fragment shader should not fog skybox lake", fragmentShader,
				"linear_fog(");
		assertNotContains("fragment shader should not import fog for skybox lake", fragmentShader,
				"#moj_import <fog.glsl>");
		assertNotContains("fragment shader should not use Java float suffixes", fragmentShader,
				"2f");
		assertNotContains("fragment shader should not add a moving desaturation haze mask", fragmentShader,
				"hazeSuppress");
		assertNotContains("lowtide lake fragment shader should not contain surface fog feature", fragmentShader,
				"wateryFog");

		assertContains("skybox shader json points to vertex program", skyboxShaderJson,
				"\"vertex\": \"hemomancy:world/mnemonic_lowtide_skybox\"");
		assertContains("skybox shader json exposes face seed", skyboxShaderJson,
				"\"name\": \"FaceSeed\"");
		assertContains("skybox shader json exposes coverage bias", skyboxShaderJson,
				"\"name\": \"CoverageBias\"");
		assertContains("skybox vertex shader passes skybox UVs", skyboxVertexShader,
				"texCoord0 = UV0;");
		assertContains("skybox fragment shader uses curling tunnel masks", skyboxFragmentShader,
				"tunnelMask");
		assertContains("skybox fragment shader uses glass bubble membranes", skyboxFragmentShader,
				"bubbleMembrane");
		assertContains("skybox fragment shader boosts membranes over red nodule base", skyboxFragmentShader,
				"foregroundLift");
		assertContains("skybox fragment shader keeps warm membrane fill near cube-face edges",
				skyboxFragmentShader, "foregroundEdgeFill");
		assertContains("skybox fragment shader uses red tendril borders", skyboxFragmentShader,
				"redTendril");
		assertContains("skybox fragment shader uses parchment tendril highlights", skyboxFragmentShader,
				"parchmentTendril");
		assertContains("skybox fragment shader adds small vein flecks to tunnel and bubble lines",
				skyboxFragmentShader, "lineVeinFlecks");
		assertContains("skybox fragment shader adds breakup points to membrane lines", skyboxFragmentShader,
				"membraneBreakupPoints");
		assertContains("skybox fragment shader preserves dark negative sky space", skyboxFragmentShader,
				"negativeSpace");
		assertContains("skybox fragment shader fades lower faces more than overhead", skyboxFragmentShader,
				"CoverageBias");
		assertContains("skybox fragment shader uses noisy cube-face edge cleanup", skyboxFragmentShader,
				"edgeCleanup");
		assertContains("skybox fragment shader breaks up the edge fade organically", skyboxFragmentShader,
				"edgeBreakup");
		assertNotContains("skybox fragment shader should not drop all foreground coverage at cube-face edges",
				skyboxFragmentShader, "CoverageBias * edgeCleanup");
		assertContains("skybox fragment shader fades tendril masks before cube-face boundaries",
				skyboxFragmentShader, "tendrilEdgeFade");
		assertContains("skybox fragment shader fades tunnel and bubble masks before cube-face boundaries",
				skyboxFragmentShader, "structureEdgeFade");
		assertNotContains("skybox fragment shader should not use a hard narrow cube-face seam fade",
				skyboxFragmentShader, "smoothstep(0.0, 0.018, faceEdge)");
		assertNotContains("skybox fragment shader should not use Java float suffixes", skyboxFragmentShader,
				"2f");
		assertNotContains("skybox fragment shader should not require a static skybox sampler", skyboxFragmentShader,
				"sampler2D");

		assertContains("skybox base shader json points to vertex program", skyboxBaseShaderJson,
				"\"vertex\": \"hemomancy:world/mnemonic_lowtide_skybox_base\"");
		assertContains("skybox base shader json exposes nodule scale", skyboxBaseShaderJson,
				"\"name\": \"NoduleScale\"");
		assertContains("skybox base shader json exposes vein intensity", skyboxBaseShaderJson,
				"\"name\": \"VeinIntensity\"");
		assertContains("skybox base shader json exposes base intensity", skyboxBaseShaderJson,
				"\"name\": \"BaseIntensity\"");
		assertContains("skybox base vertex shader passes skybox UVs", skyboxBaseVertexShader,
				"texCoord0 = UV0;");
		assertContains("skybox base fragment shader uses red bulbous nodule forms", skyboxBaseFragmentShader,
				"noduleBulge");
		assertContains("skybox base fragment shader uses small black veins", skyboxBaseFragmentShader,
				"blackVeinWeb");
		assertContains("skybox base fragment shader adds smaller capillary veins", skyboxBaseFragmentShader,
				"capillaryVeins");
		assertContains("skybox base fragment shader uses cellular boundaries between nodules",
				skyboxBaseFragmentShader, "cellularBoundary");
		assertContains("skybox base fragment shader reduces blank black with red ambient wash",
				skyboxBaseFragmentShader, "ambientRedWash");
		assertContains("skybox base fragment shader keeps red fill through cube corners",
				skyboxBaseFragmentShader, "cornerRedFill");
		assertContains("skybox base fragment shader uses moderate base layer presence",
				skyboxBaseFragmentShader, "basePresence = clamp(0.26");
		assertContains("skybox base fragment shader cleans up cube-face edges", skyboxBaseFragmentShader,
				"edgeCleanup");
		assertNotContains("skybox base fragment shader should not use Java float suffixes",
				skyboxBaseFragmentShader, "2f");
		assertNotContains("skybox base fragment shader should not require a static skybox sampler",
				skyboxBaseFragmentShader, "sampler2D");

		assertContains("reference documents lowtide skybox tide", reference,
				"Mnemonic Lowtide");
		assertContains("reference documents unreachable horizon lake", reference,
				"skybox-space");
		assertContains("reference documents lowtide tunnel bubble skybox", reference,
				"curling membrane tunnels");
		assertContains("reference documents lowtide nodule base skybox", reference,
				"red bulbous nodule");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertFileDoesNotExist(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": should not exist " + path);
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
