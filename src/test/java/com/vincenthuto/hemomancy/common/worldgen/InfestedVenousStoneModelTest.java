package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfestedVenousStoneModelTest {

	@Test
	void infestedVenousStoneUsesAllHorizontalTextureRotations() throws IOException {
		JsonObject blockstate = JsonParser.parseString(Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/blockstates/infested_venous_stone.json")))
				.getAsJsonObject();
		JsonArray variants = blockstate.getAsJsonObject("variants").getAsJsonArray("");

		assertEquals(4, variants.size());
		Set<Integer> rotations = new HashSet<>();
		for (var variant : variants) {
			JsonObject model = variant.getAsJsonObject();
			assertEquals("hemomancy:block/infested_venous_stone", model.get("model").getAsString());
			rotations.add(model.get("y").getAsInt());
		}
		assertEquals(Set.of(0, 90, 180, 270), rotations);
		assertTrue(variants.size() > 1, "Infested Venous Stone needs position-selected texture rotations");
	}
}
