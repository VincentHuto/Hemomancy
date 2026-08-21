package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class FieldMycologyResourceTest {

	@Test
	void normalFungiDropTheHarvestedPlant() throws IOException {
		assertSinglePlantDrop("infected_fungus");
		assertSinglePlantDrop("stinkhorn_fungus");
		assertSinglePlantDrop("puffball_fungus");
		assertSinglePlantDrop("sarcodes");
		assertSinglePlantDrop("rafflesia");
		assertSinglePlantDrop("devils_tooth");
	}

	@Test
	void bleedingHeartHasDistinctNormalAndSilkTouchDrops() throws IOException {
		String loot = resourceText("data/hemomancy/loot_table/blocks/bleeding_heart.json");
		assertTrue(loot.contains("hemomancy:bleeding_heart"));
		assertTrue(loot.contains("hemomancy:bleeding_bulb"));
		assertTrue(loot.contains("minecraft:silk_touch"));
		assertTrue(loot.contains("minecraft:explosion_decay"));
	}

	@Test
	void specialPotsReturnTheirPlantAndPot() throws IOException {
		assertPottedDrop("ghost_pipe");
		assertPottedDrop("sarcodes");
		assertPottedDrop("lethean_poppy");
	}

	@Test
	void fieldFungiSelectorIncludesAllThreeFungi() throws IOException {
		String json = resourceText("data/hemomancy/worldgen/configured_feature/small_infected_fungus.json");
		assertEquals(1, occurrences(json, "\"Name\": \"hemomancy:infected_fungus\""));
		assertEquals(1, occurrences(json, "\"Name\": \"hemomancy:stinkhorn_fungus\""));
		assertEquals(1, occurrences(json, "\"Name\": \"hemomancy:puffball_fungus\""));
		assertEquals(0, occurrences(json, "\"Name\": \"hemomancy:bleeding_heart\""));
	}

	@Test
	void flowerFeaturesOnlyDeclareRealBlockStateProperties() throws IOException {
		String fieldFungi = resourceText("data/hemomancy/worldgen/configured_feature/small_infected_fungus.json");
		String bleedingHearts = resourceText("data/hemomancy/worldgen/configured_feature/bleeding_hearts.json");
		String stinkhorns = resourceText("data/hemomancy/worldgen/configured_feature/stink_horns.json");
		assertFalse(fieldFungi.contains("\"half\""));
		assertFalse(bleedingHearts.contains("\"half\""));
		assertFalse(stinkhorns.contains("\"half\""));
	}

	@Test
	void sarcodesRetainsVanillaGrassSurvival() throws IOException {
		String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/plant/SarcodesBlock.java"));
		assertTrue(source.contains("super.canSurvive(state, level, pos)"),
				"Sarcodes must retain FlowerBlock survival for grass and other dirt-tag soils");
	}

	@Test
	void overworldBiomeModifierMakesFieldFungiReachable() throws IOException {
		JsonObject modifier = resourceJson("data/hemomancy/neoforge/biome_modifier/add_field_fungi.json");
		assertEquals("#minecraft:is_overworld", modifier.get("biomes").getAsString());
		assertEquals("hemomancy:small_infected_fungus", modifier.get("features").getAsString());
		assertEquals("vegetal_decoration", modifier.get("step").getAsString());
	}

	@Test
	void rareFieldFungiAlwaysAttemptPlacementAfterPassingTheirRarityFilter() throws IOException {
		for (String feature : new String[] { "small_infected_fungus", "stink_horns", "sarcodes" }) {
			JsonArray placement = resourceJson("data/hemomancy/worldgen/placed_feature/" + feature + ".json")
					.getAsJsonArray("placement");
			boolean hasUnitCount = false;
			for (var modifier : placement) {
				JsonObject object = modifier.getAsJsonObject();
				if ("minecraft:count".equals(object.get("type").getAsString())
						&& object.get("count").isJsonPrimitive() && object.get("count").getAsInt() == 1) {
					hasUnitCount = true;
				}
			}
			assertTrue(hasUnitCount, feature + " must not add a second zero-attempt roll");
		}
	}

	@Test
	void allHemomancyFloraPlacementDensityIsTripled() throws IOException {
		for (String feature : new String[] { "bleeding_hearts", "small_infected_fungus", "stink_horns",
				"lethean_poppies", "ghost_pipes", "sarcodes", "rafflesia", "devils_tooth", "mycelium_blob" }) {
			assertLiteralCount(feature, 1, 3);
		}

		JsonObject hyphaeCount = placement("patch_hyphae").get(0).getAsJsonObject();
		assertEquals(15, hyphaeCount.get("below_noise").getAsInt());
		assertEquals(30, hyphaeCount.get("above_noise").getAsInt());
		assertLiteralCount("huge_fungus", 0, 12);
		assertLiteralCount("hyphae_tendril", 0, 96);
		assertWeightedCounts("mushroom/canopy_mushrooms_sparse", 9, 12);
		assertWeightedCounts("mushroom/canopy_mushrooms_dense", 15, 18);
		assertLiteralCount("erythrocoral_reef", 0, 18);
	}

	@Test
	void rafflesiaIsRareButFindable() throws IOException {
		JsonArray placement = resourceJson("data/hemomancy/worldgen/placed_feature/rafflesia.json")
				.getAsJsonArray("placement");
		assertEquals(24, placement.get(0).getAsJsonObject().get("chance").getAsInt());
	}

	@Test
	void rafflesiaWorldgenUsesItsLogAttachmentFeature() throws IOException {
		JsonObject configured = resourceJson("data/hemomancy/worldgen/configured_feature/rafflesia.json");
		assertEquals("hemomancy:rafflesia", configured.get("type").getAsString());
	}

	@Test
	void rafflesiaWallRotationsPutItsStemAgainstTheSupportingLog() throws IOException {
		JsonObject variants = resourceJson("assets/hemomancy/blockstates/rafflesia.json")
				.getAsJsonObject("variants");
		for (String facing : new String[] { "up", "north", "south", "east", "west" }) {
			assertTrue(variants.has("facing=" + facing), "missing Rafflesia model for " + facing);
		}
		assertEquals(270, variants.getAsJsonObject("facing=north").get("x").getAsInt());
		assertEquals(90, variants.getAsJsonObject("facing=south").get("x").getAsInt());
		assertEquals(270, variants.getAsJsonObject("facing=east").get("x").getAsInt());
		assertEquals(90, variants.getAsJsonObject("facing=east").get("y").getAsInt());
		assertEquals(270, variants.getAsJsonObject("facing=west").get("x").getAsInt());
		assertEquals(270, variants.getAsJsonObject("facing=west").get("y").getAsInt());
	}

	@Test
	void plantLanesHaveRepeatableInfrastructureRecipes() throws IOException {
		JsonObject infestedWood = resourceJson("data/hemomancy/recipe/infested_wood.json");
		assertTrue(infestedWood.getAsJsonArray("ingredients").toString().contains("hemomancy:foul_paste"));
		assertEquals("hemomancy:infested_wood", infestedWood.getAsJsonObject("result").get("id").getAsString());

		JsonObject devilsTooth = resourceJson("data/hemomancy/recipe/distillation/devils_tooth.json");
		assertEquals("hemomancy:devils_tooth", devilsTooth.getAsJsonObject("ingredient").get("item").getAsString());
		assertEquals("hemomancy:foul_paste", devilsTooth.get("result").getAsString());
	}

	private static void assertSinglePlantDrop(String name) throws IOException {
		JsonObject loot = resourceJson("data/hemomancy/loot_table/blocks/" + name + ".json");
		String text = loot.toString();
		assertTrue(text.contains("hemomancy:" + name), name + " must drop itself");
		assertTrue(text.contains("minecraft:survives_explosion"), name + " must respect explosions");
	}

	private static void assertPottedDrop(String name) throws IOException {
		JsonObject loot = resourceJson("data/hemomancy/loot_table/blocks/potted_" + name + ".json");
		JsonArray pools = loot.getAsJsonArray("pools");
		assertEquals(2, pools.size());
		String text = loot.toString();
		assertTrue(text.contains("minecraft:flower_pot"));
		assertTrue(text.contains("hemomancy:" + name));
		assertTrue(text.contains("minecraft:survives_explosion"));
	}

	private static JsonObject resourceJson(String path) throws IOException {
		return JsonParser.parseString(resourceText(path)).getAsJsonObject();
	}

	private static JsonArray placement(String feature) throws IOException {
		return resourceJson("data/hemomancy/worldgen/placed_feature/" + feature + ".json")
				.getAsJsonArray("placement");
	}

	private static void assertLiteralCount(String feature, int index, int expected) throws IOException {
		JsonObject count = placement(feature).get(index).getAsJsonObject();
		assertEquals("minecraft:count", count.get("type").getAsString(), feature);
		assertEquals(expected, count.get("count").getAsInt(), feature);
	}

	private static void assertWeightedCounts(String feature, int first, int second) throws IOException {
		JsonArray distribution = placement(feature).get(0).getAsJsonObject().getAsJsonObject("count")
				.getAsJsonArray("distribution");
		assertEquals(first, distribution.get(0).getAsJsonObject().get("data").getAsInt(), feature);
		assertEquals(second, distribution.get(1).getAsJsonObject().get("data").getAsInt(), feature);
	}

	private static String resourceText(String path) throws IOException {
		try (InputStream input = FieldMycologyResourceTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(input, "Missing resource " + path);
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static int occurrences(String text, String needle) {
		int count = 0;
		for (int index = text.indexOf(needle); index >= 0; index = text.indexOf(needle, index + needle.length())) {
			count++;
		}
		return count;
	}
}
