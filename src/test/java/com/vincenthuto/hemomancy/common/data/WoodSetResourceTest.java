package com.vincenthuto.hemomancy.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Both wood families are complete vanilla-style sets: every piece has its assets, drops, recipe, name and tag. */
class WoodSetResourceTest {
	private static final List<String> FAMILIES = List.of("blood_wood", "heartwood");
	/** Piece id (with %s for the family) to the vanilla tag it belongs in, or null when a family tag carries it. */
	private static final Map<String, String> PIECES = Map.ofEntries(
			Map.entry("%s_log", "%logs"), Map.entry("%s_bark", "%logs"), Map.entry("stripped_%s_log", "%logs"),
			Map.entry("stripped_%s_bark", "%logs"), Map.entry("%s_planks", "planks"), Map.entry("%s_slab", "wooden_slabs"),
			Map.entry("%s_stairs", "wooden_stairs"), Map.entry("%s_fence", "wooden_fences"),
			Map.entry("%s_fence_gate", "fence_gates"), Map.entry("%s_door", "wooden_doors"),
			Map.entry("%s_trapdoor", "wooden_trapdoors"), Map.entry("%s_button", "wooden_buttons"),
			Map.entry("%s_pressure_plate", "wooden_pressure_plates"));
	private static final Map<String, String> SIGNS = Map.of("%s_sign", "signs", "%s_hanging_sign", "hanging_signs");
	private static final Map<String, String> BLOCK_ONLY_SIGNS = Map.of(
			"%s_sign", "standing_signs", "%s_wall_sign", "wall_signs",
			"%s_hanging_sign", "ceiling_hanging_signs", "%s_wall_hanging_sign", "wall_hanging_signs");
	private static final Pattern TEXTURE = Pattern.compile("\"hemomancy:((?:block|item)/[a-z0-9_/]+)\"");

	private static String id(String piece, String family) {
		return String.format(piece.replace("%s", "%1$s"), family);
	}

	private static List<String> values(String tag) {
		List<String> values = new ArrayList<>();
		for (JsonElement value : ResourceReader.text("data/minecraft/tags/" + tag + ".json").contains("\"values\"")
				? JsonParser.parseString(ResourceReader.text("data/minecraft/tags/" + tag + ".json")).getAsJsonObject().getAsJsonArray("values")
				: new com.google.gson.JsonArray()) {
			values.add(value.getAsString());
		}
		return values;
	}

	@Test
	void everyPieceHasABlockstateItemModelNameAndDrop() {
		List<String> missing = new ArrayList<>();
		for (String family : FAMILIES) {
			JsonObject lang = ResourceReader.asset("lang/en_us.json");
			List<String> pieces = new ArrayList<>(PIECES.keySet());
			pieces.addAll(SIGNS.keySet());
			for (String piece : pieces) {
				String id = id(piece, family);
				if (!ResourceReader.exists("assets/hemomancy/blockstates/" + id + ".json")) missing.add("blockstate " + id);
				if (!ResourceReader.exists("assets/hemomancy/models/item/" + id + ".json")) missing.add("item model " + id);
				if (!ResourceReader.exists("data/hemomancy/loot_table/blocks/" + id + ".json")) missing.add("loot " + id);
				if (!lang.has("block.hemomancy." + id)) missing.add("name " + id);
			}
			for (String wall : List.of("%s_wall_sign", "%s_wall_hanging_sign")) {
				String id = id(wall, family);
				if (!ResourceReader.exists("assets/hemomancy/blockstates/" + id + ".json")) missing.add("blockstate " + id);
			}
		}
		assertEquals(List.of(), missing);
	}

