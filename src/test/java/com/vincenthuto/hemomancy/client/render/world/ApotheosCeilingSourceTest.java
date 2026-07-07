package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApotheosCeilingSourceTest {
	private static final Path APOTHEOS_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ApotheosChamberEffects.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_membrane.json");
	private static final Path SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_membrane.vsh");
	private static final Path SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/apotheos_ceiling_membrane.fsh");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private ApotheosCeilingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertFileExists("apotheos ceiling shader json", SHADER_JSON);
		assertFileExists("apotheos ceiling vertex shader", SHADER_VERTEX);
		assertFileExists("apotheos ceiling fragment shader", SHADER_FRAGMENT);

		String apotheosEffects = read(APOTHEOS_EFFECTS);
		String shaderInit = read(SHADER_INIT);
		String renderTypes = read(RENDER_TYPES);
		String shaderJson = read(SHADER_JSON);
		String vertexShader = read(SHADER_VERTEX);
		String fragmentShader = read(SHADER_FRAGMENT);
		String reference = read(REFERENCE);

		assertContains("shader init declares apotheos ceiling shader", shaderInit,
				"APOTHEOS_CEILING_MEMBRANE");
		assertContains("shader init uses world apotheos ceiling shader path", shaderInit,
				"Hemomancy.rloc(\"world/apotheos_ceiling_membrane\")");
		assertContains("shader init registers apotheos ceiling shader", shaderInit,
				"registerShader(event, APOTHEOS_CEILING_MEMBRANE.createInstance(provider));");
		assertContains("shader init requests mass descent uniform", shaderInit,
				"\"MassDescent\"");
		assertContains("shader init requests rim fade start uniform", shaderInit,
				"\"RimFadeStart\"");
		assertContains("shader init requests rim fade end uniform", shaderInit,
				"\"RimFadeEnd\"");

		assertContains("ceiling render type method exists", renderTypes,
				"public static RenderType apotheosCeilingMembrane(");
		assertContains("ceiling render type uses apotheos ceiling shader shard", renderTypes,
				"ShaderInit.APOTHEOS_CEILING_MEMBRANE.getShard()");
		assertContains("ceiling render type uploads fiber scale", renderTypes,
				"setUniform(shader, \"FiberScale\", fiberScale);");
		assertContains("ceiling render type uploads red glow intensity", renderTypes,
				"setUniform(shader, \"RedGlowIntensity\", redGlowIntensity);");
		assertContains("ceiling render type uploads mass descent", renderTypes,
				"setUniform(shader, \"MassDescent\", massDescent);");
		assertContains("ceiling render type uploads rim fade start", renderTypes,
				"setUniform(shader, \"RimFadeStart\", rimFadeStart);");
		assertContains("ceiling render type uploads rim fade end", renderTypes,
				"setUniform(shader, \"RimFadeEnd\", rimFadeEnd);");
		assertContains("ceiling render type disables depth test for skybox-space ceiling", renderTypes,
				"RenderType.NO_DEPTH_TEST");
		assertContains("ceiling render type uses color-only write mask", renderTypes,
				"RenderType.COLOR_WRITE");

		assertContains("apotheos effects render the ceiling canopy after the floor funnel", apotheosEffects,
				"renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosCeilingCanopy");
		assertContains("apotheos effects render the descending mass after the canopy", apotheosEffects,
				"renderApotheosCeilingCanopy(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosCeilingMass");
		assertContains("apotheos effects render ceiling growths after the descending mass", apotheosEffects,
				"renderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());\n\t\trenderApotheosCeilingGrowths");
		assertContains("apotheos effects build a spanning canopy dome mesh", apotheosEffects,
				"emitApotheosCanopyMesh");
		assertContains("apotheos effects build a descending overhead mass mesh", apotheosEffects,
				"emitApotheosMassMesh");
		assertContains("apotheos effects give the mass a bulbous radius profile", apotheosEffects,
				"apotheosMassRadiusScale");
		assertContains("apotheos effects stud the mass with red glow nodes", apotheosEffects,
				"emitApotheosCeilingNode");
		assertContains("apotheos effects hang drip tendrils from the canopy", apotheosEffects,
				"emitApotheosCeilingTendril");
		assertContains("apotheos effects reuse the qliphoth glow render type for ceiling growths", apotheosEffects,
				"HemoRenderTypes.QLIPHOTH_GLOW");
		assertContains("apotheos effects span the canopy out to the wall rim radius", apotheosEffects,
				"APOTHEOS_CANOPY_OUTER_RADIUS_SCALE = 0.66F");
		assertContains("apotheos effects raise the canopy apex above the wall top", apotheosEffects,
				"APOTHEOS_CANOPY_APEX_Y_SCALE = 2.02F");
		assertContains("apotheos effects hang the mass tip down into the room", apotheosEffects,
				"APOTHEOS_MASS_TIP_Y_SCALE = 0.62F");
		assertContains("apotheos effects bulge the mass to a wide middle", apotheosEffects,
				"APOTHEOS_MASS_MAX_RADIUS_SCALE = 0.235F");
		assertContains("apotheos effects scale ceiling motion from chamber sky distance", apotheosEffects,
				"skyDistance *");
		assertContains("apotheos effects send shader time for ceiling animation", apotheosEffects,
				"time * APOTHEOS_CEILING_SHADER_TIME_SCALE");
		assertNotContains("apotheos ceiling should not be physical chamber terrain", apotheosEffects,
				"ChamberOfWillManager.FLOOR_Y");
		assertNotContains("apotheos ceiling should not depend on synced room radius", apotheosEffects,
				"ChamberOfWillClientData.radius()");

		assertContains("ceiling shader json points to vertex program", shaderJson,
				"\"vertex\": \"hemomancy:world/apotheos_ceiling_membrane\"");
		assertContains("ceiling shader json exposes fiber scale", shaderJson,
				"\"name\": \"FiberScale\"");
		assertContains("ceiling shader json exposes red glow intensity", shaderJson,
				"\"name\": \"RedGlowIntensity\"");
		assertContains("ceiling shader json exposes mass descent", shaderJson,
				"\"name\": \"MassDescent\"");
		assertContains("ceiling shader json exposes rim fade start", shaderJson,
				"\"name\": \"RimFadeStart\"");
		assertContains("ceiling shader json exposes rim fade end", shaderJson,
				"\"name\": \"RimFadeEnd\"");

		assertContains("ceiling vertex shader passes ceiling angle", vertexShader,
				"ceilAngleT =");
		assertContains("ceiling vertex shader passes ceiling radial", vertexShader,
				"ceilRadialT =");
		assertNotContains("ceiling vertex shader should not use java float suffixes", vertexShader,
				"2f");

		assertContains("ceiling fragment shader uses seam-safe cylindrical coordinates", fragmentShader,
				"unitCircle");
		assertContains("ceiling fragment shader creates deep teal-black fungal fibers", fragmentShader,
				"deepTealBlack");
		assertContains("ceiling fragment shader concentrates a descending mass core", fragmentShader,
				"descendingMassMask");
		assertContains("ceiling fragment shader glows red hottest at the mass tip", fragmentShader,
				"massCoreGlow");
		assertContains("ceiling fragment shader defines a hot core red", fragmentShader,
				"hotCoreRed");
		assertContains("ceiling fragment shader lifts the membrane out of near-black", fragmentShader,
				"readableTealMembrane");
		assertContains("ceiling fragment shader keeps pale web traces subtle", fragmentShader,
				"subtlePaleWebTrace");
		assertContains("ceiling fragment shader hands the rim back to the wall pass", fragmentShader,
				"rimHandoffFade");
		assertNotContains("ceiling fragment shader should not require a static texture sampler", fragmentShader,
				"sampler2D");
		assertNotContains("ceiling fragment shader should not use java float suffixes", fragmentShader,
				"2f");

		assertContains("reference documents the apotheos ceiling membrane pass", reference,
				"APOTHEOS ceiling membrane");
		assertContains("reference documents the descending overhead mass", reference,
				"descending overhead mass");
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
