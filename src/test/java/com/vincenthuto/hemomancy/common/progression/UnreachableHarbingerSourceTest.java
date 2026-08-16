package com.vincenthuto.hemomancy.common.progression;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UnreachableHarbingerSourceTest {
	private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe/blood_structure");

	private UnreachableHarbingerSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertBloodStructureSource("scarlet_vanity.json", 3, "hemomancy:scarlet_vanity");
		assertBloodStructureSource("fungal_implantation_pylon.json", 4,
				"hemomancy:fungal_implantation_pylon");
	}

	private static void assertBloodStructureSource(String file, int degree, String result) throws IOException {
		Path path = RECIPES.resolve(file);
		if (!Files.isRegularFile(path)) {
			throw new AssertionError(result + " has no survival Blood Structure source");
		}
		JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		assertEquals(file + " recipe type", "hemomancy:blood_structure_recipe", recipe.get("type").getAsString());
		assertEquals(file + " degree", degree, recipe.get("required_degree").getAsInt());
		assertEquals(file + " output", result, recipe.getAsJsonObject("result").get("id").getAsString());
		if (recipe.getAsJsonArray("pattern").isEmpty()) {
			throw new AssertionError(file + " must consume an authored world structure");
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
