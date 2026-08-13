package com.vincenthuto.hemomancy.common.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

final class WarpChairAssetTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final Path CHAIR_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/WarpChairRenderer.java");

	@Test
	void blockEntityRendererUsesTheBakedJsonModel() throws IOException {
		String source = Files.readString(CHAIR_RENDERER);
		assertTrue(source.contains("getBlockModel(state)"));
		assertTrue(source.contains("getModelRenderer().renderModel("));
		assertFalse(source.contains("WarpChairModel"));
	}

	@Test
	void regularBlockAndItemModelsAreWiredTogether() throws IOException {
		JsonObject model = readJson(ASSETS.resolve("models/block/warp_chair.json"));
		JsonArray elements = model.getAsJsonArray("elements");
		assertFalse(elements.isEmpty(), "chair needs authored cubes");
		JsonObject item = readJson(ASSETS.resolve("models/item/warp_chair.json"));
		assertEquals("hemomancy:block/warp_chair", item.get("parent").getAsString());
	}

	@Test
	void runtimeResourcesExposeAllFourFacingsAndNoFillerItem() throws IOException {
		JsonObject blockstate = readJson(ASSETS.resolve("blockstates/warp_chair.json"));
		JsonObject variants = blockstate.getAsJsonObject("variants");
		assertEquals(4, variants.size());
		for (String facing : new String[] { "north", "south", "east", "west" }) {
			assertTrue(variants.has("facing=" + facing));
		}
		assertEquals(0, rotation(variants, "north"));
		assertEquals(180, rotation(variants, "south"));
		assertEquals(90, rotation(variants, "east"));
		assertEquals(270, rotation(variants, "west"));
		assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/warp_chair.json")));
		assertFalse(Files.exists(ASSETS.resolve("models/item/warp_chair_filler.json")));
	}

	@Test
	void survivalAndProgressionResourcesArePresent() throws IOException {
		Path data = Path.of("src/main/resources/data/hemomancy");
		Path recipePath = data.resolve("recipe/blood_structure/warp_chair.json");
		assertFalse(Files.exists(data.resolve("recipe/warp_chair.json")));
		JsonObject recipe = readJson(recipePath);
		assertEquals("hemomancy:blood_structure_recipe", recipe.get("type").getAsString());
		assertEquals(250, recipe.get("bloodCost").getAsInt());
		assertEquals(3, recipe.get("required_degree").getAsInt());
		assertEquals("hemomancy:sanguine_formation", recipe.get("heldItem").getAsString());
		assertEquals("hemomancy:blood_wood_planks", recipe.get("hitBlock").getAsString());
		assertEquals("hemomancy:warp_chair",
				recipe.getAsJsonObject("result").get("id").getAsString());

		JsonArray pattern = recipe.getAsJsonArray("pattern");
		assertEquals(3, pattern.size(), "chair structure needs three depth aisles");
		for (int aisle = 0; aisle < pattern.size(); aisle++) {
			JsonArray rows = pattern.get(aisle).getAsJsonArray();
			assertEquals(3, rows.size(), "each chair aisle needs three vertical rows");
			for (int row = 0; row < rows.size(); row++) {
				assertEquals(3, rows.get(row).getAsString().length(), "chair rows must be three blocks wide");
			}
		}

		JsonArray offerings = recipe.getAsJsonArray("offerings");
		assertEquals(2, offerings.size());
		assertOffering(offerings.get(0).getAsJsonObject(), "hemomancy:blood_crystal", 2);
		assertOffering(offerings.get(1).getAsJsonObject(), "minecraft:ender_pearl", 2);
		assertTrue(Files.isRegularFile(data.resolve("loot_table/blocks/warp_chair.json")));
		assertTrue(Files.isRegularFile(data.resolve("advancement/hemomancy/warp_chair_bound.json")));
		assertTrue(Files.isRegularFile(data.resolve("advancement/hemomancy/chamber_rite_attuned.json")));
		assertTrue(Files.isRegularFile(ASSETS.resolve("textures/entity/model_sanguine_monolith.png")));
	}

	private static JsonObject readJson(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "missing " + path);
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}

	private static int rotation(JsonObject variants, String facing) {
		JsonObject variant = variants.getAsJsonObject("facing=" + facing);
		return variant.has("y") ? variant.get("y").getAsInt() : 0;
	}

	private static void assertOffering(JsonObject offering, String item, int count) {
		assertEquals(item, offering.getAsJsonObject("ingredient").get("item").getAsString());
		assertEquals(count, offering.get("count").getAsInt());
	}
}
