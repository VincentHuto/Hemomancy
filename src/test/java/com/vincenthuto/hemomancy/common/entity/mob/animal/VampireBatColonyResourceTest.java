package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VampireBatColonyResourceTest {
	@Test
	void naturalSpawnsCreateAColonyInsteadOfIsolatedBats() throws Exception {
		var root = JsonParser.parseString(Files.readString(Path.of(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_vampire_bat.json"))).getAsJsonObject();
		var spawn = root.getAsJsonArray("spawners").get(0).getAsJsonObject();
		assertEquals(4, spawn.get("minCount").getAsInt());
		assertEquals(8, spawn.get("maxCount").getAsInt());
	}

	@Test
	void coloniesLiveInTheNetherButNotItsWarpedOrSoulSandBiomes() throws Exception {
		var biomes = JsonParser.parseString(Files.readString(Path.of(
				"src/main/resources/data/hemomancy/tags/worldgen/biome/vampire_bat_spawnlist.json"))).getAsJsonObject()
				.getAsJsonArray("values");
		List<String> names = new ArrayList<>();
		biomes.forEach(biome -> names.add(biome.getAsString()));
		assertEquals(List.of("minecraft:nether_wastes", "minecraft:crimson_forest", "minecraft:basalt_deltas",
				"hemomancy:phlegethontic_basin"), names);
		assertFalse(names.contains("minecraft:warped_forest"));
		assertFalse(names.contains("minecraft:soul_sand_valley"));
		assertFalse(names.contains("#minecraft:is_nether"), "an explicit list, not the whole Nether tag");
	}
}
