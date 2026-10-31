package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Template validation for the six inner-house outposts (Track 8), on the NBT tools/worldgen/brined_court_outposts.py
 * writes: every pool resolves and ignores waterlogging, nothing forbidden is placed, the Bell's air pocket is sealed
 * from the sea, the frames match {@link CourtSeatLayout}, each outpost names its house at its edge, both Line Camp
 * variants are there and differ, and every resident spot Track 9 needs is marked.
 */
class CourtOutpostTemplateTest {
	private static final String START = "hemomancy:court_outpost_start";
	/** Each outpost's templates (the Line Camp's two variants), its house and the resident roles it must mark. */
	private static final Map<CourtSite, List<String>> PIECES = Map.of(
			CourtSite.TIDE_REFUGE, List.of("refuge"),
			CourtSite.WAKE_NURSERY, List.of("nursery"),
			CourtSite.LINE_CAMP, List.of("joined", "parted"),
			CourtSite.SCREENED_CLEFT, List.of("cleft"),
			CourtSite.STILL_BELL, List.of("bell"),
			CourtSite.WHALE_FALL, List.of("fall"));
	private static final Map<CourtSite, List<String>> ROLES = Map.of(
			CourtSite.TIDE_REFUGE, List.of("refuge_keeper", "holdfast_braced"),
			CourtSite.WAKE_NURSERY, List.of("wake_tender"),
			CourtSite.LINE_CAMP, List.of("line_tender", "pod_keeper"),
			CourtSite.SCREENED_CLEFT, List.of("cleft_host", "cleft_veiled"),
			CourtSite.STILL_BELL, List.of("bell_keeper", "bell_sleeper"),
			CourtSite.WHALE_FALL, List.of("marrow_carer", "marrow_rooted"));

	@Test
	void everyOutpostPoolNamesItsTemplatesAndIgnoresWaterlogging() {
		for (var entry : PIECES.entrySet()) {
			var site = entry.getKey();
			var pool = ResourceReader.data("worldgen/template_pool/brined_court/" + site.id() + "/start_pool.json");
			var elements = pool.getAsJsonArray("elements");
			assertEquals(entry.getValue().size(), elements.size(), site + " pool elements");
			var locations = new HashSet<String>();
			for (var element : elements) {
				var e = element.getAsJsonObject().getAsJsonObject("element");
				assertEquals("ignore_waterlogging", e.get("override_liquid_settings").getAsString(),
						"An outpost template carries its own water: " + site);
				locations.add(e.get("location").getAsString());
			}
			for (var piece : entry.getValue()) {
				assertTrue(locations.contains("hemomancy:brined_court/" + site.id() + "/" + piece), site + " misses " + piece);
				assertTrue(ResourceReader.exists("data/hemomancy/structure/brined_court/" + site.id() + "/" + piece + ".nbt"));
			}
			var structure = ResourceReader.data("worldgen/structure/" + site.id() + ".json");
			assertEquals(START, structure.get("start_jigsaw_name").getAsString(), site.id());
		}
		assertFalse(ResourceReader.exists("data/hemomancy/structure/brined_court/stub_marker.nbt"),
				"Track 6's stub marker is gone once every outpost has its template");
	}

	@Test
	void noOutpostPlacesTheCopperFamilyOrAFlame() throws Exception {
		for (var site : PIECES.keySet()) {
			for (var piece : PIECES.get(site)) {
				for (var cell : load(site, piece).cells().values()) {
					String path = cell.name().substring(cell.name().indexOf(':') + 1);
					assertFalse(CourtTemplates.FORBIDDEN.stream().anyMatch(path::contains), site + "/" + piece + " places " + cell.name());
				}
			}
		}
	}

	@Test
	void theFramesMatchTheLayout() throws Exception {
		for (var site : PIECES.keySet()) {
			for (var piece : PIECES.get(site)) {
				var template = load(site, piece);
				int[] size = template.size();
				assertEquals(size[0], size[2], site + " is square, so its centre survives rotation");
				assertEquals(1, size[0] % 2, site + " has a centre column");
				assertTrue(size[0] < 32, site + " stays under 32 x 32");
				int[] start = template.jigsaw(START);
				assertEquals(size[0] / 2, start[0]);
				assertEquals(size[2] / 2, start[2]);
				assertEquals(CourtSeatLayout.outpostBelow(site), start[1], site + " marker row");
			}
		}
	}

	@Test
	void theBellsAirPocketIsSealedAndTheRefugeKeepsItsAirAboveTheTide() throws Exception {
		for (var site : PIECES.keySet()) {
			for (var piece : PIECES.get(site)) {
				var template = load(site, piece);
				int waterline = site == CourtSite.TIDE_REFUGE ? CourtSeatLayout.REFUGE_DEPTH : 1000;
				var leaks = template.leaks(waterline, false);
				assertTrue(leaks.isEmpty(), site + "/" + piece + " has air the sea can reach: "
						+ leaks.subList(0, Math.min(10, leaks.size())));
			}
		}
		var bell = load(CourtSite.STILL_BELL, "bell");
		assertTrue(bell.count("minecraft:air") > 150, "The Bell holds a real air pocket: " + bell.count("minecraft:air"));
		assertTrue(bell.count("hemomancy:bell_sleeper") >= 4, "Sealed sleepers hang in the upper ring");
		assertTrue(bell.count("hemomancy:sclerotium_shell") > 200, "The bell is grown sclerotium");
	}

	@Test
	void everyOutpostNamesItsHouseAtItsEdge() throws Exception {
		for (var site : PIECES.keySet()) {
			for (var piece : PIECES.get(site)) {
				long markers = load(site, piece).cells().values().stream()
						.filter(c -> c.name().equals("hemomancy:house_marker") && site.house().equals(c.props().get("house")))
						.count();
				assertEquals(1, markers, site + "/" + piece + " house markers for " + site.house());
			}
		}
	}

	@Test
	void theLineCampHasAJoinedAndAPartedVariant() throws Exception {
		var joined = load(CourtSite.LINE_CAMP, "joined");
		var parted = load(CourtSite.LINE_CAMP, "parted");
		assertNotEquals(joined.cells(), parted.cells());
		for (var camp : List.of(joined, parted)) {
			assertTrue(camp.count("hemomancy:current_strand") > 30, "Lines strung across the gully");
			assertTrue(camp.count("hemomancy:current_tissue") > 40, "Two grown pods");
			assertTrue(camp.count("hemomancy:rope_pod") >= 4, "Lamp pods light the camp");
		}
	}

	@Test
	void residentsAreMarkedForTrackNine() throws Exception {
		for (var site : PIECES.keySet()) {
			var names = new ArrayList<String>();
			for (var piece : PIECES.get(site)) {
				for (var cell : load(site, piece).cells().values()) {
					if (cell.name().equals("minecraft:jigsaw")) names.add(cell.nbt().getString("name"));
				}
			}
			for (var role : ROLES.get(site)) {
				assertTrue(names.contains(CourtResidentMarkers.RESIDENT + role), site + " has no resident marker for " + role);
			}
		}
	}

	private static CourtTemplates.Template load(CourtSite site, String piece) throws Exception {
		return CourtTemplates.load("brined_court/" + site.id() + "/" + piece);
	}
}
