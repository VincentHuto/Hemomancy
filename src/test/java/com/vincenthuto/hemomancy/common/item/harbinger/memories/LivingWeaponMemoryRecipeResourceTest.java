package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LivingWeaponMemoryRecipeResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponMemoryRecipeResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		livingWeaponMemoryRecipesAreRemovedFromSurvivalAcquisition();
	}

	private static void livingWeaponMemoryRecipesAreRemovedFromSurvivalAcquisition() throws IOException {
		List<String> names = List.of("blade", "axe", "spear", "claws", "crossbow", "torch", "flail");
		for (String name : names) {
			Path recipePath = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_living_" + name + ".json");
			if (Files.exists(recipePath)) {
				throw new AssertionError("Living weapon memory recipe should be replaced by graft recipe: " + recipePath);
			}
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
