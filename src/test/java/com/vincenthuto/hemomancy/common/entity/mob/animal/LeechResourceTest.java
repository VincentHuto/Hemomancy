package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Leeches are the only source of the Swollen Leech, a required offering of the Morphling Incubator craft, so they
 * must spawn naturally (cleanup audit EC-1 / A-2) rather than only from an egg or breeding.
 */
class LeechResourceTest {
	@Test
	void leechesSpawnNaturallyInSwamps() {
		JsonObject modifier = ResourceReader.data("neoforge/biome_modifier/add_leech.json");
		assertEquals("neoforge:add_spawns", modifier.get("type").getAsString());
		assertEquals("#hemomancy:leech_spawnlist", modifier.get("biomes").getAsString());
		JsonObject spawner = modifier.getAsJsonArray("spawners").get(0).getAsJsonObject();
		assertEquals("hemomancy:leech", spawner.get("type").getAsString());
		assertTrue(spawner.get("weight").getAsInt() > 0, "leech spawn weight");

		JsonObject tag = ResourceReader.data("tags/worldgen/biome/leech_spawnlist.json");
		var values = tag.getAsJsonArray("values");
		assertFalse(values.isEmpty(), "leech spawnlist is empty");
		assertTrue(values.toString().contains("minecraft:swamp"), "leeches belong in swamps");
	}

	@Test
	void leechesHaveAnAnimalSpawnPlacement() {
		String entityInit = SourceAssertions.readMainJava("common/init/EntityInit.java");
		SourceAssertions.assertContains("leech spawn placement", entityInit, "event.register(EntityInit.leech.get()");
	}
}
