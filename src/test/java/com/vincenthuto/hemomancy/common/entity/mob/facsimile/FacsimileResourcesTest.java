package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacsimileResourcesTest {
	static final List<String> IDS = List.of("facsimile_bat", "facsimile_enderman", "facsimile_blaze",
			"facsimile_golem", "facsimile_horse", "facsimile_donkey", "facsimile_wolf", "facsimile_spider");
	static final List<String> DIMENSION_BIOMES = List.of("mycelial_meadows", "fungal_isles", "sporecrown_thicket",
			"hyphal_spires", "drifting_mycelium", "mycelial_depths", "hemorrhagic_plateau");

	@Test
	void everyFacsimileDropsOnlySporeSacsToAPlayer() {
		for (String id : IDS) {
			String loot = ResourceReader.text("data/hemomancy/loot_table/entities/" + id + ".json");
			assertTrue(loot.contains("hemomancy:spore_sac"), id);
			assertTrue(loot.contains("minecraft:killed_by_player"), id);
			assertFalse(loot.contains("minecraft:looting_enchant"), id);
		}
	}

	@Test
	void everyFacsimileHasNamesAnEggAndItsGrowth() {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		for (String id : IDS) {
			assertTrue(lang.has("entity.hemomancy." + id), id);
			assertTrue(lang.has("item.hemomancy.spawn_egg_" + id), id);
			assertTrue(ResourceReader.exists("assets/hemomancy/models/item/spawn_egg_" + id + ".json"), id);
			assertTrue(ResourceReader.exists("assets/hemomancy/textures/entity/facsimile/" + id + "_growth.png"), id);
		}
	}

	@Test
	void everyFacsimileBleedsFungal() {
		for (String id : IDS) {
			assertTrue(ResourceReader.text("data/hemomancy/blood_profiles/" + id + ".json").contains("hemomancy:fungal"), id);
		}
	}

	@Test
	void facsimilesLiveInTheDimensionAndNotTheOverworldBiome() {
		String overworld = ResourceReader.text("data/hemomancy/worldgen/biome/fungal_gardens.json");
		for (String id : IDS) {
			boolean spawns = DIMENSION_BIOMES.stream().anyMatch(biome -> ResourceReader
					.text("data/hemomancy/worldgen/biome/" + biome + ".json").contains("hemomancy:" + id));
			assertTrue(spawns, id + " spawns in no Gardens biome");
			assertFalse(overworld.contains("hemomancy:" + id), id + " leaked into the Overworld biome");
		}
	}
}
