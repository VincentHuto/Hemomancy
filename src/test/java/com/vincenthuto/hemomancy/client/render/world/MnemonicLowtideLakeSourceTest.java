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
		assertNotContains("shader init should not keep obsolete lowtide surface fog shader", shaderInit,
				"MNEMONIC_LOWTIDE_SURFACE_FOG");
		assertContains("shader init uses world lowtide shader path", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_lake\")");
		assertContains("shader init registers lowtide lake shader", shaderInit,
				"registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));");

		assertContains("render type method exists", renderTypes,
				"public static RenderType mnemonicLowtideLake(");
		assertContains("watery fog uses a simple position-color render type", renderTypes,
				"MNEMONIC_LOWTIDE_WATERY_FOG");
		assertContains("watery fog render type avoids custom shader fragility", renderTypes,
				"DefaultVertexFormat.POSITION_COLOR");
		assertNotContains("render types should not keep obsolete lowtide surface fog shader binding", renderTypes,
				"mnemonicLowtideSurfaceFog(");
		assertContains("render type uses lowtide shader shard", renderTypes,
				"ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard()");
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
		assertContains("renderer scales lake from the chamber skybox", lowtideEffects,
				"skyDistance *");
		assertContains("renderer raises lowtide close to the refuge without becoming physical", lowtideEffects,
				"SKY_LAKE_Y_SCALE =");
		assertContains("renderer uses visible skybox wave amplitude", lowtideEffects,
				"WAVE_STRENGTH =");
		assertContains("renderer exposes wave detail scale separately from texture noise", lowtideEffects,
				"WAVE_DETAIL_SCALE =");
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
		assertContains("vertex shader damps edges", vertexShader,
				"smoothstep(0.0, EdgeFade, edge)");
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
		assertContains("fragment shader softens the far skybox lake edge", fragmentShader,
				"horizonFade");
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

		assertContains("reference documents lowtide skybox tide", reference,
				"Mnemonic Lowtide");
		assertContains("reference documents unreachable horizon lake", reference,
				"skybox-space");
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
