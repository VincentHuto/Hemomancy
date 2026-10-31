package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Template validation for the two Seats (Track 7), on the NBT tools/worldgen/brined_court_seats.py writes: every pool
 * and jigsaw resolves, nothing forbidden is placed, every air pocket under the waterline is sealed from the sea, the
 * frames match {@link CourtSeatLayout}, the Sanctum leaves its chimney zone alone, and the residents are marked.
 */
class CourtSeatTemplateTest {
	/** Where each piece's waterline row sits; the Sanctum's is above the template (all of it is under the sea). */
	private record Piece(String site, String name, String pool, int waterline, boolean openTop) {
		String location() {
			return "hemomancy:brined_court/" + site + "/" + name;
		}
	}

	private static final List<Piece> PIECES = List.of(
			new Piece("lighthouse_garden", "garden", "start_pool", CourtSeatLayout.GARDEN_DEPTH, false),
			new Piece("lighthouse_garden", "undercroft", "undercroft", 1000, true),
			new Piece("vent_sanctum", "sanctum", "start_pool", 1000, false),
			new Piece("vent_sanctum", "body", "body", 1000, false));

	@Test
	void everyPoolNamesAnExistingTemplateAndIgnoresWaterlogging() {
		for (var piece : PIECES) {
			var pool = ResourceReader.data("worldgen/template_pool/brined_court/" + piece.site() + "/" + piece.pool() + ".json");
			var element = pool.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("element");
			assertEquals(piece.location(), element.get("location").getAsString());
			assertEquals("ignore_waterlogging", element.get("override_liquid_settings").getAsString(),
					"A Seat template carries its own water: " + piece);
			assertTrue(ResourceReader.exists("data/hemomancy/structure/brined_court/" + piece.site() + "/" + piece.name() + ".nbt"));
		}
		for (var site : List.of("lighthouse_garden", "vent_sanctum")) {
			var structure = ResourceReader.data("worldgen/structure/" + site + ".json");
			assertEquals("hemomancy:court_seat_start", structure.get("start_jigsaw_name").getAsString());
		}
	}

	@Test
	void everyJigsawConnectsToAPoolThatExists() throws Exception {
		for (var piece : PIECES) {
			var template = load(piece);
			for (var cell : template.cells().values()) {
				if (!cell.name().equals("minecraft:jigsaw")) continue;
				String pool = cell.nbt().getString("pool");
				if (pool.equals("minecraft:empty")) continue;
				String path = pool.substring("hemomancy:".length());
				assertTrue(ResourceReader.exists("data/hemomancy/worldgen/template_pool/" + path + ".json"),
						piece + " connects to a missing pool " + pool);
			}
		}
	}

	@Test
	void noTemplatePlacesTheCopperFamilyOrAFlame() throws Exception {
		for (var piece : PIECES) {
			for (var cell : load(piece).cells().values()) {
				String path = cell.name().substring(cell.name().indexOf(':') + 1);
				assertFalse(CourtTemplates.FORBIDDEN.stream().anyMatch(path::contains), piece + " places " + cell.name());
			}
		}
	}

	@Test
	void airPocketsUnderTheWaterlineCannotFlood() throws Exception {
		for (var piece : PIECES) {
			var leaks = load(piece).leaks(piece.waterline(), piece.openTop());
			assertTrue(leaks.isEmpty(), piece + " has air the sea can reach: " + leaks.subList(0, Math.min(10, leaks.size())));
		}
	}

	@Test
	void theFramesMatchTheLayout() throws Exception {
		var garden = load(PIECES.get(0));
		var start = garden.jigsaw("hemomancy:court_seat_start");
		assertEquals(CourtSeatLayout.GARDEN_DEPTH, start[1], "The Garden's start jigsaw is its waterline row");
		assertEquals(garden.size()[0] / 2, start[0]);
		assertEquals(garden.size()[2] / 2, start[2]);

		var sanctum = load(PIECES.get(2));
		start = sanctum.jigsaw("hemomancy:court_seat_start");
		assertEquals(CourtSeatLayout.SANCTUM_BELOW + CourtSeatLayout.SANCTUM_LIFT, start[1]);
		assertEquals(sanctum.size()[0] / 2, start[0]);
		assertEquals(sanctum.size()[2] / 2, start[2]);
		assertEquals(sanctum.size()[0], sanctum.size()[2], "The Sanctum is square, so its centre survives rotation");
		assertEquals(1, sanctum.size()[0] % 2);
	}

	@Test
	void theSanctumLeavesItsChimneyZoneToTheTerrain() throws Exception {
		var sanctum = load(PIECES.get(2));
		int c = sanctum.size()[0] / 2;
		for (var entry : sanctum.cells().entrySet()) {
			int[] p = CourtTemplates.unkey(entry.getKey());
			double r = Math.hypot(p[0] - c, p[2] - c);
			if (r < CourtSeatLayout.SANCTUM_CHIMNEY_ZONE) {
				assertTrue(entry.getValue().name().equals("minecraft:jigsaw") && p[1] > CourtSeatLayout.SANCTUM_BELOW + 10,
						"Only the start jigsaw, high above the heads, may stand in the chimney zone: " + entry.getValue());
			}
		}
	}

	@Test
	void residentsAndPresencesAreMarked() throws Exception {
		var names = new ArrayList<String>();
		for (var piece : PIECES) {
			for (var cell : load(piece).cells().values()) {
				if (cell.name().equals("minecraft:jigsaw")) names.add(cell.nbt().getString("name"));
			}
		}
		for (var role : List.of("lamp_keeper", "guest_host", "gardener", "arbor_anchored", "arbor_withdrawing",
				"undercroft_tender", "undercroft_anchored", "undercroft_anchored_deep", "kindled_elder", "kindled_preparer")) {
			assertTrue(names.contains(CourtResidentMarkers.RESIDENT + role), "No resident marker for " + role);
		}
		assertTrue(names.contains(CourtResidentMarkers.MARK + "marimo_presence"));
		assertTrue(names.contains(CourtResidentMarkers.MARK + "hydra_body"));
	}

	@Test
	void theUndercroftDescendsToPetrifiedFollowersAndMarimosPool() throws Exception {
		var undercroft = load(PIECES.get(1));
		long followers = undercroft.cells().values().stream().filter(c -> c.name().equals("hemomancy:petrified_follower")).count();
		long pool = undercroft.cells().values().stream().filter(c -> c.name().equals("minecraft:water")).count();
		assertTrue(followers >= 6, "The lowest walls are lined with petrified followers: " + followers);
		assertTrue(pool > 40, "Marimo's island stands in a pool: " + pool);
		var garden = load(PIECES.get(0));
		assertTrue(garden.cells().values().stream().anyMatch(c -> c.name().equals("hemomancy:sunburst_lamp")));
	}

	private static CourtTemplates.Template load(Piece piece) throws Exception {
		return CourtTemplates.load("brined_court/" + piece.site() + "/" + piece.name());
	}
}
