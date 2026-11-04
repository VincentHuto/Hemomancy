package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Fungal Gardens biomes list their own spawns through the generator (tools/worldgen/fungal_gardens_worldgen.py).
 * The puppeteer's Overworld spawn tag must not add them a second time at weight 24, which made the puppeteer most of
 * two biomes' monster weight (cleanup audit EC-4 / A-13).
 */
class PuppeteerSpawnlistResourceTest {
	@Test
	void puppeteerSpawnlistLeavesTheGardensToTheGenerator() {
		String values = ResourceReader.data("tags/worldgen/biome/blood_drunk_puppeteer_spawnlist.json")
				.getAsJsonArray("values").toString();
		assertFalse(values.contains("hemomancy:"), "a Gardens biome is listed twice: " + values);
		assertTrue(values.contains("minecraft:dark_forest"), "the Overworld haunts stay");
	}
}
