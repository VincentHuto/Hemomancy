package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TermiteMoundPlacementResourceTest {

	@Test
	void packagedPlacementUsesTheIntendedSavannaRarity() throws IOException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(
				"data/hemomancy/worldgen/placed_feature/termite_mound.json")) {
			assertNotNull(input);
			JsonObject placement = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
					.getAsJsonObject().getAsJsonArray("placement").get(0).getAsJsonObject();
			assertEquals(60, placement.get("chance").getAsInt());
		}
	}
}
