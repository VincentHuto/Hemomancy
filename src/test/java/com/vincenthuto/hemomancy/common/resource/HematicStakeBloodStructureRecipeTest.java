package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class HematicStakeBloodStructureRecipeTest {
	private static final Path RECIPE = Path.of(
			"src/main/resources/data/hemomancy/recipe/blood_structure/hematic_stake.json");
	private static final Path AUTHORITY_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/HematicStakeAuthorityEvents.java");

	@Test
	void stakeUsesTheTwoBlockBloodStructureRecipe() throws IOException {
		JsonObject recipe = JsonParser.parseString(Files.readString(RECIPE)).getAsJsonObject();

		assertEquals("hemomancy:blood_structure_recipe", recipe.get("type").getAsString());
		assertEquals(100, recipe.get("bloodCost").getAsInt());
		assertEquals(0, recipe.get("required_degree").getAsInt());
		assertEquals("hemomancy:sanguine_formation", recipe.get("heldItem").getAsString());
		assertEquals("hemomancy:hematic_iron_bars", recipe.get("hitBlock").getAsString());
		assertEquals("hemomancy:hematic_stake", recipe.getAsJsonObject("result").get("id").getAsString());

		JsonArray pattern = recipe.getAsJsonArray("pattern");
		assertEquals(1, pattern.size());
		assertEquals("C", pattern.get(0).getAsJsonArray().get(0).getAsString());
		assertEquals("B", pattern.get(0).getAsJsonArray().get(1).getAsString());
		assertEquals("hemomancy:blood_crystal",
				recipe.getAsJsonObject("key").getAsJsonObject("C").get("block").getAsString());
		assertEquals("hemomancy:hematic_iron_bars",
				recipe.getAsJsonObject("key").getAsJsonObject("B").get("block").getAsString());
	}

	@Test
	void bareHandManifestationIsRemovedButBreakAuthorityRemains() throws IOException {
		String source = Files.readString(AUTHORITY_EVENTS);
		assertFalse(source.contains("PlayerInteractEvent.RightClickBlock"));
		assertFalse(source.contains("manifestStake"));
		assertTrue(source.contains("BlockEvent.BreakEvent"));
		assertTrue(source.contains("event.setCanceled(true)"));
	}
}
