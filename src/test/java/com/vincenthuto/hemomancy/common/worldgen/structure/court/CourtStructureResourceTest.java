package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pins the Court's structure sets, the eight site structures and the {@code hemomancy:brined_court} tag. */
class CourtStructureResourceTest {
	@Test
	void everySiteHasAStructureInItsOwnHousesBiome() {
		for (var site : CourtSite.values()) {
			var structure = ResourceReader.data("worldgen/structure/" + site.id() + ".json");
			assertEquals("hemomancy:brined_court", structure.get("type").getAsString());
			assertEquals(site.id(), structure.get("site").getAsString());
			assertEquals("hemomancy:brined_court/" + site.id() + "/start_pool", structure.get("start_pool").getAsString());
			String tag = "#hemomancy:has_structure/brined_court_" + site.id();
			assertEquals(tag, structure.get("biomes").getAsString());
			var biomes = ResourceReader.data("tags/worldgen/biome/has_structure/brined_court_" + site.id() + ".json")
					.getAsJsonArray("values");
			// The Wake nursery also stands on the upper Twilight slope (review fixes 2026-10-30).
			var expected = site == CourtSite.WAKE_NURSERY
					? List.of("hemomancy:" + site.layer().id, "hemomancy:twilight_ocean")
					: List.of("hemomancy:" + site.layer().id);
			assertEquals(expected, strings(biomes));
			assertTrue(ResourceReader.exists("data/hemomancy/worldgen/template_pool/brined_court/" + site.id() + "/start_pool.json"));
		}
	}

	@Test
	void seatsAndOutpostsUseTheSettledSpreads() {
		var seats = ResourceReader.data("worldgen/structure_set/brined_court_seats.json");
		var placement = seats.getAsJsonObject("placement");
		assertEquals(64, placement.get("spacing").getAsInt());
		assertEquals(24, placement.get("separation").getAsInt());
		assertEquals(1029834751, placement.get("salt").getAsInt());
		assertEquals(sites(CourtSite.Tier.SEAT), structures(seats.getAsJsonArray("structures")));

		var outposts = ResourceReader.data("worldgen/structure_set/brined_court_outposts.json");
		placement = outposts.getAsJsonObject("placement");
		assertEquals(28, placement.get("spacing").getAsInt());
		assertEquals(10, placement.get("separation").getAsInt());
		assertEquals(1029834763, placement.get("salt").getAsInt());
		var exclusion = placement.getAsJsonObject("exclusion_zone");
		assertEquals("hemomancy:brined_court_seats", exclusion.get("other_set").getAsString());
		assertEquals(CourtPlacementRules.LANDMARK_RADIUS / 16, exclusion.get("chunk_count").getAsInt());
		assertEquals(sites(CourtSite.Tier.OUTPOST), structures(outposts.getAsJsonArray("structures")));
	}

	@Test
	void theCourtTagNamesEverySite() {
		var values = ResourceReader.data("tags/worldgen/structure/brined_court.json").getAsJsonArray("values");
		assertEquals(Arrays.stream(CourtSite.values()).map(site -> "hemomancy:" + site.id()).sorted().toList(),
				strings(values).stream().sorted().toList());
	}

	@Test
	void theMidnightHousesShareOneLayer() {
		assertEquals(PelagicLayer.MIDNIGHT, CourtSite.SCREENED_CLEFT.layer());
		assertEquals(PelagicLayer.MIDNIGHT, CourtSite.STILL_BELL.layer());
		assertEquals(CourtSite.SurfaceClimate.TEMPERATE_OR_WARM, CourtSite.SCREENED_CLEFT.climate());
		assertEquals(CourtSite.SurfaceClimate.COLD_OR_FROZEN, CourtSite.STILL_BELL.climate());
	}

	private static List<String> sites(CourtSite.Tier tier) {
		return Arrays.stream(CourtSite.values()).filter(site -> site.tier() == tier)
				.map(site -> "hemomancy:" + site.id()).sorted().toList();
	}

	private static List<String> structures(Iterable<JsonElement> entries) {
		var result = new ArrayList<String>();
		for (var entry : entries) result.add(entry.getAsJsonObject().get("structure").getAsString());
		result.sort(null);
		return result;
	}

	private static List<String> strings(Iterable<JsonElement> values) {
		var result = new ArrayList<String>();
		for (var value : values) result.add(value.getAsString());
		return result;
	}
}
