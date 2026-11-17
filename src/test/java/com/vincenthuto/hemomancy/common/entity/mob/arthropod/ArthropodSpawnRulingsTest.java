package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArthropodSpawnRulingsTest {

	@Test
	void venomRibCentipedeHasTwentySixHealthAndKeepsWeightFour() {
		assertEquals(26.0D, VenomRibCentipedeRules.MAX_HEALTH, "CR-8");
		JsonObject spawn = ResourceReader.data("neoforge/biome_modifier/add_venom_rib_centipede.json")
				.getAsJsonObject("spawners");
		assertEquals(4, spawn.get("weight").getAsInt(), "CR-8 keeps the weight");
	}

	@Test
	void hemolymphopodaAmbientWeightIsTen() {
		JsonObject spawn = ResourceReader.data("neoforge/biome_modifier/add_hemolymphopoda.json")
				.getAsJsonArray("spawners").get(0).getAsJsonObject();
		assertEquals(10, spawn.get("weight").getAsInt(), "CR-9");
		assertEquals(2, spawn.get("minCount").getAsInt());
		assertEquals(5, spawn.get("maxCount").getAsInt());
	}
}
