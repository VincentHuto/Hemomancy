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
	private static final Path WALL_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_membrane.json");
	private static final Path WALL_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_membrane.vsh");
	private static final Path WALL_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_membrane.fsh");
	private static final Path CEILING_CORE_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_core.json");
	private static final Path CEILING_CORE_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_core.vsh");
	private static final Path CEILING_CORE_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_core.fsh");
	private static final Path CEILING_ATMOSPHERE_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_atmosphere.json");
	private static final Path CEILING_ATMOSPHERE_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_atmosphere.vsh");
	private static final Path CEILING_ATMOSPHERE_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_atmosphere.fsh");
	private static final Path RIM_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_top_rim.json");
	private static final Path RIM_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_top_rim.vsh");
	private static final Path RIM_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_wall_top_rim.fsh");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");
	private static final Path LORE_REFERENCE = Path.of("docs/LORE_REFERENCE.md");

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
		assertFileExists("apotheos wall membrane shader json", WALL_SHADER_JSON);
		assertFileExists("apotheos wall membrane vertex shader", WALL_SHADER_VERTEX);
		assertFileExists("apotheos wall membrane fragment shader", WALL_SHADER_FRAGMENT);
		assertFileExists("apotheos ceiling core shader json", CEILING_CORE_SHADER_JSON);
		assertFileExists("apotheos ceiling core vertex shader", CEILING_CORE_SHADER_VERTEX);
		assertFileExists("apotheos ceiling core fragment shader", CEILING_CORE_SHADER_FRAGMENT);
		assertFileExists("apotheos ceiling atmosphere shader json", CEILING_ATMOSPHERE_SHADER_JSON);
		assertFileExists("apotheos ceiling atmosphere vertex shader", CEILING_ATMOSPHERE_SHADER_VERTEX);
		assertFileExists("apotheos ceiling atmosphere fragment shader", CEILING_ATMOSPHERE_SHADER_FRAGMENT);
		assertFileExists("apotheos wall-top rim shader json", RIM_SHADER_JSON);
		assertFileExists("apotheos wall-top rim vertex shader", RIM_SHADER_VERTEX);
		assertFileExists("apotheos wall-top rim fragment shader", RIM_SHADER_FRAGMENT);
		assertFileMissing("removed shared apotheos ceiling mass shader json",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_mass.json"));
		assertFileMissing("removed shared apotheos ceiling mass vertex shader",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_mass.vsh"));
		assertFileMissing("removed shared apotheos ceiling mass fragment shader",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_mass.fsh"));
		assertFileMissing("removed apotheos ceiling canopy shader json",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_canopy.json"));
		assertFileMissing("removed apotheos ceiling canopy vertex shader",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_canopy.vsh"));
		assertFileMissing("removed apotheos ceiling canopy fragment shader",
				Path.of("src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_canopy.fsh"));

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
		String wallShaderJson = read(WALL_SHADER_JSON);
		String wallVertexShader = read(WALL_SHADER_VERTEX);
		String wallFragmentShader = read(WALL_SHADER_FRAGMENT);
		String ceilingCoreShaderJson = read(CEILING_CORE_SHADER_JSON);
		String ceilingCoreVertexShader = read(CEILING_CORE_SHADER_VERTEX);
		String ceilingCoreFragmentShader = read(CEILING_CORE_SHADER_FRAGMENT);
		String ceilingAtmosphereShaderJson = read(CEILING_ATMOSPHERE_SHADER_JSON);
		String ceilingAtmosphereVertexShader = read(CEILING_ATMOSPHERE_SHADER_VERTEX);
		String ceilingAtmosphereFragmentShader = read(CEILING_ATMOSPHERE_SHADER_FRAGMENT);
		String rimShaderJson = read(RIM_SHADER_JSON);
		String rimVertexShader = read(RIM_SHADER_VERTEX);
		String rimFragmentShader = read(RIM_SHADER_FRAGMENT);
		String reference = read(REFERENCE);
		String loreReference = read(LORE_REFERENCE);

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
		assertContains("shader init declares apotheos wall membrane shader", shaderInit,
				"APOTHEOS_WALL_MEMBRANE");
		assertContains("shader init declares apotheos ceiling core shader", shaderInit,
				"APOTHEOS_CEILING_CORE");
		assertContains("shader init declares apotheos ceiling atmosphere shader", shaderInit,
				"APOTHEOS_CEILING_ATMOSPHERE");
		assertContains("shader init declares apotheos wall-top rim shader", shaderInit,
				"APOTHEOS_WALL_TOP_RIM");
		assertContains("shader init uses world apotheos shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_floor_funnel\")");
		assertContains("shader init uses world apotheos portal haze shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_portal_haze\")");
		assertContains("shader init uses world apotheos portal glow shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_portal_glow\")");
		assertContains("shader init uses world apotheos wall membrane shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_wall_membrane\")");
		assertContains("shader init uses world apotheos ceiling core shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_ceiling_core\")");
		assertContains("shader init uses world apotheos ceiling atmosphere shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_ceiling_atmosphere\")");
		assertContains("shader init uses world apotheos wall-top rim shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_wall_top_rim\")");
		assertContains("shader init registers apotheos floor shader", shaderInit,
				"registerShader(event, APOTHEOS_FLOOR_FUNNEL.createInstance(provider));");
		assertContains("shader init registers apotheos portal haze shader", shaderInit,
				"registerShader(event, APOTHEOS_PORTAL_HAZE.createInstance(provider));");
		assertContains("shader init registers apotheos portal glow shader", shaderInit,
				"registerShader(event, APOTHEOS_PORTAL_GLOW.createInstance(provider));");
		assertContains("shader init registers apotheos wall membrane shader", shaderInit,
				"registerShader(event, APOTHEOS_WALL_MEMBRANE.createInstance(provider));");
		assertContains("shader init registers apotheos ceiling core shader", shaderInit,
				"registerShader(event, APOTHEOS_CEILING_CORE.createInstance(provider));");
		assertContains("shader init registers apotheos ceiling atmosphere shader", shaderInit,
				"registerShader(event, APOTHEOS_CEILING_ATMOSPHERE.createInstance(provider));");
		assertContains("shader init registers apotheos wall-top rim shader", shaderInit,
				"registerShader(event, APOTHEOS_WALL_TOP_RIM.createInstance(provider));");
		assertContains("shader init requests ring rise uniform", shaderInit,
				"\"RingRise\"");
		assertContains("shader init requests center void uniform", shaderInit,
				"\"CenterVoidRadius\"");
		assertContains("shader init requests ceiling seed uniform", shaderInit,
				"\"CeilingSeed\"");
		assertContains("shader init requests ceiling core noise uniform", shaderInit,
				"\"CoreNoiseScale\"");
		assertContains("shader init requests ceiling rotation speed uniform", shaderInit,
				"\"RotationSpeed\"");
		assertContains("shader init requests ceiling yellow glow uniform", shaderInit,
				"\"YellowGlowIntensity\"");
		assertContains("shader init requests ceiling green orb uniform", shaderInit,
				"\"GreenOrbIntensity\"");
		assertContains("shader init requests ceiling core undulation uniform", shaderInit,
				"\"CoreUndulationIntensity\"");
		assertContains("shader init requests ceiling atmosphere noise uniform", shaderInit,
				"\"AtmosphereNoiseScale\"");
		assertContains("shader init requests ceiling atmosphere storm uniform", shaderInit,
				"\"StormIntensity\"");
		assertContains("shader init requests ceiling atmosphere opacity uniform", shaderInit,
				"\"AtmosphereOpacity\"");
		assertNotContains("shader init should not keep shared mass layer uniform", shaderInit,
				"\"MassLayer\"");
		assertNotContains("shader init should not keep shared layer opacity uniform", shaderInit,
				"\"LayerOpacity\"");
		assertContains("shader init requests wall-top rim glow uniform", shaderInit,
				"\"RimGlowIntensity\"");
		assertContains("shader init requests wall-top rim pulse speed uniform", shaderInit,
				"\"RimPulseSpeed\"");
		assertNotContains("ceiling mass shader holder should not own the rim glow uniform", shaderInit,
				"\"GreenOrbIntensity\", \"RimGlowIntensity\"");
		assertNotContains("shader init should not declare apotheos ceiling canopy shader", shaderInit,
				"APOTHEOS_CEILING_CANOPY");
		assertNotContains("shader init should not use world apotheos ceiling canopy shader path", shaderInit,
				"world/apotheos_ceiling_canopy");

		assertContains("render type method exists", renderTypes,
				"public static RenderType apotheosFloorFunnel(");
		assertContains("portal haze render type method exists", renderTypes,
				"public static RenderType apotheosPortalHaze(");
		assertContains("portal glow render type method exists", renderTypes,
				"public static RenderType apotheosPortalGlow(");
		assertContains("wall membrane render type method exists", renderTypes,
				"public static RenderType apotheosWallMembrane(");
		assertContains("ceiling core render type method exists", renderTypes,
				"public static RenderType apotheosCeilingCore(");
		assertContains("ceiling atmosphere render type method exists", renderTypes,
				"public static RenderType apotheosCeilingAtmosphere(");
		assertContains("wall-top rim render type method exists", renderTypes,
				"public static RenderType apotheosWallTopRim(");
		assertContains("render type uses apotheos shader shard", renderTypes,
				"ShaderInit.APOTHEOS_FLOOR_FUNNEL.getShard()");
		assertContains("portal haze render type uses apotheos haze shader shard", renderTypes,
				"ShaderInit.APOTHEOS_PORTAL_HAZE.getShard()");
		assertContains("portal glow render type uses apotheos glow shader shard", renderTypes,
				"ShaderInit.APOTHEOS_PORTAL_GLOW.getShard()");
		assertContains("wall membrane render type uses apotheos wall shader shard", renderTypes,
				"ShaderInit.APOTHEOS_WALL_MEMBRANE.getShard()");
		assertContains("ceiling core render type uses apotheos ceiling core shader shard", renderTypes,
				"ShaderInit.APOTHEOS_CEILING_CORE.getShard()");
		assertContains("ceiling atmosphere render type uses apotheos ceiling atmosphere shader shard", renderTypes,
				"ShaderInit.APOTHEOS_CEILING_ATMOSPHERE.getShard()");
		assertContains("wall-top rim render type uses apotheos rim shader shard", renderTypes,
				"ShaderInit.APOTHEOS_WALL_TOP_RIM.getShard()");
		assertContains("apotheos ceiling primitive render type exists", renderTypes,
				"APOTHEOS_CEILING_PRIMITIVES");
		assertContains("apotheos ceiling primitive render type ignores skybox depth", renderTypes,
				"RenderType.create(\"apotheos_ceiling_primitives\"");
		assertContains("apotheos wall frame render type exists", renderTypes,
				"APOTHEOS_WALL_FRAME");
		assertContains("apotheos wall frame render type ignores skybox depth", renderTypes,
				"RenderType.create(\"apotheos_wall_frame\"");
		assertContains("apotheos wall frame render type uses no depth test", renderTypes,
				".setDepthTestState(RenderType.NO_DEPTH_TEST)");
		assertContains("render type uploads ring speed", renderTypes,
				"setUniform(shader, \"RingSpeed\", ringSpeed);");
		assertContains("wall membrane render type uploads fiber scale", renderTypes,
				"setUniform(shader, \"FiberScale\", fiberScale);");
		assertContains("wall membrane render type uploads trace intensity", renderTypes,
				"setUniform(shader, \"TraceIntensity\", traceIntensity);");
		assertContains("wall membrane render type uploads red glow intensity", renderTypes,
				"setUniform(shader, \"RedGlowIntensity\", redGlowIntensity);");
		assertContains("wall membrane render type uploads ceiling fade start", renderTypes,
				"setUniform(shader, \"CeilingFadeStart\", ceilingFadeStart);");
		assertContains("wall membrane render type uploads ceiling fade end", renderTypes,
				"setUniform(shader, \"CeilingFadeEnd\", ceilingFadeEnd);");
		assertContains("ceiling core render type uploads core noise", renderTypes,
				"setUniform(shader, \"CoreNoiseScale\", coreNoiseScale);");
		assertContains("ceiling core render type uploads rotation speed", renderTypes,
				"setUniform(shader, \"RotationSpeed\", rotationSpeed);");
		assertContains("ceiling core render type uploads yellow glow", renderTypes,
				"setUniform(shader, \"YellowGlowIntensity\", yellowGlowIntensity);");
		assertContains("ceiling core render type uploads green orb glow", renderTypes,
				"setUniform(shader, \"GreenOrbIntensity\", greenOrbIntensity);");
		assertContains("ceiling core render type uploads core undulation", renderTypes,
				"setUniform(shader, \"CoreUndulationIntensity\", coreUndulationIntensity);");
		assertContains("ceiling atmosphere render type uploads atmosphere noise", renderTypes,
				"setUniform(shader, \"AtmosphereNoiseScale\", atmosphereNoiseScale);");
		assertContains("ceiling atmosphere render type uploads storm intensity", renderTypes,
				"setUniform(shader, \"StormIntensity\", stormIntensity);");
		assertContains("ceiling atmosphere render type uploads atmosphere opacity", renderTypes,
				"setUniform(shader, \"AtmosphereOpacity\", atmosphereOpacity);");
		assertNotContains("render types should not upload a shared mass layer", renderTypes,
				"setUniform(shader, \"MassLayer\"");
		assertNotContains("render types should not upload shared layer opacity", renderTypes,
				"setUniform(shader, \"LayerOpacity\"");
		assertContains("wall-top rim render type uploads rim glow", renderTypes,
				"setUniform(shader, \"RimGlowIntensity\", rimGlowIntensity);");
		assertContains("wall-top rim render type uploads rim pulse speed", renderTypes,
				"setUniform(shader, \"RimPulseSpeed\", rimPulseSpeed);");
		assertNotContains("render types should not keep apotheos ceiling canopy method", renderTypes,
				"apotheosCeilingCanopy");
		assertNotContains("render types should not keep apotheos ceiling canopy shader", renderTypes,
				"APOTHEOS_CEILING_CANOPY");
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
		assertContains("apotheos effects render wall membrane before shared chamber layers", apotheosEffects,
				"renderApotheosWallMembrane(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render wall frames before shared chamber layers", apotheosEffects,
				"renderApotheosWallFrames(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render ceiling mass before shared chamber layers", apotheosEffects,
				"renderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects split ceiling mass into explicit atmosphere pass", apotheosEffects,
				"renderApotheosCeilingAtmosphere(poseStack, time, skyDistance);");
		assertContains("apotheos effects split ceiling mass into explicit core pass", apotheosEffects,
				"renderApotheosCeilingCore(poseStack, time, skyDistance);");
		assertOrder("apotheos effects render the transparent atmosphere before the solid core", apotheosEffects,
				"renderApotheosCeilingAtmosphere(poseStack, time, skyDistance);",
				"renderApotheosCeilingCore(poseStack, time, skyDistance);");
		assertNotContains("apotheos effects should not keep shared mass layer renderer", apotheosEffects,
				"renderApotheosCeilingMassLayer");
		assertNotContains("apotheos effects should not keep layer selector constants", apotheosEffects,
				"APOTHEOS_CEILING_MASS_CORE_LAYER");
		assertNotContains("apotheos effects should not keep atmosphere layer selector constants", apotheosEffects,
				"APOTHEOS_CEILING_MASS_ATMOSPHERE_LAYER");
		assertContains("apotheos effects render ceiling tendrils before shared chamber layers", apotheosEffects,
				"renderApotheosCeilingTendrils(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render ceiling orbs before shared chamber layers", apotheosEffects,
				"renderApotheosCeilingOrbsAndGlow(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render wall-top rim before shared chamber layers", apotheosEffects,
				"renderApotheosWallTopRim(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render portal glow before shared chamber layers", apotheosEffects,
				"renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render floor funnel before shared chamber layers", apotheosEffects,
				"renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render the ceiling mass after the wall frames", apotheosEffects,
				"renderApotheosWallFrames(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());");
        assertOrder("apotheos effects render tendrils after the full ceiling mass", apotheosEffects,
                "renderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());",
                "renderApotheosCeilingTendrils(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects render outward tendrils after the hanging tendrils", apotheosEffects,
				"renderApotheosCeilingTendrils(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosCeilingOutwardTendrils(context.poseStack(), context.tesselator(), context.time()");
		assertContains("apotheos effects render orbs after the outward tendrils", apotheosEffects,
				"renderApotheosCeilingOutwardTendrils(context.poseStack(), context.tesselator(), context.time(),\n\t\t\t\tcontext.skyDistance());\n\t\trenderApotheosCeilingOrbsAndGlow(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("apotheos effects draw the floor funnel after the portal glow", apotheosEffects,
				"renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance())");
		assertContains("apotheos effects draw the portal glow before the floor funnel", apotheosEffects,
				"renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosFloorFunnel");
		assertContains("apotheos effects draw a second portal haze layer", apotheosEffects,
				"renderApotheosPortalHaze");
		assertContains("apotheos effects draw a separate third portal glow layer", apotheosEffects,
				"renderApotheosPortalGlow");
		assertContains("apotheos effects keeps the accepted portal glow radius", apotheosEffects,
				"APOTHEOS_PORTAL_GLOW_RADIUS = 0.42F");
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
		assertContains("apotheos effects use cylindrical wall angular subdivisions", apotheosEffects,
				"APOTHEOS_WALL_RING_SEGMENTS = 96");
		assertContains("apotheos effects use cylindrical wall vertical subdivisions", apotheosEffects,
				"APOTHEOS_WALL_VERTICAL_SEGMENTS = 20");
		assertContains("apotheos effects use radial ceiling mass subdivisions", apotheosEffects,
				"APOTHEOS_CEILING_RADIAL_SEGMENTS");
		assertContains("apotheos effects use angular ceiling mass subdivisions", apotheosEffects,
				"APOTHEOS_CEILING_RING_SEGMENTS");
		assertContains("apotheos effects stabilize the collapsed center pole uv", apotheosEffects,
				"APOTHEOS_CEILING_CORE_CENTER_ANGLE_T = 0.5F");
		assertContains("apotheos effects reuse the stable center angle at the mass pole", apotheosEffects,
				"stableAngleT");
		assertContains("apotheos effects use a planetoid ceiling radius curve instead of lensy sqrt expansion",
				apotheosEffects, "apotheosCeilingPlanetoidRadiusT");
		assertNotContains("apotheos ceiling mass should not use sqrt radial expansion", apotheosEffects,
				"float shapedRadiusT = Mth.sqrt(radialT);\n\t\treturn skyDistance * Mth.lerp(shapedRadiusT, 0.035F, APOTHEOS_CEILING_DOME_SPAN_SCALE);");
		assertContains("apotheos effects keep the solid inner core smaller than the old shared mass", apotheosEffects,
				"APOTHEOS_CEILING_CORE_RADIUS_SCALE = 0.82F");
		assertContains("apotheos effects give the atmosphere a separate larger shell radius", apotheosEffects,
				"APOTHEOS_CEILING_ATMOSPHERE_RADIUS_SCALE = 1.1F");
		assertContains("apotheos effects expose core undulation tuning", apotheosEffects,
				"APOTHEOS_CEILING_CORE_UNDULATION_INTENSITY");
		assertContains("apotheos effects expose atmosphere storm tuning", apotheosEffects,
				"APOTHEOS_CEILING_ATMOSPHERE_STORM_INTENSITY");
		assertContains("apotheos effects keep the atmosphere transparent", apotheosEffects,
				"APOTHEOS_CEILING_ATMOSPHERE_OPACITY = 0.18F");
		assertNotContains("apotheos effects should not keep shared mass radius constants", apotheosEffects,
				"APOTHEOS_CEILING_MASS_CORE_RADIUS_SCALE");
		assertNotContains("apotheos effects should not keep shared atmosphere opacity constants", apotheosEffects,
				"APOTHEOS_CEILING_MASS_ATMOSPHERE_OPACITY");
		assertContains("apotheos effects use deterministic ceiling tendrils", apotheosEffects,
				"APOTHEOS_CEILING_TENDRIL_COUNT");
		assertContains("apotheos effects use qliphoth-style outward mass tendrils", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_COUNT");
		assertContains("apotheos effects expose outward mass tendril depth pull", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE");
		assertContains("apotheos effects expose outward mass tendril width", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE");
		assertContains("apotheos effects expose outward mass tendril white ratio", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RATIO");
		assertContains("apotheos outward tendrils start inside the visible mass edge", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_ROOT_RADIAL_T = 0.56F");
		assertContains("apotheos outward tendrils keep camera-depth pull secondary to outward reach",
				apotheosEffects, "APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE = 0.30F");
		assertContains("apotheos outward tendrils push laterally past the mass edge", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_RADIAL_PUSH_SCALE = 0.78F");
		assertContains("apotheos outward tendrils keep downward sag secondary to outward length", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_VERTICAL_SAG_SCALE = 0.026F");
		assertContains("apotheos outward tendrils expose per-segment wriggle speed", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_TIME_SCALE = 0.24F");
		assertContains("apotheos outward tendrils expose per-segment wriggle distance", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_SCALE = 0.055F");
		assertContains("apotheos outward tendrils expose per-segment wriggle frequency", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_FREQUENCY = 14.0F");
		assertContains("apotheos outward tendrils expose faster geometry animation", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_GEOMETRY_TIME_SCALE = 0.24F");
		assertContains("apotheos outward tendrils expose minimum length variation", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_MIN_LENGTH_MULTIPLIER = 0.88F");
		assertContains("apotheos outward tendrils expose maximum length variation", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_MAX_LENGTH_MULTIPLIER = 1.82F");
		assertContains("apotheos outward tendrils compute deterministic length variation", apotheosEffects,
				"apotheosCeilingOutwardTendrilLengthMultiplier");
		assertContains("apotheos outward tendrils are wide enough to read against the mass", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE = 0.014F");
		assertContains("apotheos outward tendrils include enough white strands to be visible", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RATIO = 0.48F");
		assertContains("apotheos outward tendrils expose bright strand alpha", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_ALPHA");
		assertContains("apotheos outward tendrils expose yellow strand alpha", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_ALPHA");
		assertContains("apotheos outward tendrils use yellow-red color", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_RED = 255");
		assertContains("apotheos outward tendrils use yellow-green color", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_GREEN = 218");
		assertContains("apotheos outward tendrils use yellow-blue color", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_BLUE = 46");
		assertNotContains("apotheos outward tendrils should no longer expose black strand constants",
				apotheosEffects, "APOTHEOS_CEILING_OUTWARD_TENDRIL_BLACK_");
		assertContains("apotheos outward tendrils use visible qliphoth-style tube geometry", apotheosEffects,
				"TendrilGeometry.generate");
		assertContains("apotheos outward tendrils emit tube quads instead of flat ceiling strips", apotheosEffects,
				"TendrilGeometry.createTubeQuads");
		assertContains("apotheos outward tendrils use the qliphoth direct position-color shader path",
				apotheosEffects, "RenderSystem.setShader(GameRenderer::getPositionColorShader);");
		assertContains("apotheos outward tendrils use the qliphoth direct buffer builder path", apotheosEffects,
				"BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);");
		assertContains("apotheos outward tendrils flush like qliphoth communion", apotheosEffects,
				"BufferUploader.drawWithShader(buffer.buildOrThrow());");
		assertContains("apotheos outward tendrils are a separate direct render pass", apotheosEffects,
				"renderApotheosCeilingOutwardTendrils(context.poseStack(), context.tesselator()");
		assertContains("apotheos outward tendrils use freeform hutoslib tendril config", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_CONFIG");
		assertContains("apotheos outward tendrils pull toward the viewer like qliphoth communion", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE");
		assertContains("apotheos outward tendrils start from the rendered mass surface", apotheosEffects,
				"Vec3 sourceAtMass = apotheosCeilingOutwardTendrilPoint");
		assertContains("apotheos outward tendrils explicitly end toward the player like qliphoth communion",
				apotheosEffects, "Vec3 targetTowardPlayer = sourceAtMass.subtract(forward.scale");
		assertContains("apotheos outward tendrils add lateral extension beyond the mass silhouette",
				apotheosEffects, ".add(radialOut.scale(radialPush))");
		assertContains("apotheos outward tendrils deform each generated ring instead of swaying the endpoint",
				apotheosEffects, "apotheosCeilingOutwardWriggledStrand(strand, time, skyDistance");
		assertContains("apotheos outward tendrils calculate ring-local wriggle offsets", apotheosEffects,
				"apotheosCeilingOutwardWriggleOffset");
		assertNotContains("apotheos outward tendrils should not move as one rigid endpoint sway", apotheosEffects,
				"Vec3 animatedSway");
		assertNotContains("apotheos outward tendrils should not derive their target from a second mass-surface point",
				apotheosEffects, "Vec3 target = apotheosCeilingOutwardTendrilPoint");
		assertNotContains("apotheos outward tendrils should not keep a surface-radial length control",
				apotheosEffects, "APOTHEOS_CEILING_OUTWARD_TENDRIL_LENGTH_SCALE");
		assertContains("apotheos outward tendril tube width stays scaled to the chamber sky distance",
				apotheosEffects, "skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE");
		assertContains("apotheos outward tendril tube compensates for hutoslib base width scaling",
				apotheosEffects, "APOTHEOS_CEILING_OUTWARD_TENDRIL_CONFIG.baseWidth()");
		assertContains("apotheos effects use deterministic ceiling orbs", apotheosEffects,
				"APOTHEOS_CEILING_ORB_COUNT");
		assertContains("apotheos effects anchor the red rim to the wall handoff", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_HEIGHT_T");
		assertContains("apotheos effects expose an independent rim radius", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_RADIUS_SCALE");
		assertContains("apotheos effects expose an independent bright rim width", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_CORE_WIDTH_SCALE");
		assertContains("apotheos effects expose an independent rim glow width", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_GLOW_WIDTH_SCALE");
		assertContains("apotheos effects expose wall-top rim glow red color", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_GLOW_RED");
		assertContains("apotheos effects expose wall-top rim glow green color", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_GLOW_GREEN");
		assertContains("apotheos effects expose wall-top rim glow blue color", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_GLOW_BLUE");
		assertContains("apotheos effects uses the configurable wall-top rim glow color", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_GLOW_RED, APOTHEOS_WALL_TOP_RIM_GLOW_GREEN, APOTHEOS_WALL_TOP_RIM_GLOW_BLUE");
		assertContains("apotheos effects uses rim radius independent from the ceiling mass", apotheosEffects,
				"skyDistance * APOTHEOS_WALL_TOP_RIM_RADIUS_SCALE");
		assertNotContains("apotheos rim should not inherit the wall radius directly in its band helper",
				apotheosEffects, "float baseRadius = skyDistance * APOTHEOS_WALL_RADIUS_SCALE;");
		assertNotContains("apotheos rim should not size from ceiling mass span", apotheosEffects,
				"APOTHEOS_WALL_TOP_RIM_RADIUS_SCALE = APOTHEOS_CEILING_DOME_SPAN_SCALE");
		assertContains("apotheos effects place wall membrane just outside the floor rim", apotheosEffects,
				"APOTHEOS_WALL_RADIUS_SCALE = 0.66F");
		assertContains("apotheos effects extend the wall below the portal rim for floor blending", apotheosEffects,
				"APOTHEOS_WALL_BOTTOM_Y_SCALE = -0.58F");
		assertContains("apotheos effects boost wall trace readability", apotheosEffects,
				"APOTHEOS_WALL_TRACE_INTENSITY = 0.68F");
		assertContains("apotheos effects boost the low red wall glow", apotheosEffects,
				"APOTHEOS_WALL_RED_GLOW_INTENSITY = 0.95F");
		assertContains("apotheos effects builds a cylindrical APOTHEOS wall mesh", apotheosEffects,
				"emitApotheosWallCylinderMesh");
		assertContains("apotheos effects builds prominent APOTHEOS wall ribs", apotheosEffects,
				"emitApotheosWallRib");
		assertContains("apotheos effects builds thin APOTHEOS wall web ribbons", apotheosEffects,
				"emitApotheosWallWebRibbon");
		assertContains("apotheos effects segment wall web ribbons into arcs", apotheosEffects,
				"APOTHEOS_WALL_WEB_RIBBON_SEGMENTS = 22");
		assertContains("apotheos effects keep wall web ribbons out of the red lower glow area", apotheosEffects,
				"APOTHEOS_WALL_WEB_MIN_HEIGHT_T = 0.46F");
		assertContains("apotheos effects clamps web arc spans above the red wall zone", apotheosEffects,
				"APOTHEOS_WALL_WEB_MIN_HEIGHT_T, 0.80F");
		assertContains("apotheos effects keeps wall web arc geometry stable between frames", apotheosEffects,
				"endHeightT, ribbon * 5.61F);");
		assertContains("apotheos effects bends APOTHEOS web ribbons into arcs", apotheosEffects,
				"apotheosWallWebArcBend");
		assertContains("apotheos effects emits curved APOTHEOS web ribbon spans", apotheosEffects,
				"emitApotheosWallWebRibbonSpan");
		assertNotContains("apotheos effects should not render apotheos ceiling canopy", apotheosEffects,
				"renderApotheosCeilingCanopy");
		assertNotContains("apotheos effects should not render the removed apotheos ceiling funnel", apotheosEffects,
				"renderApotheosCeilingFunnel");
		assertContains("apotheos wall frame pass uses the no-depth wall frame render type", apotheosEffects,
				"RenderType renderType = HemoRenderTypes.APOTHEOS_WALL_FRAME");
		assertNotContains("apotheos effects should not keep old ceiling-mouth collar helper", apotheosEffects,
				"emitApotheosWallTopFleshyCollar");
		assertNotContains("apotheos effects should not keep old collar y anchor", apotheosEffects,
				"apotheosWallTopCollarY");
		assertNotContains("apotheos effects should not keep old flat ceiling cap helper", apotheosEffects,
				"emitApotheosCeilingCanopyMesh");
		assertNotContains("apotheos effects should not keep old separate ceiling funnel core", apotheosEffects,
				"emitApotheosCeilingFunnelCore");
		assertNotContains("apotheos effects should not own the visible lip from the ceiling pass", apotheosEffects,
				"emitApotheosCeilingFleshyRimLip");
		assertNotContains("apotheos effects should not keep old radial ceiling mass radius", apotheosEffects,
				"APOTHEOS_CEILING_MASS_RADIUS");
		assertContains("apotheos effects build the solid core ceiling mesh", apotheosEffects,
				"emitApotheosCeilingCoreMesh");
		assertContains("apotheos effects build the storm atmosphere ceiling mesh", apotheosEffects,
				"emitApotheosCeilingAtmosphereMesh");
		assertContains("apotheos effects build yellow and white ceiling tendrils", apotheosEffects,
				"emitApotheosCeilingTendril");
		assertContains("apotheos ceiling tendrils use yellow instead of black red channel", apotheosEffects,
				"int red = whiteTendril ? 230 : 255");
		assertContains("apotheos ceiling tendrils use yellow instead of black green channel", apotheosEffects,
				"int green = whiteTendril ? 230 : 218");
		assertContains("apotheos ceiling tendrils use yellow instead of black blue channel", apotheosEffects,
				"int blue = whiteTendril ? 226 : 46");
		assertContains("apotheos effects build qliphoth-style outward yellow and white mass tendrils", apotheosEffects,
				"emitApotheosCeilingOutwardTendril");
		assertContains("apotheos outward tendrils start from the mass perimeter", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_ROOT_RADIAL_T");
		assertContains("apotheos outward tendrils pull away from the mass edge with varied length", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE * lengthMultiplier");
		assertContains("apotheos outward tendrils spend length outward instead of downward", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_RADIAL_PUSH_SCALE * lengthMultiplier");
		assertContains("apotheos outward tendrils use a low sag scale", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_VERTICAL_SAG_SCALE");
		assertContains("apotheos outward tendrils feed animated time into hutoslib geometry", apotheosEffects,
				"time * APOTHEOS_CEILING_OUTWARD_TENDRIL_GEOMETRY_TIME_SCALE");
		assertContains("apotheos outward tendrils use ring progress so different segments animate separately",
				apotheosEffects, "ring.progress() * APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_FREQUENCY");
		assertNotContains("apotheos outward tendrils should not use the old nearly static geometry time",
				apotheosEffects, "time * 0.052F, TendrilGeometry.SurfaceResolver.NONE");
		assertNotContains("apotheos outward tendrils should not slide their target around the mass surface",
				apotheosEffects, "APOTHEOS_CEILING_OUTWARD_TENDRIL_LENGTH_SCALE * lengthMultiplier * t");
		assertContains("apotheos outward tendrils render white strands", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RED");
		assertContains("apotheos outward tendrils render yellow strands", apotheosEffects,
				"APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_RED");
		assertContains("apotheos outward tendrils do not use the flat ceiling span helper", apotheosEffects,
				"emitApotheosCeilingOutwardTendrilTube");
		assertContains("apotheos outward tendril geometry writes directly to BufferBuilder like qliphoth",
				apotheosEffects, "emitApotheosCeilingOutwardTendril(BufferBuilder buffer");
		assertContains("apotheos effects build yellow and green ceiling orbs", apotheosEffects,
				"emitApotheosCeilingOrb");
		assertContains("apotheos effects build the wall-top red rim", apotheosEffects,
				"emitApotheosWallTopRim");
		assertContains("apotheos rim pass uses the dedicated rim render type", apotheosEffects,
				"RenderType renderType = HemoRenderTypes.apotheosWallTopRim(");
		assertNotContains("apotheos rim should not render through the ceiling primitive layer", apotheosEffects,
				"renderApotheosWallTopRim(PoseStack poseStack, float time, float skyDistance) {\n\t\tRenderSystem.enableBlend();\n\t\tRenderSystem.disableCull();\n\t\tRenderSystem.disableDepthTest();\n\t\tRenderSystem.depthMask(false);\n\t\tRenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,\n\t\t\t\tGlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,\n\t\t\t\tGlStateManager.DestFactor.ZERO);\n\t\tRenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);\n\n\t\tMultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();\n\t\tRenderType renderType = HemoRenderTypes.APOTHEOS_CEILING_PRIMITIVES;");
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
		assertContains("portal glow shader json defaults to the accepted glow radius", glowShaderJson,
				"\"name\": \"GlowRadius\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.42 ]");
		assertContains("portal glow shader json exposes center void radius", glowShaderJson,
				"\"name\": \"CenterVoidRadius\"");
		assertContains("wall membrane shader json points to vertex program", wallShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_wall_membrane\"");
		assertContains("wall membrane shader json exposes fiber scale", wallShaderJson,
				"\"name\": \"FiberScale\"");
		assertContains("wall membrane shader json exposes trace intensity", wallShaderJson,
				"\"name\": \"TraceIntensity\"");
		assertContains("wall membrane shader json exposes red glow intensity", wallShaderJson,
				"\"name\": \"RedGlowIntensity\"");
		assertContains("wall membrane shader json exposes ceiling fade start", wallShaderJson,
				"\"name\": \"CeilingFadeStart\"");
		assertContains("wall membrane shader json exposes ceiling fade end", wallShaderJson,
				"\"name\": \"CeilingFadeEnd\"");
		assertContains("ceiling core shader json points to vertex program", ceilingCoreShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_ceiling_core\"");
		assertContains("ceiling core shader json exposes core noise scale", ceilingCoreShaderJson,
				"\"name\": \"CoreNoiseScale\"");
		assertContains("ceiling core shader json exposes core undulation intensity", ceilingCoreShaderJson,
				"\"name\": \"CoreUndulationIntensity\"");
		assertContains("ceiling core shader json exposes yellow glow intensity", ceilingCoreShaderJson,
				"\"name\": \"YellowGlowIntensity\"");
		assertContains("ceiling core shader json exposes green orb intensity", ceilingCoreShaderJson,
				"\"name\": \"GreenOrbIntensity\"");
		assertNotContains("ceiling core shader json should not expose rim glow intensity", ceilingCoreShaderJson,
				"\"name\": \"RimGlowIntensity\"");
		assertContains("ceiling atmosphere shader json points to vertex program", ceilingAtmosphereShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_ceiling_atmosphere\"");
		assertContains("ceiling atmosphere shader json exposes atmosphere noise scale", ceilingAtmosphereShaderJson,
				"\"name\": \"AtmosphereNoiseScale\"");
		assertContains("ceiling atmosphere shader json exposes storm intensity", ceilingAtmosphereShaderJson,
				"\"name\": \"StormIntensity\"");
		assertContains("ceiling atmosphere shader json exposes atmosphere opacity", ceilingAtmosphereShaderJson,
				"\"name\": \"AtmosphereOpacity\"");
		assertContains("wall-top rim shader json points to vertex program", rimShaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_wall_top_rim\"");
		assertContains("wall-top rim shader json exposes rim glow intensity", rimShaderJson,
				"\"name\": \"RimGlowIntensity\"");
		assertContains("wall-top rim shader json exposes rim pulse speed", rimShaderJson,
				"\"name\": \"RimPulseSpeed\"");

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
		assertContains("wall membrane vertex shader passes wall angle", wallVertexShader,
				"wallAngleT =");
		assertContains("wall membrane vertex shader passes wall height", wallVertexShader,
				"wallHeightT =");
		assertContains("ceiling core vertex shader passes ceiling angle", ceilingCoreVertexShader,
				"ceilingAngleT =");
		assertContains("ceiling core vertex shader passes ceiling radius", ceilingCoreVertexShader,
				"ceilingRadialT =");
		assertContains("ceiling core vertex shader shifts the organic surface", ceilingCoreVertexShader,
				"organicShift");
		assertNotContains("ceiling core vertex shader should not use Java float suffixes", ceilingCoreVertexShader,
				"2f");
		assertContains("ceiling atmosphere vertex shader passes ceiling angle", ceilingAtmosphereVertexShader,
				"ceilingAngleT =");
		assertContains("ceiling atmosphere vertex shader passes ceiling radius", ceilingAtmosphereVertexShader,
				"ceilingRadialT =");
		assertContains("ceiling atmosphere vertex shader shifts the storm shell", ceilingAtmosphereVertexShader,
				"stormShift");
		assertNotContains("ceiling atmosphere vertex shader should not use Java float suffixes",
				ceilingAtmosphereVertexShader, "2f");

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
		assertContains("wall membrane fragment shader uses seam-safe cylindrical coordinates", wallFragmentShader,
				"unitCircle");
		assertContains("wall membrane fragment shader creates deep teal-black fungal fibers", wallFragmentShader,
				"deepTealBlack");
		assertContains("wall membrane fragment shader lifts the wall out of near-black", wallFragmentShader,
				"readableTealMembrane");
		assertContains("wall membrane fragment shader adds controlled readability without washing out black", wallFragmentShader,
				"wallReadabilityLift");
		assertContains("wall membrane fragment shader adds a low red wall glow", wallFragmentShader,
				"redLowWallGlow");
		assertContains("wall membrane fragment shader feathers the lower wall into the portal floor",
				wallFragmentShader, "wallFloorBlendFeather");
		assertContains("wall membrane fragment shader extends red glow down into the floor rim", wallFragmentShader,
				"portalFloorGlowBlend");
		assertContains("wall membrane fragment shader keeps pale web traces subtle", wallFragmentShader,
				"subtlePaleWebTrace");
		assertContains("wall membrane fragment shader brightens pale traces enough to read", wallFragmentShader,
				"visiblePaleTraceBoost");
		assertContains("wall membrane fragment shader raises wall opacity headroom", wallFragmentShader,
				"wallOpacityHeadroom");
		assertContains("wall membrane fragment shader fades before the ceiling pass", wallFragmentShader,
				"ceilingHandoffFade");
		assertNotContains("wall membrane fragment shader should not require a static texture sampler", wallFragmentShader,
				"sampler2D");
		assertNotContains("wall membrane fragment shader should not include nodules", wallFragmentShader,
				"wallNodule");
		assertNotContains("wall membrane fragment shader should not include suspended droplets", wallFragmentShader,
				"suspendedDroplet");
		assertContains("ceiling core fragment shader uses seam-safe circular coordinates", ceilingCoreFragmentShader,
				"unitCircle");
		assertContains("ceiling core fragment shader creates red and purple organic mass", ceilingCoreFragmentShader,
				"meatyCoreColor");
		assertContains("ceiling core fragment shader has a solid red fleshy inner core", ceilingCoreFragmentShader,
				"solidFleshCore");
		assertContains("ceiling core fragment shader fills the underside as solid tissue", ceilingCoreFragmentShader,
				"solidUndersideFill");
		assertContains("ceiling core fragment shader lifts the underside out of black-hole darkness",
				ceilingCoreFragmentShader, "undersideMassLift");
		assertContains("ceiling core fragment shader overwrites the pole with solid flesh color",
				ceilingCoreFragmentShader, "centerPoleFleshFill");
		assertContains("ceiling core fragment shader keeps void black away from the exact center",
				ceilingCoreFragmentShader, "centerVoidSuppression");
		assertContains("ceiling core vertex shader damps pole writhe to avoid lens distortion", ceilingCoreVertexShader,
				"centerWritheMask");
		assertContains("ceiling core fragment shader adds layered planetoid depth", ceilingCoreFragmentShader,
				"planetoidLayerDepth");
		assertContains("ceiling core fragment shader shades the limb as a rounded body", ceilingCoreFragmentShader,
				"planetoidLimbShade");
		assertContains("ceiling core vertex shader uses core undulation tuning", ceilingCoreVertexShader,
				"CoreUndulationIntensity");
		assertContains("ceiling core shader json declares core undulation uniform", ceilingCoreShaderJson,
				"\"CoreUndulationIntensity\"");
		assertNotContains("ceiling core fragment shader should not alpha-feather the bottom into a hole",
				ceilingCoreFragmentShader, "centerFeather");
		assertNotContains("ceiling core shader should not include atmosphere alpha masking", ceilingCoreFragmentShader,
				"stormAlphaMask");
		assertContains("ceiling core fragment shader creates black and white tendril traces", ceilingCoreFragmentShader,
				"blackWhiteTendrilTrace");
		assertContains("ceiling core fragment shader creates yellow glow lighting", ceilingCoreFragmentShader,
				"yellowBiolume");
		assertOrder("ceiling core fragment shader defines yellow glow before the underside uses it",
				ceilingCoreFragmentShader, "float yellowBiolume", "float undersideMassLift");
		assertContains("ceiling core fragment shader creates green orb glow", ceilingCoreFragmentShader,
				"greenOrbGlow");
		assertContains("ceiling core fragment shader defines x-axis rotation", ceilingCoreFragmentShader,
				"vec3 rotateX(");
		assertContains("ceiling core fragment shader defines y-axis rotation", ceilingCoreFragmentShader,
				"vec3 rotateY(");
		assertContains("ceiling core fragment shader defines z-axis rotation", ceilingCoreFragmentShader,
				"vec3 rotateZ(");
		assertContains("ceiling core fragment shader samples a rotated sphere position", ceilingCoreFragmentShader,
				"rotatedSpherePosition");
		assertContains("ceiling core fragment shader derives organic noise from 3d sphere motion", ceilingCoreFragmentShader,
				"sphericalMassFlow");
		assertNotContains("ceiling core fragment shader should not rely on a single flat rotating circle",
				ceilingCoreFragmentShader, "rotatingCircle");
		assertNotContains("ceiling core fragment shader should not own rim glow", ceilingCoreFragmentShader,
				"rimGlow");
		assertNotContains("ceiling core fragment shader should not reference rim glow uniform", ceilingCoreFragmentShader,
				"RimGlowIntensity");
		assertNotContains("ceiling core fragment shader should not require a static texture sampler", ceilingCoreFragmentShader,
				"sampler2D");
		assertNotContains("ceiling core fragment shader should not use Java float suffixes", ceilingCoreFragmentShader,
				"2f");
		assertContains("ceiling atmosphere fragment shader creates violent storm bands", ceilingAtmosphereFragmentShader,
				"violentStormBands");
		assertContains("ceiling atmosphere fragment shader creates broken transparent shell alpha",
				ceilingAtmosphereFragmentShader, "stormAlphaMask");
		assertContains("ceiling atmosphere fragment shader creates yellow storm blooms",
				ceilingAtmosphereFragmentShader, "yellowStormBloom");
		assertContains("ceiling atmosphere fragment shader creates green storm blooms",
				ceilingAtmosphereFragmentShader, "greenStormBloom");
		assertContains("ceiling atmosphere fragment shader uses storm intensity uniform",
				ceilingAtmosphereFragmentShader, "StormIntensity");
		assertContains("ceiling atmosphere fragment shader uses opacity uniform", ceilingAtmosphereFragmentShader,
				"AtmosphereOpacity");
		assertContains("ceiling atmosphere vertex shader uses independent storm undulation",
				ceilingAtmosphereVertexShader, "stormWrithe");
		assertContains("ceiling atmosphere shader json declares storm intensity uniform", ceilingAtmosphereShaderJson,
				"\"StormIntensity\"");
		assertContains("ceiling atmosphere shader json declares atmosphere opacity uniform",
				ceilingAtmosphereShaderJson, "\"AtmosphereOpacity\"");
		assertNotContains("ceiling atmosphere shader should not contain solid core fill logic",
				ceilingAtmosphereFragmentShader, "solidFleshCore");
		assertNotContains("ceiling atmosphere shader should not use shared mass layer uniform",
				ceilingAtmosphereFragmentShader, "MassLayer");
		assertNotContains("ceiling core shader should not use shared layer opacity uniform",
				ceilingCoreFragmentShader, "LayerOpacity");
		assertContains("rim vertex shader passes rim angle", rimVertexShader,
				"rimAngleT =");
		assertContains("rim vertex shader passes rim width", rimVertexShader,
				"rimWidthT =");
		assertContains("rim fragment shader creates a separate rim core", rimFragmentShader,
				"separateRimCore");
		assertContains("rim fragment shader creates a separate rim halo", rimFragmentShader,
				"separateRimHalo");
		assertContains("rim fragment shader derives glow hue from chamber effect vertex color", rimFragmentShader,
				"rimGlowColor = max(vertexColor.rgb");
		assertContains("rim fragment shader builds heat color from configurable rim color", rimFragmentShader,
				"rimHeatColor = mix(rimGlowColor");
		assertNotContains("rim fragment shader should not bake the rim glow as blood red", rimFragmentShader,
				"vec3 bloodRed");
		assertNotContains("rim fragment shader should not bake the rim glow as hot orange", rimFragmentShader,
				"vec3 hotOrange");
		assertNotContains("rim fragment shader should not force configurable rim colors toward yellow-white heat",
				rimFragmentShader, "vec3(1.0, 0.78, 0.48)");
		assertNotContains("rim fragment shader should not add a standalone yellow-white heat layer",
				rimFragmentShader, "whiteHeat");
		assertContains("rim fragment shader uses seam-safe angular phase", rimFragmentShader,
				"seamSafeRimPhase");
		assertContains("rim fragment shader keeps the pulse periodic across the uv wrap", rimFragmentShader,
				"sin(seamSafeRimPhase * 8.0 - time * 5.2 + pulseNoise * 1.4)");
		assertNotContains("rim fragment shader should not pulse directly from the wrapping uv coordinate",
				rimFragmentShader, "sin(rimAngleT *");
		assertNotContains("rim fragment shader should not require a static texture sampler", rimFragmentShader,
				"sampler2D");
		assertContains("reference documents apotheos floor funnel", reference,
				"APOTHEOS floor funnel");
		assertContains("reference documents apotheos wall membrane", reference,
				"APOTHEOS wall membrane");
		assertContains("reference documents apotheos ceiling mass", reference,
				"APOTHEOS ceiling mass");
		assertNotContains("reference should not document removed apotheos ceiling canopy", reference,
				"APOTHEOS ceiling canopy");
		assertContains("reference documents renderer-only floor treatment", reference,
				"renderer-only");
		assertContains("reference documents renderer-only ceiling treatment", reference,
				"ceiling mass is renderer-only");
		assertNotContains("reference should not document removed sparse ceiling drops", reference,
				"sparse high hanging drops");
		assertNotContains("reference should not document removed deep hanging ceiling funnel", reference,
				"deep hanging ceiling funnel");
		assertContains("lore reference documents apotheos ceiling mass", loreReference,
				"Apotheos ceiling mass");
		assertNotContains("lore reference should no longer say the apotheos ceiling remains pending", loreReference,
				"APOTHEOS ceiling remains pending");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertFileMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists " + path);
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

	private static void assertOrder(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
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
}
