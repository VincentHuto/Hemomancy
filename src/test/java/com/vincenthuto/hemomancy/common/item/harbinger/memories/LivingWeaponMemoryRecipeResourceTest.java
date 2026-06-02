package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LivingWeaponMemoryRecipeResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponMemoryRecipeResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		livingWeaponRecipesExistAndAreUnique();
	}

	private static void livingWeaponRecipesExistAndAreUnique() throws IOException {
		List<String> names = List.of("blade", "axe", "spear", "claws", "crossbow", "torch", "flail");
		Set<String> recipeFingerprints = new HashSet<>();
		for (String name : names) {
			Path recipePath = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_living_" + name + ".json");
			String recipe = Files.readString(recipePath).replace("\r\n", "\n");
			String compact = recipe.replaceAll("\\s+", "");
			assertContains("recipe result for " + name, compact, "\"result\":\"hemomancy:memory_living_" + name + "\"");
			assertContains("recipe has ferric-style living weapon enzyme", compact, "\"ferric\":1");
			String catalystsAndEnzymes = between(recipe, "\"catalysts\"", "\"blood\"");
			if (!recipeFingerprints.add(catalystsAndEnzymes.replaceAll("\\s+", ""))) {
				throw new AssertionError("Living weapon memory recipe is not unique for " + name);
			}
		}
	}

	private static String between(String text, String start, String end) {
		int startIndex = text.indexOf(start);
		int endIndex = text.indexOf(end, startIndex);
		if (startIndex < 0 || endIndex < 0 || endIndex < startIndex) {
			throw new AssertionError("Unable to find recipe section");
		}
		return text.substring(startIndex, endIndex);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
