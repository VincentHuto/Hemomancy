package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphicTonicRecipeResourceTest {
	@Test
	void aBloodyJugSteepedInMorphicNectarBecomesTheTonic() {
		JsonObject recipe = ResourceReader.data("recipe/morphic_nectar/bloody_jug_to_morphic_tonic.json");
		assertEquals("hemomancy:morphic_nectar", recipe.get("type").getAsString());
		assertEquals("hemomancy:bloody_jug", recipe.getAsJsonObject("input").get("item").getAsString());
		assertEquals("hemomancy:morphic_tonic", recipe.getAsJsonObject("result").get("id").getAsString());
		assertEquals(100, recipe.get("transform_time").getAsInt());
		assertTrue(ResourceReader.exists("assets/hemomancy/models/item/morphic_tonic.json"));
		assertTrue(ResourceReader.exists("assets/hemomancy/textures/item/morphic_tonic.png"));
	}
}
