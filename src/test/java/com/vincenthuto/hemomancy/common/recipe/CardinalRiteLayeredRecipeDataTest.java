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
		// The special rites' floors come in the grand tier only.
		for (String special : new String[] {"apotheos/grand", "qliphoth/grand", "ancestral/grand"}) {
			assertTrue(pairs.contains(special), "missing special floor " + special);
		}
		assertEquals(19, pairs.size(), "four standard styles in four tiers, plus three grand special floors");
	}

	@Test
	void everyGashCellIsOneBlockAboveTheFloorPlane() {
		for (int half = 1; half <= 4; half++) {
			for (var cell : com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteGashRules.cells(half)) {
				assertEquals(1, cell.getY(), "a brazier stands on the floor, one block above its plane");
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
				// A rite that reads its braziers for optional offerings selects on floor and medium alone.
				assertTrue(recipe.has("brazier_signature") ^ recipe.has("optional_offerings"),
						path + " must define one selector");
				if (!recipe.has("brazier_signature")) continue;
				for (JsonElement element : recipe.getAsJsonArray("brazier_signature")) {
					assertFalse(element.getAsJsonObject().has("consume_on_success"),
							path + " must exercise the consume-by-default offering contract");
				}
			}
		}
	}

	@Test
	void requiredStructureUsesInlineLayeredPatternAndDefaultsToReusable() throws IOException {
		JsonObject recipe = read(RITE_ROOT.resolve("initiate_rite.json"));
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
		int workingCrafts = 0;
		// Walk subfolders too: Working crafts live under cardinal_rite/working/.
		try (var paths = Files.walk(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (recipe.has("unstained") && recipe.get("unstained").getAsBoolean()) continue;
				if (recipe.has("subject")) workingCrafts++;
				// A larger floor of the same style also accepts a smaller rite, so rites collide by style.
				String floor = recipe.get("floor").getAsString();
				String selector = floor.substring(0, floor.lastIndexOf('_')) + "|"
						+ (recipe.has("optional_offerings") ? "optional " + recipe.getAsJsonArray("optional_offerings")
								: recipe.getAsJsonArray("brazier_signature")) + "|"
						+ (recipe.has("medium") ? recipe.get("medium") : "no medium") + "|"
						+ (recipe.has("required_structure")
								? recipe.getAsJsonObject("required_structure").get("pattern") : "none") + "|"
						+ (recipe.has("subject") ? recipe.getAsJsonObject("subject").get("pattern") : "no subject");
				selectors.computeIfAbsent(selector, ignored -> new java.util.ArrayList<>()).add(recipe);
			}
		}
		assertTrue(workingCrafts >= 20, "the selector check must reach the Working crafts, found " + workingCrafts);
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

	@Test
	void everyAuthoredAnchorOccupiesItsOwnRingPosition() throws IOException {
		try (var paths = Files.list(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (!recipe.has("ceremony") || !recipe.getAsJsonObject("ceremony").has("anchors")) continue;
				Set<String> positions = new HashSet<>();
				for (JsonElement element : recipe.getAsJsonObject("ceremony").getAsJsonArray("anchors")) {
					JsonObject anchor = element.getAsJsonObject();
					if (!anchor.has("ring")) continue;
					// CardinalRiteRingTuning places four anchors per ring at order % 4.
					String slot = anchor.get("ring").getAsInt() + ":" + Math.floorMod(anchor.get("order").getAsInt(), 4);
					assertTrue(positions.add(slot), path + " stacks two anchors on ring slot " + slot);
				}
			}
		}
	}

	@Test
	void rankRitesGrantTheDegreeAboveTheirRequirement() throws IOException {
		Map<String, Integer> legacyTargets = Map.of(
				"sanguine_brotherhood", 4, "illuminatus_rite", 5, "sanctified_rite", 6,
				"archon_rite", 7, "apotheos_rite", 8);
		int rankRites = 0;
		try (var paths = Files.list(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (!recipe.has("rankup") || !recipe.get("rankup").getAsBoolean()) continue;
				rankRites++;
				String name = path.getFileName().toString().replace(".json", "");
				assertEquals(legacyTargets.get(name), recipe.get("required_degree").getAsInt() + 1,
						path + " must grant exactly the degree above its requirement");
			}
		}
		assertEquals(legacyTargets.size(), rankRites);
	}

	@Test
	void authoredCulminationsNameAKnownStyle() throws IOException {
		try (var paths = Files.list(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = read(path);
				if (!recipe.has("ceremony") || !recipe.getAsJsonObject("ceremony").has("culmination")) continue;
				String style = recipe.getAsJsonObject("ceremony").get("culmination").getAsString();
				assertNotNull(com.vincenthuto.hemomancy.common.rite.CardinalRiteCulmination.byId(style),
						path + " names unknown culmination " + style);
			}
		}
	}

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
