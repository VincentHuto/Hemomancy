package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LivingBloodPriceDataTest {
	@Test
	void bloodRushWeavesForOneHundredMlAndTwoEnzymes() {
		JsonObject recipe = ResourceReader.data("recipe/memory_weaving/memory_blood_rush.json");
		assertEquals(100, recipe.get("blood").getAsInt(), "CB-17");
		JsonObject enzymes = recipe.getAsJsonObject("enzymes");
		Map<String, Integer> expected = Map.of("animus", 1, "flammeus", 1);
		for (String school : enzymes.keySet()) {
			assertEquals(expected.getOrDefault(school, 0).intValue(), enzymes.get(school).getAsInt(), school);
		}
	}

	@Test
	void morphicNectarTurnsOneScrapIntoOnePowder() {
		JsonObject recipe = ResourceReader.data("recipe/morphic_nectar/hematic_iron_scrap_to_powder.json");
		assertEquals("hemomancy:hematic_iron_powder", recipe.getAsJsonObject("result").get("id").getAsString());
		assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt(), "NQ-01-14: the braze back makes no scrap");
	}
}
