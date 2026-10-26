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

/**
 * Each Canon Memory is woven from its Saint's residuum and its Saint's tendency pair. The registered
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

			Set<String> enzymes = new TreeSet<>();
			recipe.getAsJsonObject("enzymes").entrySet().forEach(entry -> {
				if (entry.getValue().getAsInt() > 0) enzymes.add(entry.getKey());
			});
			Set<String> pair = new TreeSet<>(Set.of(
					saint.getPrimaryTendency().name().toLowerCase(Locale.ROOT),
					saint.getSecondaryTendency().name().toLowerCase(Locale.ROOT)));
			assertEquals(pair, enzymes, memory + " takes " + saintId + "'s tendency pair");
		});
	}
}
