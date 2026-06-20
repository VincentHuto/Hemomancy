package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodStructureStationProgressionRecipeTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String BLOOD_STRUCTURE_RECIPE = "\"type\": \"hemomancy:blood_structure_recipe\"";

	private BloodStructureStationProgressionRecipeTest() {
	}

	public static void main(String[] args) throws IOException {
		assertStationRecipe("Vial Centrifuge", "vial_centrifuge", 1, "hemomancy:vial_centrifuge");
		assertStationRecipe("Ghastly Alembic", "ghastly_alembic", 2, "hemomancy:ghastly_alembic");
		assertStationRecipe("Somatic Loom", "somatic_loom", 3, "hemomancy:somatic_loom");
		assertStationRecipe("Cerebral Scarring Station", "runic_chisel_station", 4, "hemomancy:scar_station");
		assertStationRecipe("Mason's Effigy", "mason_effigy", 4, "hemomancy:mason_effigy");
		assertStationRecipe("Liber Sanguinum", "liber_sanguinum", 1, "hemomancy:liber_sanguinum");
		assertStationRecipe("Hematic Iron Block", "hematic_iron_block", 1, "hemomancy:hematic_iron_block");
	}

	private static void assertStationRecipe(String label, String recipeName, int degree, String resultId)
			throws IOException {
		String recipe = read("src/main/resources/data/hemomancy/recipe/blood_structure/" + recipeName + ".json");
		assertContains(label + " is blood-structure crafted", recipe, BLOOD_STRUCTURE_RECIPE);
		assertContains(label + " unlocks at expected degree", recipe, "\"required_degree\": " + degree);
		assertContains(label + " crafts the expected block", recipe, "\"id\": \"" + resultId + "\"");
	}

	private static String read(String path) throws IOException {
		Path resolved = ROOT.resolve(path);
		if (!Files.exists(resolved)) {
			throw new AssertionError("Missing recipe file: " + path);
		}
		return Files.readString(resolved);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
