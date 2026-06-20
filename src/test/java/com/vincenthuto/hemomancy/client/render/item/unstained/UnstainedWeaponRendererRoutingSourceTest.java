package com.vincenthuto.hemomancy.client.render.item.unstained;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UnstainedWeaponRendererRoutingSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path ITEM_ROOT = ROOT.resolve(
			"src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool");
	private static final Path RENDERER_ROOT = ROOT.resolve(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained");

	private UnstainedWeaponRendererRoutingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String warhammerItem = read(ITEM_ROOT.resolve("UnstainedWarhammerItem.java"));
		String glaiveItem = read(ITEM_ROOT.resolve("SilthmereGlaiveItem.java"));
		String daggerItem = read(ITEM_ROOT.resolve("AbsolutionDaggerItem.java"));

		assertContains("warhammer item uses its renderer",
				warhammerItem, "new UnstainedWarhammerItemRenderer(");
		assertContains("glaive item uses its renderer",
				glaiveItem, "new SilthmereGlaiveItemRenderer(");
		assertContains("dagger item uses its renderer",
				daggerItem, "new AbsolutionDaggerItemRenderer(");

		assertDoesNotContain("warhammer item should not route through shared weapon renderer",
				warhammerItem, "UnstainedWeaponItemRenderer");
		assertDoesNotContain("glaive item should not route through shared weapon renderer",
				glaiveItem, "UnstainedWeaponItemRenderer");
		assertDoesNotContain("dagger item should not route through shared weapon renderer",
				daggerItem, "UnstainedWeaponItemRenderer");

		assertRenderer("warhammer renderer", "UnstainedWarhammerItemRenderer.java",
				"UnstainedWarhammerModel", "WARHAMMER_TEXTURE",
				"GUI_MODEL_SCALE = 0.46F", "GUI_MODEL_TRANSLATE_X = 0.42D",
				"GUI_MODEL_TRANSLATE_Y = -0.44D");
		assertRenderer("glaive renderer", "SilthmereGlaiveItemRenderer.java",
				"SilthmereGlaiveModel", "GLAIVE_TEXTURE",
				"GUI_MODEL_SCALE = 0.32F", "GUI_MODEL_TRANSLATE_X = 0.42D",
				"GUI_MODEL_TRANSLATE_Y = -0.4D");
		assertRenderer("dagger renderer", "AbsolutionDaggerItemRenderer.java",
				"AbsolutionDaggerModel", "DAGGER_TEXTURE",
				"GUI_MODEL_SCALE = 0.62F", "GUI_MODEL_TRANSLATE_X = 0.2D",
				"GUI_MODEL_TRANSLATE_Y = -0.2D");
	}

	private static void assertRenderer(String label, String fileName, String modelName,
			String textureName, String guiScale, String guiTranslateX, String guiTranslateY) throws IOException {
		Path path = RENDERER_ROOT.resolve(fileName);
		if (!Files.exists(path)) {
			throw new AssertionError(label + " missing at " + path);
		}
		String renderer = read(path);
		assertContains(label + " uses its model", renderer, modelName);
		assertContains(label + " owns its texture", renderer, textureName);
		assertContains(label + " owns its gui scale", renderer, guiScale);
		assertContains(label + " moves right in gui", renderer, guiTranslateX);
		assertContains(label + " moves up in gui", renderer, guiTranslateY);
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + " still contains: " + forbidden);
		}
	}
}
