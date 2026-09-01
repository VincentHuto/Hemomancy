package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteLayeredRecipeDataTest {
	private static final Path FLOOR_ROOT =
			Path.of("src/main/resources/data/hemomancy/cardinal_rite_floor");
	private static final Path RITE_ROOT =
			Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	@Test
	void everyFloorStyleHasAnIndependentJsonForEveryTier() throws IOException {
		Set<String> pairs = new HashSet<>();
		try (var paths = Files.list(FLOOR_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject floor = read(path);
				assertNotNull(floor.getAsJsonArray("pattern"), path + " pattern");
				assertNotNull(floor.getAsJsonObject("key"), path + " key");
				pairs.add(floor.get("style").getAsString() + "/" + floor.get("tier").getAsString());
			}
		}

		for (String style : new String[] {"threshold", "communion", "working", "dominion"}) {
			for (String tier : new String[] {"minor", "lesser", "greater", "grand"}) {
				assertTrue(pairs.contains(style + "/" + tier), "missing " + style + "/" + tier);
			}
		}
		assertEquals(16, pairs.size());
	}

	@Test
	void everyFloorPlacesBrazierSocketsInTheAirAboveItsBlocks() throws IOException {
		try (var paths = Files.list(FLOOR_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				for (JsonElement socket : read(path).getAsJsonArray("brazier_sockets")) {
					assertEquals(1, socket.getAsJsonArray().get(1).getAsInt(),
							path + " brazier socket must be one block above the floor plane");
				}
			}
		}
	}

	@Test
	void everyHarbingerRiteUsesFloorAndNoLongerOwnsAWholePattern() throws IOException {
		try (var paths = Files.list(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (recipe.has("unstained") && recipe.get("unstained").getAsBoolean()) continue;
				assertTrue(recipe.has("floor"), path + " must name its minimum floor");
				assertFalse(recipe.has("pattern"), path + " must not retain a whole-rite pattern");
				assertTrue(recipe.has("brazier_signature"), path + " must define its selector");
				for (JsonElement element : recipe.getAsJsonArray("brazier_signature")) {
					assertFalse(element.getAsJsonObject().has("consume_on_success"),
							path + " must exercise the consume-by-default offering contract");
				}
			}
		}
	}

	@Test
	void requiredStructureUsesInlineLayeredPatternAndDefaultsToReusable() throws IOException {
		JsonObject recipe = read(RITE_ROOT.resolve("votary_rite.json"));
		JsonObject structure = recipe.getAsJsonObject("required_structure");

		assertNotNull(structure);
		assertTrue(structure.has("pattern"));
		assertTrue(structure.has("key"));
		assertFalse(structure.has("consume_on_success"),
				"omitting consume_on_success must exercise the false default");
	}

	@Test
	void preBrazierRitesMayUseAnEmptySignature() throws IOException {
		JsonObject recipe = read(RITE_ROOT.resolve("sanguine_attunement.json"));
		assertFalse(recipe.has("required_structure"));
		JsonArray signature = recipe.getAsJsonArray("brazier_signature");
		assertTrue(signature.isEmpty(),
				"degree-one rites must not introduce braziers before their lesson");
	}

	@Test
	void simultaneouslyAvailableHarbingerSelectorsAreNotIndistinguishable() throws IOException {
		Map<String, java.util.List<JsonObject>> selectors = new HashMap<>();
		try (var paths = Files.list(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (recipe.has("unstained") && recipe.get("unstained").getAsBoolean()) continue;
				String selector = recipe.get("floor") + "|"
						+ recipe.getAsJsonArray("brazier_signature") + "|"
						+ (recipe.has("required_structure")
								? recipe.getAsJsonObject("required_structure").get("pattern") : "none");
				selectors.computeIfAbsent(selector, ignored -> new java.util.ArrayList<>()).add(recipe);
			}
		}
		for (java.util.List<JsonObject> duplicates : selectors.values()) {
			if (duplicates.size() < 2) continue;
			Set<Integer> degrees = new HashSet<>();
			for (JsonObject recipe : duplicates) {
				assertTrue(recipe.has("rankup") && recipe.get("rankup").getAsBoolean(),
						"only exact-degree rank rites may share a physical selector");
				assertTrue(degrees.add(recipe.get("required_degree").getAsInt()),
						"shared rank-rite selectors must occupy different degree windows");
			}
		}
	}

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
