package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScryingPodiumShaderResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private ScryingPodiumShaderResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String scryingRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/ScryingPodiumRenderer.java"));
		String fungalRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/FungalPodiumRenderer.java"));
		String waterShader = read(RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/water.vsh"));
		String waterShaderJson = read(RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/water.json"));

		assertContains("scrying podium bakes pool vertices through the block pose",
				scryingRenderer, "addWavyVertex(vertex, mat,");
		assertContains("fungal podium keeps dormant pool renderer port-safe",
				fungalRenderer, "addWavyVertex(vertex, mat,");
		assertNotContains("scrying podium does not send a batched custom modelview uniform",
				scryingRenderer, "safeGetUniform(\"modelview\")");
		assertNotContains("fungal podium does not send a batched custom modelview uniform",
				fungalRenderer, "safeGetUniform(\"modelview\")");
		assertContains("water shader uses the vanilla camera model-view path",
				waterShader, "ProjMat * ModelViewMat * vec4(Position, 1.0)");
		assertNotContains("water shader does not use the old per-render custom modelview uniform",
				waterShader, "uniform mat4 modelview");
		assertNotContains("water shader json does not expose the old custom modelview uniform",
				waterShaderJson, "\"name\": \"modelview\"");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": contains " + forbidden);
		}
	}
}
