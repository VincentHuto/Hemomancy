package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class MycelialLanternRendererLightingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private MycelialLanternRendererLightingResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/MycelialLanternRenderer.java"));

		assertContains("mycelial lantern displayed item light should sample the chamber", renderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("mycelial lantern displayed item light should be capped below fullbright", renderer,
				"DISPLAY_ITEM_MAX_LIGHT");
		assertContains("mycelial lantern should pass capped light into dynamic item rendering", renderer,
				"renderDisplayItem(te, display, partialTick, poseStack, buffers, itemLight, overlay);");
		assertContains("mycelial lantern should render displayed stacks with world item lighting context", renderer,
				"renderStatic(stack, ItemDisplayContext.GROUND, itemLight, overlay, poseStack, buffers, te.getLevel(), 0)");
		assertNotContains("mycelial lantern should not render displayed stacks with raw block-entity light",
				renderer, "renderDisplayItem(te, display, partialTick, poseStack, buffers, light, overlay);");
		assertNotContains("mycelial lantern should not render displayed stacks without a level context",
				renderer, "renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack, buffers, null, 0)");
	}
}
