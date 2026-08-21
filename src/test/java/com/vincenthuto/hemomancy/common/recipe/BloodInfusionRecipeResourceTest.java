package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BloodInfusionRecipeResourceTest {
	@Test
	void stoneInfusesIntoVenousStoneForFiftyBlood() throws Exception {
		Path path = Path.of("src/main/resources/data/hemomancy/recipe/blood_infusion/venous_stone.json");
		JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

		assertEquals("hemomancy:blood_infusion", recipe.get("type").getAsString());
		assertEquals("minecraft:stone", recipe.get("input").getAsString());
		assertEquals(50.0D, recipe.get("blood_cost").getAsDouble());
		assertEquals("hemomancy:venous_stone", recipe.get("result").getAsString());
	}

	@Test
	void jeiRegistersTheRecipeAndBloodProjectionCatalyst() throws Exception {
		String plugin = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/compat/jei/JEIPlugin.java"));
		String language = Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/lang/en_us.json"));

		assertTrue(plugin.contains("BloodInfusionRecipeCategory.JEI_TYPE"));
		assertTrue(plugin.contains("new BloodInfusionRecipeCategory"));
		assertTrue(plugin.contains("ItemInit.blood_projection.get()), blood_infusion_recipe_type"));
		assertTrue(language.contains("\"hemomancy.jei.blood_infusion\""));
		assertTrue(language.contains("\"hemomancy.jei.blood_infusion.cost\""));
	}
}
