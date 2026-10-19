package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A recipe decoded through NBT sees its booleans as bytes; they must still read as booleans. */
final class RecipeCodecJsonBooleanTest {
	@Test
	void nbtByteBooleansReadAsBooleans() {
		JsonObject json = new JsonObject();
		json.addProperty("special", 1);
		json.addProperty("unstained", 0);
		json.addProperty("rankup", true);
		json.addProperty("legacy", "true");

		assertTrue(RecipeCodecJson.booleanField(json, "special", false), "an NBT 1b is true");
		assertFalse(RecipeCodecJson.booleanField(json, "unstained", true), "an NBT 0b is false");
		assertTrue(RecipeCodecJson.booleanField(json, "rankup", false));
		assertTrue(RecipeCodecJson.booleanField(json, "legacy", false));
		assertTrue(RecipeCodecJson.booleanField(json, "missing", true), "absent fields take the fallback");
	}
}
