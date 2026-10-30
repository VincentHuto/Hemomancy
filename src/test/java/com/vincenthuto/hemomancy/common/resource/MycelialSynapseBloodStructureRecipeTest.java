package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MycelialSynapseBloodStructureRecipeTest {
	@Test
	void synapseIsATwoBlockBloodStructureForTheEighthDegree() {
		JsonObject recipe = ResourceReader.data("recipe/blood_structure/mycelial_synapse.json");
		assertEquals("hemomancy:blood_structure_recipe", recipe.get("type").getAsString());
		assertEquals(120, recipe.get("bloodCost").getAsInt());
		assertEquals(8, recipe.get("required_degree").getAsInt());
		assertEquals("hemomancy:sanguine_formation", recipe.get("heldItem").getAsString());
		assertEquals("hemomancy:hematic_iron_block", recipe.get("hitBlock").getAsString());
		assertEquals("hemomancy:mycelial_synapse", recipe.getAsJsonObject("result").get("id").getAsString());
		JsonArray row = recipe.getAsJsonArray("pattern").get(0).getAsJsonArray();
		assertEquals(1, recipe.getAsJsonArray("pattern").size());
		assertEquals("S", row.get(0).getAsString());
		assertEquals("I", row.get(1).getAsString());
		assertEquals("hemomancy:sporite_crystal", recipe.getAsJsonObject("key").getAsJsonObject("S").get("block").getAsString());
		assertEquals("hemomancy:hematic_iron_block", recipe.getAsJsonObject("key").getAsJsonObject("I").get("block").getAsString());
	}

	@Test
	void breakingASynapseReturnsItsInputsAndNoBlood() {
		JsonArray pools = ResourceReader.data("loot_table/blocks/mycelial_synapse.json").getAsJsonArray("pools");
		assertEquals(2, pools.size());
		assertEquals("hemomancy:hematic_iron_block", pools.get(0).getAsJsonObject().getAsJsonArray("entries")
				.get(0).getAsJsonObject().get("name").getAsString());
		assertEquals("hemomancy:sporite_crystal", pools.get(1).getAsJsonObject().getAsJsonArray("entries")
				.get(0).getAsJsonObject().get("name").getAsString());
	}
}
