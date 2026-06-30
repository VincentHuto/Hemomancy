package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicLowtideParchmentSourceTest {
	private static final Path LOWTIDE_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java");
	private static final Path RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path SHADER_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");
	private static final Path PARCHMENT_ATLAS = Path.of(
			"src/main/resources/assets/hemomancy/textures/world/lowtide_parchment/parchment_atlas.png");
	private static final Path PARCHMENT_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_parchment.json");
	private static final Path PARCHMENT_VERTEX_SHADER = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_parchment.vsh");
	private static final Path PARCHMENT_FRAGMENT_SHADER = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_parchment.fsh");

	private MnemonicLowtideParchmentSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String lowtideEffects = read(LOWTIDE_EFFECTS);
		String renderTypes = read(RENDER_TYPES);
		String shaderInit = read(SHADER_INIT);
		String reference = read(REFERENCE);
		String parchmentRenderType = slice(renderTypes, "public static RenderType mnemonicLowtideParchment",
				"public static RenderType loomOrbShell");

		assertContains("parchment render type exists", renderTypes,
				"MNEMONIC_LOWTIDE_PARCHMENT");
		assertContains("parchment texture path exists in render types", renderTypes,
				"textures/world/lowtide_parchment/parchment_atlas.png");
		assertContains("parchment render type uses position-uv-color vertices", parchmentRenderType,
				"DefaultVertexFormat.POSITION_TEX_COLOR");
		assertContains("parchment shader is registered", shaderInit,
				"MNEMONIC_LOWTIDE_PARCHMENT");
		assertContains("parchment shader uses position-uv-color vertices", shaderInit,
				"DefaultVertexFormat.POSITION_TEX_COLOR");
		assertContains("parchment shader caches time uniform", shaderInit,
				"\"HemoTime\"");
		assertContains("parchment shader caches wind ripple uniforms", shaderInit,
				"\"WindRippleStrength\"");
		assertContains("parchment render type uses the custom parchment shader", parchmentRenderType,
				"ShaderInit.MNEMONIC_LOWTIDE_PARCHMENT.getShard()");
		assertContains("parchment render type sets shader uniforms", parchmentRenderType,
				"mnemonic_lowtide_parchment_uniforms");
		assertContains("parchment render type sets wind strength", parchmentRenderType,
				"WindRippleStrength");
		assertContains("parchment render type sets wind scale", parchmentRenderType,
				"WindRippleScale");
		assertContains("parchment render type sets wind direction", parchmentRenderType,
				"WindDirection");
		assertContains("water parchment wind ripple stays visible without distance scaling", lowtideEffects,
				"LOWTIDE_PARCHMENT_WATER_WIND_STRENGTH = 0.055F");
		assertContains("air parchment wind ripple stays visible without distance scaling", lowtideEffects,
				"LOWTIDE_PARCHMENT_AIR_WIND_STRENGTH = 0.120F");
		assertNotContains("parchment wind ripple should not scale by sky distance", lowtideEffects,
				"skyDistance * windStrength");
		assertNotContains("parchment render type should not use the new-entity shader layout", parchmentRenderType,
				"RENDERTYPE_ENTITY_TRANSLUCENT_SHADER");
		assertNotContains("parchment render type should no longer be color-only", parchmentRenderType,
				"DefaultVertexFormat.POSITION_COLOR");
		assertContains("parchment render type binds a texture state", parchmentRenderType,
				"TextureStateShard");
		assertContains("parchment render type uses translucent blending", parchmentRenderType,
				"RenderType.TRANSLUCENT_TRANSPARENCY");
		assertContains("parchment render type does not depth test", parchmentRenderType,
				"RenderType.NO_DEPTH_TEST");
		assertContains("parchment render type writes color only", parchmentRenderType,
				"RenderType.COLOR_WRITE");
		assertContains("parchment render type disables culling", parchmentRenderType,
				"RenderType.NO_CULL");
		assertContains("parchment render type disables lightmap", parchmentRenderType,
				"RenderType.NO_LIGHTMAP");

		assertContains("renderer defines water parchment entry", lowtideEffects,
				"renderLowtideWaterParchment(");
		assertContains("renderer defines airborne parchment entry", lowtideEffects,
				"renderLowtideAirParchment(");
		assertContains("water parchment renders after ruins and before lake", lowtideEffects,
				"renderLowtideRuinedStructures(context.poseStack(), context.time(), context.skyDistance());\n"
						+ "\t\trenderLowtideWaterParchment(context.poseStack(), context.time(), context.skyDistance());\n"
						+ "\t\trenderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("air parchment renders after lake and before watery fog", lowtideEffects,
				"renderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());\n"
						+ "\t\trenderLowtideAirParchment(context.poseStack(), context.time(), context.skyDistance());\n"
						+ "\t\trenderLowtideWateryFog(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("water parchment count is sparse", lowtideEffects,
				"LOWTIDE_WATER_PARCHMENT_COUNT = 24");
		assertContains("air parchment count is sparser than water", lowtideEffects,
				"LOWTIDE_AIR_PARCHMENT_COUNT = 12");
		assertContains("parchment uses a deterministic seed", lowtideEffects,
				"LOWTIDE_PARCHMENT_SEED");
		assertContains("parchment uses the lowtide parchment render type", lowtideEffects,
				"HemoRenderTypes.mnemonicLowtideParchment(");
		assertContains("parchment emits textured sheets", lowtideEffects,
				"emitLowtideTexturedParchmentSheet");
		assertContains("parchment has atlas uv variants", lowtideEffects,
				"LOWTIDE_PARCHMENT_ATLAS_VARIANTS");
		assertContains("parchment surface is subdivided for paper curl", lowtideEffects,
				"LOWTIDE_PARCHMENT_GRID_COLUMNS");
		assertContains("parchment emits uv vertices", lowtideEffects,
				"addLowtideParchmentTexturedVertex");
		assertContains("parchment vertices set texture coordinates", lowtideEffects,
				".setUv(");
		assertNotContains("parchment should not use procedural body color strokes", lowtideEffects,
				"lowtideParchmentBodyColor");
		assertNotContains("parchment should not use procedural stain strokes", lowtideEffects,
				"emitLowtideParchmentStains");
		assertNotContains("parchment should not use procedural glyph strokes", lowtideEffects,
				"emitLowtideParchmentGlyphs");
		assertNotContains("parchment should not emit generic color-only strokes", lowtideEffects,
				"emitLowtideParchmentStroke");
		assertContains("parchment has near pieces closer than the ruin field", lowtideEffects,
				"LOWTIDE_PARCHMENT_NEAR_DISTANCE_SCALE");
		assertContains("parchment still has distant pieces behind the near layer", lowtideEffects,
				"LOWTIDE_PARCHMENT_DISTANCE_LAYER_PERIOD");
		assertContains("air parchment is pitched upright enough to read in the chamber", lowtideEffects,
				"LOWTIDE_PARCHMENT_AIR_MIN_UPRIGHT_PITCH");
		assertContains("air parchment uses the upright pitch range", lowtideEffects,
				"Mth.lerp(pieceRandom.nextFloat(), LOWTIDE_PARCHMENT_AIR_MIN_UPRIGHT_PITCH");
		assertContains("parchment is positioned from skybox distance", lowtideEffects,
				"skyDistance *");

		assertNotContains("parchment should not use world entities", lowtideEffects,
				"EntityType");
		assertNotContains("parchment should not spawn particles", lowtideEffects,
				"addParticle");
		assertNotContains("parchment should not use packets", lowtideEffects,
				"Packet");
		assertNotContains("parchment should not use synced chamber radius", lowtideEffects,
				"ChamberOfWillClientData.radius()");
		assertNotContains("parchment should not use physical chamber floor", lowtideEffects,
				"ChamberOfWillManager.FLOOR_Y");

		assertContains("reference documents renderer-only floating parchment", reference,
				"floating torn parchment");
		assertContains("reference documents parchment is visual only", reference,
				"renderer-only");
		assertContains("reference documents parchment wind ripple", reference,
				"wind-rippling");
		assertFileExists("parchment texture atlas asset exists", PARCHMENT_ATLAS);
		assertFileExists("parchment shader json exists", PARCHMENT_SHADER_JSON);
		assertFileExists("parchment vertex shader exists", PARCHMENT_VERTEX_SHADER);
		assertFileExists("parchment fragment shader exists", PARCHMENT_FRAGMENT_SHADER);
		String parchmentShaderJson = read(PARCHMENT_SHADER_JSON);
		String parchmentVertexShader = read(PARCHMENT_VERTEX_SHADER);
		String parchmentFragmentShader = read(PARCHMENT_FRAGMENT_SHADER);
		assertContains("parchment shader json points to vertex shader", parchmentShaderJson,
				"hemomancy:world/mnemonic_lowtide_parchment");
		assertContains("parchment shader json samples parchment texture", parchmentShaderJson,
				"Sampler0");
		assertContains("parchment vertex shader uses wind strength", parchmentVertexShader,
				"WindRippleStrength");
		assertStartsWith("parchment vertex shader starts with a valid GLSL version directive", parchmentVertexShader,
				"#version 150");
		assertContains("parchment vertex shader normalizes the four-column atlas x coordinate", parchmentVertexShader,
				"pageUv = vec2(fract(UV0.x * 4.0), UV0.y)");
		assertNotContains("parchment vertex shader should not fold full-height atlas y into zero damping",
				parchmentVertexShader, "pageUv = fract(UV0 * 4.0)");
		assertContains("parchment vertex shader still samples the atlas uv", parchmentVertexShader,
				"texCoord0 = UV0;");
		assertNotContains("parchment vertex shader should not reference the removed uv variable", parchmentVertexShader,
				"texCoord0 = uv;");
		assertContains("parchment vertex shader ripples vertices", parchmentVertexShader,
				"rippleOffset");
		assertContains("parchment fragment shader samples atlas", parchmentFragmentShader,
				"texture(Sampler0");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
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

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertStartsWith(String label, String text, String expectedPrefix) {
		if (!text.startsWith(expectedPrefix)) {
			throw new AssertionError(label + ": expected prefix " + expectedPrefix);
		}
	}

	private static String slice(String text, String startMarker, String endMarker) {
		int start = text.indexOf(startMarker);
		if (start < 0) {
			throw new AssertionError("slice start missing: " + startMarker);
		}
		int end = text.indexOf(endMarker, start);
		if (end < 0) {
			throw new AssertionError("slice end missing: " + endMarker);
		}
		return text.substring(start, end);
	}
}
