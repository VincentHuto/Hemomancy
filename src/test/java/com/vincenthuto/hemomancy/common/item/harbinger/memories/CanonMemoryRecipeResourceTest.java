package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Each Canon Memory is woven from its Saint's residuum and its Saint's temper pair, two enzymes of each and 300 blood. The registered
 * manipulation's pair is checked over the live registry by canonMemoriesTakeTheirSaintsPair.
 */
final class CanonMemoryRecipeResourceTest {
	private static final Map<EnumSaintType, String> CANON = Map.of(
			EnumSaintType.HEMORATH, "crimson_tithe",
			EnumSaintType.SERAPHAE, "unclosing_eye",
			EnumSaintType.PUTRICIEL, "bloom_of_rot",
			EnumSaintType.VELORUM, "endless_hour");

	@Test
	void everyCanonMemoryTakesItsSaintsResiduumAndPair() {
		CANON.forEach((saint, memory) -> {
			String saintId = saint.name().toLowerCase(Locale.ROOT);
			JsonObject recipe = ResourceReader.data("recipe/memory_weaving/memory_" + memory + ".json");

			assertEquals("hemomancy:memory_" + memory, recipe.get("result").getAsString(), memory);
			assertEquals("hemomancy:hallowed_residuum_" + saintId,
					recipe.getAsJsonArray("catalysts").get(0).getAsJsonObject().get("item").getAsString(),
					memory + " is woven from " + saintId + "'s residuum");
			assertEquals("hemomancy:recollection_" + saintId,
					recipe.getAsJsonArray("catalysts").get(1).getAsJsonObject().get("item").getAsString(),
					memory + " also needs the Gardens' Recollection of " + saintId);

			Set<String> enzymes = new TreeSet<>();
			recipe.getAsJsonObject("enzymes").entrySet().forEach(entry -> {
				if (entry.getValue().getAsInt() > 0) enzymes.add(entry.getKey());
			});
			Set<String> pair = new TreeSet<>(Set.of(
					saint.getPrimaryTemper().name().toLowerCase(Locale.ROOT),
					saint.getSecondaryTemper().name().toLowerCase(Locale.ROOT)));
			assertEquals(pair, enzymes, memory + " takes " + saintId + "'s temper pair");
			assertEquals(8, recipe.get("required_degree").getAsInt(), memory + " weaves only at D8 (C-6)");
			recipe.getAsJsonObject("enzymes").entrySet().forEach(entry -> {
				if (entry.getValue().getAsInt() > 0)
					assertEquals(2, entry.getValue().getAsInt(), memory + " takes two of each " + entry.getKey() + " (NQ-08-1 A)");
			});
			assertEquals(300, recipe.get("blood").getAsInt(), memory + " costs 300 blood to weave (NQ-08-1 A)");
		});
	}

	@Test
	void theTreasureBarrelHoldsNoCanonMemory() {
		String table = ResourceReader.data("loot_table/chests/mausoleum_treasure.json").toString();
		CANON.values().forEach(memory -> assertFalse(table.contains("hemomancy:memory_" + memory),
				"mausoleum_treasure drops no " + memory + " directly (C-6)"));
	}
}
