package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FungalGardensNoiseSettingsTest {
	@Test
	void fungalSeasDoNotFillEveryCavernBelowSeaLevel() throws IOException {
		JsonObject settings = resourceJson("data/hemomancy/worldgen/noise_settings/fungal_noise_settings.json");
		JsonElement initialDensity = settings.getAsJsonObject("noise_router")
				.get("initial_density_without_jaggedness");

		assertTrue(settings.get("aquifers_enabled").getAsBoolean(),
				"Disabled aquifers fill every open density below sea level with the default fluid");
		assertFalse(initialDensity.isJsonPrimitive() && initialDensity.getAsJsonPrimitive().isNumber()
				&& initialDensity.getAsDouble() == 0.0,
				"Aquifers need preliminary terrain density to distinguish seas from deep caverns");
	}

	@Test
	void bottomBedrockHasAHemorrhagicCrustCap() throws IOException {
		JsonObject settings = resourceJson("data/hemomancy/worldgen/noise_settings/fungal_noise_settings.json");
		JsonArray rules = settings.getAsJsonObject("surface_rule").getAsJsonArray("sequence");
		JsonObject bedrock = topLevelBlockRule(rules, "minecraft:bedrock").getAsJsonObject("if_true");
		JsonObject crust = topLevelBlockRule(rules, "hemomancy:hemorrhagic_crust").getAsJsonObject("if_true");

		assertEquals(0, bedrock.getAsJsonObject("true_at_and_below").get("above_bottom").getAsInt());
		assertEquals(1, bedrock.getAsJsonObject("false_at_and_above").get("above_bottom").getAsInt());
		assertEquals(1, crust.getAsJsonObject("true_at_and_below").get("above_bottom").getAsInt());
		assertEquals(2, crust.getAsJsonObject("false_at_and_above").get("above_bottom").getAsInt());
	}

	@Test
	void mainDeepCaveRuleUsesHemorrhagicCrust() throws IOException {
		JsonObject settings = resourceJson("data/hemomancy/worldgen/noise_settings/fungal_noise_settings.json");
		JsonArray rules = settings.getAsJsonObject("surface_rule").getAsJsonArray("sequence");
		JsonObject result = topLevelGradientRule(rules, "hemomancy:infested_venous_stone")
				.getAsJsonObject("then_run").getAsJsonObject("result_state");

		assertEquals("hemomancy:hemorrhagic_crust", result.get("Name").getAsString());
	}

	private static JsonObject topLevelBlockRule(JsonArray rules, String block) {
		for (JsonElement element : rules) {
			JsonObject rule = element.getAsJsonObject();
			JsonObject result = rule.getAsJsonObject("then_run");
			if (result != null && "minecraft:block".equals(result.get("type").getAsString())
					&& block.equals(result.getAsJsonObject("result_state").get("Name").getAsString())) {
				return rule;
			}
		}
		throw new AssertionError("Missing top-level surface rule for " + block);
	}

	private static JsonObject topLevelGradientRule(JsonArray rules, String randomName) {
		for (JsonElement element : rules) {
			JsonObject rule = element.getAsJsonObject();
			JsonObject condition = rule.getAsJsonObject("if_true");
			if (condition != null && "minecraft:vertical_gradient".equals(condition.get("type").getAsString())
					&& randomName.equals(condition.get("random_name").getAsString())) {
				return rule;
			}
		}
		throw new AssertionError("Missing top-level gradient rule " + randomName);
	}

	private static JsonObject resourceJson(String path) throws IOException {
		try (InputStream input = FungalGardensNoiseSettingsTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(input, "Missing resource " + path);
			return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}
}
