package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScarStationRendererLightingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private ScarStationRendererLightingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/ScarStationRenderer.java"));

		assertContains("scar station displayed item light should sample the tabletop", renderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("scar station displayed item light should be capped below fullbright", renderer,
				"DISPLAY_ITEM_MAX_LIGHT");
		assertContains("scar station should render tabletop stacks with world item lighting context", renderer,
				"renderStatic(stack, ItemDisplayContext.GROUND, itemLight");
		assertDoesNotContain("scar station should not render tabletop stacks with fixed display lighting context",
				renderer, "renderStatic(stack, ItemDisplayContext.FIXED");
		assertDoesNotContain("scar station should not render displayed stacks with raw block-entity light",
				renderer, "renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn");
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

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
