package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SpecimenJarItemRendererSourceTest {
	private static final Path ITEM_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/functional/SpecimenJarBlockItem.java");
	private static final Path ITEM_MODEL = Path.of(
			"src/main/resources/assets/hemomancy/models/item/specimen_jar.json");
	private static final Path ITEM_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/tile/harbinger/functional/SpecimenJarItemRenderer.java");
	private static final Path TILE_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/SpecimenJarRenderer.java");

	@Test
	void specimenJarItemRendererIsWired() throws IOException {
		String item = (Files.readString(ITEM_SOURCE) + Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/item/ItemClientExtensions.java"))).replace("\r\n", "\n");
		assertContains("specimen jar item exposes client renderer extensions", item,
				"Map.entry(SpecimenJarBlockItem.class");
		assertContains("specimen jar item registers the item renderer", item,
				"new SpecimenJarItemRenderer");

		String model = Files.readString(ITEM_MODEL).replace("\r\n", "\n");
		assertContains("specimen jar item model opts into custom item rendering", model,
				"\"parent\": \"builtin/entity\"");

		String renderer = Files.readString(ITEM_RENDERER).replace("\r\n", "\n");
		assertContains("item renderer draws the static jar block", renderer, "renderSingleBlock");
		assertContains("item renderer reads the stored specimen", renderer, "SpecimenJarData.getSpecimen(stack)");
		assertContains("item renderer reuses dynamic specimen rendering", renderer, "renderSpecimen");

		String tileRenderer = Files.readString(TILE_RENDERER).replace("\r\n", "\n");
		assertContains("jar renderer measures all specimen dimensions", tileRenderer, "getBoundingBox()");
		assertContains("jar renderer clamps oversized specimens to the inner jar", tileRenderer,
				"specimenFitScale");
		assertContains("jar renderer shrinks long-bodied specimens with compact hitboxes", tileRenderer,
				"longBodiedVisualLength");
		assertContains("jar renderer recenters head-biased long specimens inside the jar", tileRenderer,
				"applySpecimenVisualOffset");
		assertContains("jar renderer shrinks broad flat specimens that otherwise press through glass", tileRenderer,
				"broadSpecimenVisualWidth");
		assertContains("jar renderer keeps captured specimens visually suspended", tileRenderer,
				"freezeCapturedSpecimen");
		assertDoesNotContain("jar renderer must not force walking legs in jars", tileRenderer,
				"walkAnimation.update(0.25F, 1.0F)");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
