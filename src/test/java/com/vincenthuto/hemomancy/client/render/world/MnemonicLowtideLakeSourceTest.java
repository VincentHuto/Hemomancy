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
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private MnemonicLowtideLakeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertFileExists("lowtide effects class", LOWTIDE_EFFECTS);
		assertFileExists("lowtide shader json", SHADER_JSON);
		assertFileExists("lowtide vertex shader", SHADER_VERTEX);
		assertFileExists("lowtide fragment shader", SHADER_FRAGMENT);

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
		assertContains("client event imports lowtide effects", clientEvents,
				"import com.vincenthuto.hemomancy.client.render.world.chamberofwill.MnemonicLowtideChamberEffects;");
		assertContains("client event renders lake after translucent blocks", clientEvents,
				"MnemonicLowtideChamberEffects.renderLake(event);");

		assertContains("shader init declares lowtide lake shader", shaderInit,
				"MNEMONIC_LOWTIDE_LAKE");
		assertContains("shader init uses world lowtide shader path", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_lake\")");
		assertContains("shader init registers lowtide lake shader", shaderInit,
				"registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));");

		assertContains("render type method exists", renderTypes,
				"public static RenderType mnemonicLowtideLake(");
		assertContains("render type uses lowtide shader shard", renderTypes,
				"ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard()");
		assertContains("render type keeps depth test enabled", renderTypes,
				"RenderType.LEQUAL_DEPTH_TEST");
		assertContains("render type uses color-only write mask", renderTypes,
				"RenderType.COLOR_WRITE");

		assertContains("renderer only runs in chamber dimension", lowtideEffects,
				"level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)");
		assertContains("renderer only runs for lowtide theme", lowtideEffects,
				"ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE.equals(ChamberSkyThemeRegistry.activeTheme().id())");
		assertContains("renderer uses synced chamber radius", lowtideEffects,
				"ChamberOfWillClientData.radius()");
		assertContains("renderer snaps to chamber spacing", lowtideEffects,
				"ChamberOfWillManager.CHAMBER_SPACING");
		assertContains("renderer uses requested lake depth", lowtideEffects,
				"private static final float LAKE_DEPTH = 1.25F;");

		assertContains("shader json points to vertex program", shaderJson,
				"\"vertex\": \"hemomancy:world/mnemonic_lowtide_lake\"");
		assertContains("shader json exposes wave strength", shaderJson,
				"\"name\": \"WaveStrength\"");
		assertContains("shader json exposes gloss strength", shaderJson,
				"\"name\": \"GlossStrength\"");

		assertContains("vertex shader displaces y", vertexShader,
				"surfacePosition.y += waveLift;");
		assertContains("vertex shader damps edges", vertexShader,
				"smoothstep(0.0, EdgeFade, edge)");
		assertContains("fragment shader uses fbm", fragmentShader,
				"float fbm(vec2 value)");
		assertContains("fragment shader names parchment highlights", fragmentShader,
				"parchmentHighlight");
		assertContains("fragment shader uses glossy highlight", fragmentShader,
				"GlossStrength");

		assertContains("reference documents lowtide subfloor tide", reference,
				"Mnemonic Lowtide");
		assertContains("reference documents unreachable nearby lake", reference,
				"nearby but unreachable");
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
}
