package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class ScarStationRendererLightingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private ScarStationRendererLightingResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/ScarStationRenderer.java"));

		assertContains("scar station displayed item light should sample the tabletop", renderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("scar station displayed item light should be capped below fullbright", renderer,
				"DISPLAY_ITEM_MAX_LIGHT");
		assertContains("scar station should render tabletop stacks with world item lighting context", renderer,
				"renderStatic(stack, ItemDisplayContext.GROUND, itemLight");
		assertNotContains("scar station should not render tabletop stacks with fixed display lighting context",
				renderer, "renderStatic(stack, ItemDisplayContext.FIXED");
		assertNotContains("scar station should not render displayed stacks with raw block-entity light",
				renderer, "renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn");
	}
}
