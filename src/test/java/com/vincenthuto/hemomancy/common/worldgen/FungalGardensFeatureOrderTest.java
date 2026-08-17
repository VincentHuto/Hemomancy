package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class FungalGardensFeatureOrderTest {

	@Test
	void sharedOverworldFeaturesUseTheModifierOrder() throws IOException {
		JsonObject biome = resourceJson("data/hemomancy/worldgen/biome/fungal_gardens.json");
		JsonArray vegetalFeatures = biome.getAsJsonArray("features").get(9).getAsJsonArray();

		assertFeatureBefore(vegetalFeatures, "hemomancy:bleeding_hearts", "hemomancy:small_infected_fungus");
	}

	private static void assertFeatureBefore(JsonArray features, String first, String second) {
		int firstIndex = features.toString().indexOf('"' + first + '"');
		int secondIndex = features.toString().indexOf('"' + second + '"');
		assertTrue(firstIndex >= 0, "missing feature " + first);
		assertTrue(secondIndex >= 0, "missing feature " + second);
		assertTrue(firstIndex < secondIndex, first + " must run before " + second);
	}

	private static JsonObject resourceJson(String path) throws IOException {
		try (InputStream input = FungalGardensFeatureOrderTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(input, "Missing resource " + path);
			return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}
}
