package com.vincenthuto.hemomancy.client.render.entity.mob.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillRendererSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private WillRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/will/WillRenderer.java");
		String renderTypes = read("src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
		String shader = read("src/main/resources/assets/hemomancy/shaders/core/item/will_state_monolith.fsh");
		String shaderJson = read("src/main/resources/assets/hemomancy/shaders/core/item/will_state_monolith.json");

		assertContains("Will renderer reads absorption progress from entity", renderer,
				"entity.getAbsorptionProgress(partialTicks)");
		assertContains("Will renderer accelerates flicker as absorption channel progresses", renderer,
				"absorptionFlickerSpeed(absorptionProgress)");
		assertContains("Will renderer passes flicker alpha into the monolith shell", renderer,
				"flickerAlpha");
		assertContains("Will renderer only applies fast absorption flicker while absorbing", renderer,
				"entity.getPhase() == WillPhase.ABSORBING");
		assertDoesNotContain("Will renderer no longer depends on absorption dissolve state", renderer,
				"isAbsorptionDissolving");
		assertContains("Will render type accepts dissolve progress", renderTypes,
				"float dissolveProgress");
		assertContains("Will render type accepts flicker alpha", renderTypes,
				"float flickerAlpha");
		assertContains("Will render type sends dissolve progress to shader", renderTypes,
				"setUniform(shader, \"WillDissolveProgress\", dissolveProgress)");
		assertContains("Will render type sends flicker alpha to shader", renderTypes,
				"setUniform(shader, \"WillDissolveFlicker\", flickerAlpha)");
		assertContains("Will shader has dissolve progress uniform", shader,
				"uniform float WillDissolveProgress;");
		assertContains("Will shader has dissolve flicker uniform", shader,
				"uniform float WillDissolveFlicker;");
		assertContains("Will shader applies flicker to alpha", shader,
				"alpha *= WillDissolveFlicker");
		assertContains("Will shader json defines dissolve progress uniform", shaderJson,
				"WillDissolveProgress");
		assertContains("Will shader json defines dissolve flicker uniform", shaderJson,
				"WillDissolveFlicker");
	}

	private static String read(String path) throws IOException {
		Path resolved = ROOT.resolve(path);
		if (!Files.exists(resolved)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(resolved).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (still contains '" + unexpected + "')");
		}
	}
}
