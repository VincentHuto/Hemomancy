package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

/** The ordeal's key, a vial of the professor's own blood, and the only vial the rite's brazier takes (2026-11-17). */
class OrdealKeyTest {
	@Test
	void theKeyRecordsItsProfessor() {
		CompoundTag tag = OrdealKey.keyData("circus_strongman");
		assertEquals("hemomancy:circus_strongman", tag.getString("entity_type"));
		assertTrue(tag.getBoolean("troupe_key"));
		assertTrue(OrdealKey.isKeyData(tag, "circus_strongman"));
		assertFalse(OrdealKey.isKeyData(tag, "circus_acrobat"));
	}

	@Test
	void onlyTheGiftFitsTheBrazier() {
		CompoundTag drawn = new CompoundTag();
		drawn.putString("entity_type", "hemomancy:circus_strongman");
		assertFalse(OrdealKey.isKeyData(drawn, "circus_strongman"), "a vial drawn some other way is no key");
		JsonObject recipe = ResourceReader.data("recipe/cardinal_rite/puppeteer_trial_gorebound_hulk.json");
		boolean found = false;
		for (var entry : recipe.getAsJsonArray("brazier_signature")) {
			JsonObject ingredient = entry.getAsJsonObject().getAsJsonObject("ingredient");
			if (!"neoforge:components".equals(ingredient.has("type") ? ingredient.get("type").getAsString() : "")) continue;
			assertEquals("hemomancy:bloody_vial", ingredient.get("items").getAsString());
			assertTrue(ingredient.get("strict").getAsBoolean());
			JsonObject custom = ingredient.getAsJsonObject("components").getAsJsonObject("minecraft:custom_data");
			assertEquals("hemomancy:circus_strongman", custom.get("entity_type").getAsString());
			assertTrue(custom.get("troupe_key").getAsBoolean());
			found = true;
		}
		assertTrue(found, "the Hulk's ordeal asks for the Strongman's blood");
	}
}
