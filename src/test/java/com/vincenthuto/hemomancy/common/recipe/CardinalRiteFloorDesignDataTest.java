package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteGashRules;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
	void floorsAuthorNoSocketsBecauseBraziersStandAnywhereAlongTheGash() throws IOException {
		for (String style : STYLES) {
			for (String tier : TIERS) {
				assertFalse(floor(style, tier).has("brazier_sockets"),
						style + "_" + tier + " still pins braziers to fixed sockets; the gash decides where they stand");
			}
		}
	}

	@Test
	void everyFloorsGashHoldsEnoughBraziersForTheRitesItAccepts() throws IOException {
		Path rites = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");
		java.util.Map<String, Integer> needed = new java.util.HashMap<>();
		try (var paths = Files.walk(rites)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject rite = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				if (!rite.has("floor") || !rite.has("brazier_signature")) continue;
				int count = 0;
				for (JsonElement requirement : rite.getAsJsonArray("brazier_signature")) {
					JsonObject entry = requirement.getAsJsonObject();
					count += entry.has("count") ? entry.get("count").getAsInt() : 1;
				}
				needed.merge(rite.get("floor").getAsString().replace("hemomancy:", ""), count, Math::max);
			}
		}
		for (String style : STYLES) {
			int most = 0;
			for (String tier : TIERS) {
				// A larger floor of a style also accepts the smaller floors' rites.
				most = Math.max(most, needed.getOrDefault(style + "_" + tier, 0));
				int half = floor(style, tier).getAsJsonArray("pattern").size() / 2;
				assertTrue(CardinalRiteGashRules.cells(half).size() >= most,
						style + "_" + tier + "'s gash takes fewer braziers than its rites need: " + most);
			}
		}
	}

	@Test
	void theGashFillsFromTheBackSoCraftsAndStationsStayClearOfTheBed() {
		// The spawner and the order of play fill gash cells in order; a craft stands on the bed south of the Focus.
		// The first half of each Working floor's gash (six cells at least where it has them) must lie outside that bed.
		int[][] beds = {{3, 1}, {3, 2}, {3, 3}, {5, 4}};
		for (int tier = 0; tier < TIERS.size(); tier++) {
			List<net.minecraft.core.BlockPos> cells = CardinalRiteGashRules.cells(tier + 1);
			int width = beds[tier][0];
			int depth = beds[tier][1];
			for (int i = 0; i < Math.min(6, cells.size() / 2); i++) {
				int worldX = cells.get(i).getX();
				int worldZ = -cells.get(i).getZ();
				assertFalse(Math.abs(worldX) <= width / 2 && worldZ >= 1 && worldZ <= depth,
						"working_" + TIERS.get(tier) + " gash cell " + i + " stands in the south craft bed");
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

	private static JsonObject floor(String style, String tier) throws IOException {
		return JsonParser.parseString(Files.readString(FLOOR_ROOT.resolve(style + "_" + tier + ".json")))
				.getAsJsonObject();
	}
}