	@Test
	void everyPieceHasARecipe() {
		List<String> missing = new ArrayList<>();
		for (String family : FAMILIES) {
			for (String piece : List.of("%s_bark", "stripped_%s_bark", "%s_planks", "%s_slab", "%s_stairs", "%s_fence",
					"%s_fence_gate", "%s_door", "%s_trapdoor", "%s_button", "%s_pressure_plate", "%s_sign", "%s_hanging_sign")) {
				String id = id(piece, family);
				if (!ResourceReader.exists("data/hemomancy/recipe/" + id + ".json")) missing.add(id);
			}
		}
		assertEquals(List.of(), missing);
	}

	@Test
	void plankRecipesTakeAnyOfTheFamilysLogs() {
		for (String family : FAMILIES) {
			String recipe = ResourceReader.text("data/hemomancy/recipe/" + family + "_planks.json");
			assertTrue(recipe.contains("#hemomancy:" + family + "_logs") || recipe.contains("\"tag\": \"hemomancy:" + family + "_logs\""), recipe);
			String tag = ResourceReader.text("data/hemomancy/tags/item/" + family + "_logs.json");
			for (String piece : List.of("%s_log", "%s_bark", "stripped_%s_log", "stripped_%s_bark")) {
				assertTrue(tag.contains("hemomancy:" + id(piece, family)), id(piece, family));
			}
		}
	}

	@Test
	void everyPieceIsInItsVanillaTagsSoFencesJoinAndSticksCraft() {
		List<String> missing = new ArrayList<>();
		for (String family : FAMILIES) {
			for (var piece : PIECES.entrySet()) {
				if (piece.getValue().startsWith("%")) continue;
				String id = "hemomancy:" + id(piece.getKey(), family);
				if (!values("block/" + piece.getValue()).contains(id)) missing.add("block/" + piece.getValue() + " " + id);
				if (!values("item/" + piece.getValue()).contains(id)) missing.add("item/" + piece.getValue() + " " + id);
			}
			for (var sign : BLOCK_ONLY_SIGNS.entrySet()) {
				String id = "hemomancy:" + id(sign.getKey(), family);
				if (!values("block/" + sign.getValue()).contains(id)) missing.add("block/" + sign.getValue() + " " + id);
			}
			for (var sign : SIGNS.entrySet()) {
				String id = "hemomancy:" + id(sign.getKey(), family);
				if (!values("item/" + sign.getValue()).contains(id)) missing.add("item/" + sign.getValue() + " " + id);
			}
			for (String kind : List.of("block", "item")) {
				if (!values(kind + "/logs").contains("#hemomancy:" + family + "_logs")) missing.add(kind + "/logs " + family);
			}
		}
		assertEquals(List.of(), missing);
	}

	@Test
	void doorsAndTrapdoorsCutOutTheirWindows() {
		for (String family : FAMILIES) {
			for (String model : List.of("%s_door_bottom_left", "%s_door_top_right", "%s_trapdoor_bottom", "%s_trapdoor_open")) {
				String json = ResourceReader.text("assets/hemomancy/models/block/" + id(model, family) + ".json");
				assertTrue(json.contains("cutout"), id(model, family));
			}
		}
	}

	@Test
	void signsHaveTheirEntityAndGuiTextures() {
		List<String> missing = new ArrayList<>();
		for (String family : FAMILIES) {
			for (String path : List.of("entity/signs/%s", "entity/signs/hanging/%s", "gui/hanging_signs/%s", "item/%s_sign", "item/%s_hanging_sign", "item/%s_door")) {
				String file = "assets/hemomancy/textures/" + id(path, family) + ".png";
				if (!ResourceReader.exists(file)) missing.add(file);
			}
		}
		assertEquals(List.of(), missing);
	}

