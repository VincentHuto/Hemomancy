package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FerventChitiniteLootResourceTest {

	private static final Path LOOT_TABLE = Path.of(
			"src/main/resources/data/hemomancy/loot_table/entities/fervent_chitinite.json");

	@Test
	void alwaysDropsBloodCrystalAndTwoToFourFerventHusks() throws Exception {
		JsonArray pools = JsonParser.parseString(Files.readString(LOOT_TABLE)).getAsJsonObject().getAsJsonArray("pools");
		assertEquals(2, pools.size());

		JsonObject crystal = pools.get(0).getAsJsonObject();
		assertFalse(crystal.has("conditions"));
		assertEquals("hemomancy:blood_crystal", itemName(crystal));

		JsonObject husks = pools.get(1).getAsJsonObject();
		assertFalse(husks.has("conditions"));
		assertEquals("hemomancy:fervent_husk", itemName(husks));
		JsonObject count = husks.getAsJsonArray("entries").get(0).getAsJsonObject()
				.getAsJsonArray("functions").get(0).getAsJsonObject().getAsJsonObject("count");
		assertEquals("minecraft:uniform", count.get("type").getAsString());
		assertEquals(2.0D, count.get("min").getAsDouble());
		assertEquals(4.0D, count.get("max").getAsDouble());
	}

	private static String itemName(JsonObject pool) {
		return pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString();
	}
}
