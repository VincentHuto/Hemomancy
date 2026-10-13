package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Lore and layout rules every Harbinger rite floor must keep. */
final class CardinalRiteFloorDesignDataTest {
	private static final Path FLOOR_ROOT =
			Path.of("src/main/resources/data/hemomancy/cardinal_rite_floor");
	private static final List<String> STYLES = List.of("threshold", "dominion", "communion", "working");
	private static final List<String> TIERS = List.of("minor", "lesser", "greater", "grand");

	@Test
	void harbingerFloorsNeverUseCopper() throws IOException {
		for (String style : STYLES) {
			for (String tier : TIERS) {
				String key = floor(style, tier).getAsJsonObject("key").toString();
				assertFalse(key.contains("copper"), style + "_" + tier + " uses Unstained copper: " + key);
			}
		}
	}

	@Test
	void ceremonyAnchorsOnlyLandOnAxisAndDiagonalLanes() {
		for (int ring = 0; ring < CardinalRiteRingTuning.RADIUS_BLOCKS.length; ring++) {
			for (int order = 0; order < 4; order++) {
				var anchor = CardinalRiteRingTuning.anchor(ring, order, 1);
				assertTrue(onAnchorLane(anchor.x(), anchor.z()),
						"ring " + ring + " order " + order + " anchor left the lanes: " + anchor);
			}
		}
	}

	@Test
	void socketsFlankTheAnchorLanesOutsideTheFloorWithFullSymmetry() throws IOException {
		for (String style : STYLES) {
			for (String tier : TIERS) {
				JsonObject floor = floor(style, tier);
				int half = floor.getAsJsonArray("pattern").size() / 2;
				float footprint = floor.get("footprint_radius").getAsFloat();
				Set<Long> sockets = sockets(floor);
				for (long packed : sockets) {
					int x = x(packed);
					int z = z(packed);
					String name = style + "_" + tier + " socket " + x + "," + z;
					assertFalse(onAnchorLane(x, z), name + " sits where a ceremony anchor can be planted");
					assertTrue(Math.max(Math.abs(x), Math.abs(z)) > half, name + " is on the floor");
					assertTrue(Math.hypot(x, z) < footprint, name + " is outside the rite footprint");
					for (long image : List.of(pack(-x, z), pack(x, -z), pack(z, x), pack(-z, -x))) {
						assertTrue(sockets.contains(image), name + " has no mirror at " + x(image) + "," + z(image));
					}
				}
			}
		}
	}

	@Test
	void upgradingAFloorSlidesEachSocketOneBlockOutward() throws IOException {
		for (String style : STYLES) {
			for (int tier = 0; tier < TIERS.size() - 1; tier++) {
				Set<Long> larger = sockets(floor(style, TIERS.get(tier + 1)));
				for (long packed : sockets(floor(style, TIERS.get(tier)))) {
					int x = x(packed);
					int z = z(packed);
					long slid = Math.abs(x) > Math.abs(z) ? pack(x + Integer.signum(x), z) : pack(x, z + Integer.signum(z));
					assertTrue(larger.contains(slid), style + " " + TIERS.get(tier) + " socket " + x + "," + z
							+ " has no outward successor on the " + TIERS.get(tier + 1) + " floor");
				}
			}
		}
	}

	@Test
	void everyTierCentreRepeatsTheTierBelow() throws IOException {
		for (String style : STYLES) {
			for (int tier = 0; tier < TIERS.size() - 1; tier++) {
				JsonObject smaller = floor(style, TIERS.get(tier));
				JsonObject larger = floor(style, TIERS.get(tier + 1));
				int inset = (larger.getAsJsonArray("pattern").size() - smaller.getAsJsonArray("pattern").size()) / 2;
				var rows = smaller.getAsJsonArray("pattern");
				for (int z = 0; z < rows.size(); z++) {
					String row = rows.get(z).getAsJsonArray().get(0).getAsString();
					String outer = larger.getAsJsonArray("pattern").get(z + inset).getAsJsonArray().get(0).getAsString();
					for (int x = 0; x < row.length(); x++) {
						assertEquals(block(smaller, row.charAt(x)), block(larger, outer.charAt(x + inset)),
								style + " " + TIERS.get(tier + 1) + " centre differs from " + TIERS.get(tier)
										+ " at " + x + "," + z);
					}
				}
			}
		}
	}

	@Test
	void ironAccentedStylesCostTheSameIronPerTier() throws IOException {
		for (String tier : TIERS) {
			assertEquals(accents(floor("threshold", tier)), accents(floor("dominion", tier)),
					"dominion " + tier + " should cost the same hematic iron as threshold " + tier);
		}
	}

	private static boolean onAnchorLane(int x, int z) {
		return x == 0 || z == 0 || Math.abs(x) == Math.abs(z);
	}

	private static int accents(JsonObject floor) {
		int count = 0;
		for (JsonElement row : floor.getAsJsonArray("pattern")) {
			for (char symbol : row.getAsJsonArray().get(0).getAsString().toCharArray()) {
				if (block(floor, symbol).contains("hematic_iron")) count++;
			}
		}
		return count;
	}

	private static String block(JsonObject floor, char symbol) {
		JsonObject key = floor.getAsJsonObject("key").getAsJsonObject(String.valueOf(symbol));
		return key.has("tag") ? "#" + key.get("tag").getAsString() : key.get("block").getAsString();
	}

	private static Set<Long> sockets(JsonObject floor) {
		Set<Long> sockets = new HashSet<>();
		for (JsonElement socket : floor.getAsJsonArray("brazier_sockets")) {
			var values = socket.getAsJsonArray();
			assertTrue(sockets.add(pack(values.get(0).getAsInt(), values.get(2).getAsInt())), "duplicate socket");
		}
		return sockets;
	}

	private static long pack(int x, int z) {
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}

	private static int x(long packed) {
		return (int) (packed >> 32);
	}

	private static int z(long packed) {
		return (int) packed;
	}

	private static JsonObject floor(String style, String tier) throws IOException {
		return JsonParser.parseString(Files.readString(FLOOR_ROOT.resolve(style + "_" + tier + ".json")))
				.getAsJsonObject();
	}
}