	@Test
	void everyTextureTheSetsModelsNameExists() throws Exception {
		TreeSet<String> missing = new TreeSet<>();
		try (var files = Files.walk(RepoPaths.ASSETS.resolve("models"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")
					&& (path.getFileName().toString().contains("blood_wood") || path.getFileName().toString().contains("bloodwood"))).toList()) {
				Matcher matcher = TEXTURE.matcher(Files.readString(file));
				while (matcher.find()) {
					String reference = matcher.group(1);
					// a parent or a model reference is a model; only texture slots name a texture
					if (reference.startsWith("block/") && ResourceReader.exists("assets/hemomancy/models/" + reference + ".json")
							&& !ResourceReader.exists("assets/hemomancy/textures/" + reference + ".png")) continue;
					if (!ResourceReader.exists("assets/hemomancy/textures/" + reference + ".png")) missing.add(reference);
				}
			}
		}
		assertEquals(new TreeSet<>(), missing);
	}

	@Test
	void theCoreDropsRavenousBloodwoodOnlyWhenFull() {
		String loot = ResourceReader.text("data/hemomancy/loot_table/blocks/heartwood_core.json");
		assertTrue(loot.contains("minecraft:alternatives"), loot);
		assertTrue(loot.contains("hemomancy:ravenous_heartwood"), loot);
		assertTrue(loot.contains("minecraft:block_state_property"), loot);
		assertTrue(loot.contains("\"fed\": \"4\""), loot);
		assertTrue(loot.contains("hemomancy:heartwood_log"), loot);
	}

	@Test
	void theCoreHasAGlowingModelForEveryStage() {
		JsonObject state = ResourceReader.asset("blockstates/heartwood_core.json");
		JsonObject variants = state.getAsJsonObject("variants");
		for (int fed = 0; fed <= 4; fed++) {
			assertTrue(variants.has("fed=" + fed), "fed=" + fed);
			String model = ResourceReader.text("assets/hemomancy/models/block/heartwood_core_" + fed + ".json");
			assertTrue(model.contains("neoforge_data") && model.contains("block_light"), "stage " + fed + " draws emissive");
			assertTrue(ResourceReader.exists("assets/hemomancy/textures/block/heartwood_core_glow_" + fed + ".png"), "glow " + fed);
		}
	}

	@Test
	void ravenousBloodwoodIsARealBlock() {
		assertTrue(ResourceReader.exists("assets/hemomancy/blockstates/ravenous_heartwood.json"));
		assertTrue(ResourceReader.exists("assets/hemomancy/models/item/ravenous_heartwood.json"));
		assertTrue(ResourceReader.exists("data/hemomancy/loot_table/blocks/ravenous_heartwood.json"));
		assertTrue(ResourceReader.asset("lang/en_us.json").has("block.hemomancy.ravenous_heartwood"));
		assertTrue(values("block/mineable/axe").contains("hemomancy:ravenous_heartwood"));
		assertTrue(values("block/mineable/axe").contains("hemomancy:heartwood_core"));
	}

	@org.junit.jupiter.api.Test
	void heartwoodWoodBurnsInTheDark() {
		for (String model : java.util.List.of("heartwood_log", "heartwood_log_horizontal", "heartwood_bark",
				"stripped_heartwood_log", "stripped_heartwood_log_horizontal", "stripped_heartwood_bark")) {
			String json = com.vincenthuto.hemomancy.support.ResourceReader.text("assets/hemomancy/models/block/" + model + ".json");
			org.junit.jupiter.api.Assertions.assertTrue(json.contains("block_light") && json.contains("_glow"), model + " has no emissive veins");
		}
		for (String texture : java.util.List.of("heartwood_log_glow", "stripped_heartwood_log_glow")) {
			org.junit.jupiter.api.Assertions.assertTrue(com.vincenthuto.hemomancy.support.ResourceReader.exists("assets/hemomancy/textures/block/" + texture + ".png"), texture);
		}
	}

	@org.junit.jupiter.api.Test
	void theCoreHasAnItemThatShowsItsGlow() {
		String model = ResourceReader.text("assets/hemomancy/models/item/heartwood_core.json");
		assertTrue(model.contains("hemomancy:block/heartwood_core_2"), "the icon shows a core with its fissures open");
	}
}
