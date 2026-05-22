package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MycelialLanternRendererLightingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private MycelialLanternRendererLightingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/MycelialLanternRenderer.java"));

		assertContains("mycelial lantern displayed item light should sample the chamber", renderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("mycelial lantern displayed item light should be capped below fullbright", renderer,
				"DISPLAY_ITEM_MAX_LIGHT");
		assertContains("mycelial lantern should pass capped light into dynamic item rendering", renderer,
				"renderDisplayItem(te, display, partialTick, poseStack, buffers, itemLight, overlay);");
		assertContains("mycelial lantern should render displayed stacks with world item lighting context", renderer,
				"renderStatic(stack, ItemDisplayContext.GROUND, itemLight, overlay, poseStack, buffers, te.getLevel(), 0)");
		assertDoesNotContain("mycelial lantern should not render displayed stacks with raw block-entity light",
				renderer, "renderDisplayItem(te, display, partialTick, poseStack, buffers, light, overlay);");
		assertDoesNotContain("mycelial lantern should not render displayed stacks without a level context",
				renderer, "renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack, buffers, null, 0)");
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
