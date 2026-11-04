package com.vincenthuto.hemomancy.common.block;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A block a player makes through a Working craft, a Blood Structure or a recipe drops itself when broken. Eight such
 * blocks had no loot table and were destroyed on pickup (cleanup audit PG-6 / A-15); so did the wild Lethean Poppy
 * and Hyphae (EC-2, EC-11 / A-16).
 */
class PlayerMadeBlockLootResourceTest {
	private static final List<String> SELF_DROPPING = List.of("covenant_throne", "mycelial_lantern", "scar_station",
			"fungal_implantation_pylon", "sanguine_vigil", "hematic_stake",
			"lethean_poppy");

	@Test
	void playerMadeBlocksDropThemselves() {
		for (String block : SELF_DROPPING) {
			JsonObject loot = ResourceReader.data("loot_table/blocks/" + block + ".json");
			assertEquals("minecraft:block", loot.get("type").getAsString(), block);
			assertTrue(loot.toString().contains("\"hemomancy:" + block + "\""), block + " drops itself");
		}
	}

	@Test
	void hyphaeAreGatheredWithShears() {
		String loot = ResourceReader.data("loot_table/blocks/hyphae.json").toString();
		assertTrue(loot.contains("\"hemomancy:hyphae\""), "hyphae drop themselves");
		assertTrue(loot.contains("minecraft:shears"), "hyphae need shears");
	}
}
