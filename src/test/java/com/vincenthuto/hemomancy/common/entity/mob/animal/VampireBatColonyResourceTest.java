package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VampireBatColonyResourceTest {
	@Test
	void naturalSpawnsCreateAColonyInsteadOfIsolatedBats() throws Exception {
		var root = JsonParser.parseString(Files.readString(Path.of(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_vampire_bat.json"))).getAsJsonObject();
		var spawn = root.getAsJsonArray("spawners").get(0).getAsJsonObject();
		assertEquals(8, spawn.get("minCount").getAsInt());
		assertEquals(20, spawn.get("maxCount").getAsInt());
	}
}
