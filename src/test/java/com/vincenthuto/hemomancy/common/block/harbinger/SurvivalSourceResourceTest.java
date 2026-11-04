package com.vincenthuto.hemomancy.common.block.harbinger;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Three blocks gate rites and crafts but had no survival source (cleanup audit A-3): the Suspended Blood Crystal
 * (D7 Enduring Pattern offering), the Suspended Cleansed Blood Crystal (eight Unstained rite patterns) and the Engram
 * Block (the Mnemonist Cradle ingredient). Each now has a recipe and drops itself, or the recipe uses an obtainable
 * ingredient instead.
 */
class SurvivalSourceResourceTest {
	@Test
	void suspendedBloodCrystalIsCraftedFromShardsAndDropsItself() {
		JsonObject recipe = ResourceReader.data("recipe/suspended_blood_crystal.json");
		assertEquals("hemomancy:suspended_blood_crystal", recipe.getAsJsonObject("result").get("id").getAsString());
		assertTrue(recipe.toString().contains("hemomancy:blood_crystal_shard"), "the crystal grows from shards");
		assertDropsItself("suspended_blood_crystal");
	}

	@Test
	void suspendedCleansedBloodCrystalIsPurifiedFromTheBloodCrystalAndDropsItself() {
		JsonObject recipe = ResourceReader.data(
				"recipe/white_humor_purification/suspended_blood_crystal_to_suspended_cleansed_blood_crystal.json");
		assertEquals("hemomancy:white_humor_purification", recipe.get("type").getAsString());
		assertEquals("hemomancy:suspended_blood_crystal", recipe.getAsJsonObject("input").get("item").getAsString());
		assertEquals("hemomancy:suspended_cleansed_blood_crystal",
				recipe.getAsJsonObject("result").get("id").getAsString());
		assertDropsItself("suspended_cleansed_blood_crystal");
	}

	@Test
	void mnemonistCradleUsesTheObtainableEngramStamp() {
		JsonObject recipe = ResourceReader.data("recipe/mnemonist_cradle.json");
		String keys = recipe.getAsJsonObject("key").toString();
		assertTrue(keys.contains("hemomancy:engram_stamp"), "cradle takes the Engram Stamp");
		assertFalse(keys.contains("hemomancy:engram_block"), "engram blocks never drop, so they cannot be an ingredient");
	}

	private static void assertDropsItself(String block) {
		JsonObject loot = ResourceReader.data("loot_table/blocks/" + block + ".json");
		assertEquals("minecraft:block", loot.get("type").getAsString());
		assertTrue(loot.toString().contains("\"hemomancy:" + block + "\""), block + " drops itself");
	}
}
