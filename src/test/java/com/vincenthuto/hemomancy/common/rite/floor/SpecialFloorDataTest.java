package com.vincenthuto.hemomancy.common.rite.floor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** The special floors: one per special rite, each declaring the purpose of the rite performed on it. */
final class SpecialFloorDataTest {
	private static final Path DATA = Path.of("src/main/resources/data/hemomancy");
	private static final Map<String, String> SPECIAL_RITES = Map.of(
			"apotheos_rite", "apotheos_grand",
			"bloom_of_qliphoth", "qliphoth_grand",
			"ancestral_communion", "ancestral_grand");

	@Test
	void eachSpecialRiteHasItsOwnGrandFloorOfItsPurpose() throws IOException {
		Map<String, String> purposes = Map.of("apotheos_grand", "self", "qliphoth_grand", "world",
				"ancestral_grand", "bond");
		for (Map.Entry<String, String> entry : SPECIAL_RITES.entrySet()) {
			JsonObject rite = rite(entry.getKey());
			assertEquals("hemomancy:" + entry.getValue(), rite.get("floor").getAsString(), entry.getKey());
			assertTrue(rite.get("special").getAsBoolean(), entry.getKey() + " is not marked special");
			JsonObject floor = floors().get(entry.getValue());
			assertNotNull(floor, entry.getValue() + " is missing");
			assertEquals("grand", floor.get("tier").getAsString());
			assertEquals(purposes.get(entry.getValue()), floor.get("purpose").getAsString());
		}
	}

	@Test
	void everyRiteOnANonPurposeStyleIsSpecialAndSharesTheFloorsPurpose() throws IOException {
		Map<String, JsonObject> floors = floors();
		try (Stream<Path> files = Files.walk(DATA.resolve("recipe/cardinal_rite"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject rite = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (!rite.has("floor")) continue;
				String floorId = rite.get("floor").getAsString().replace("hemomancy:", "");
				JsonObject floor = floors.get(floorId);
				assertNotNull(floor, file + " names a missing floor " + floorId);
				if (CardinalRitePurpose.ofFloorStyle(floor.get("style").getAsString()) != null) continue;
				assertTrue(rite.has("special") && rite.get("special").getAsBoolean(), file + " is on a special floor");
				assertEquals(floor.get("purpose").getAsString(), rite.get("purpose").getAsString(),
						file + " does not share its special floor's purpose");
			}
		}
	}

	@Test
	void specialFloorsDeclareAPurposeAndStandardOnesMatchTheirStyle() throws IOException {
		for (Map.Entry<String, JsonObject> entry : floors().entrySet()) {
			JsonObject floor = entry.getValue();
			CardinalRitePurpose implied = CardinalRitePurpose.ofFloorStyle(floor.get("style").getAsString());
			if (implied == null) {
				assertTrue(floor.has("purpose"), entry.getKey() + " is special and must declare a purpose");
				assertNotNull(CardinalRitePurpose.byId(floor.get("purpose").getAsString()), entry.getKey());
			} else if (floor.has("purpose")) {
				assertEquals(implied.id(), floor.get("purpose").getAsString(), entry.getKey());
			}
		}
	}

	@Test
	void specialFloorsAreNineByNineWithOneCentredFocusAndNoCopper() throws IOException {
		for (String id : SPECIAL_RITES.values()) {
			JsonObject floor = floors().get(id);
			var rows = floor.getAsJsonArray("pattern");
			assertEquals(9, rows.size(), id);
			int focus = 0;
			for (int z = 0; z < rows.size(); z++) {
				String row = rows.get(z).getAsJsonArray().get(0).getAsString();
				assertEquals(9, row.length(), id + " row " + z);
				for (int x = 0; x < row.length(); x++) {
					if (block(floor, row.charAt(x)).equals("hemomancy:cardinal_focus")) {
						focus++;
						assertEquals(4, x, id + " focus column");
						assertEquals(4, z, id + " focus row");
					}
				}
			}
			assertEquals(1, focus, id + " focus count");
			assertFalse(floor.getAsJsonObject("key").toString().contains("copper"), id + " uses copper");
		}
	}

	@Test
	void noTwoGrandFloorsLayTheSameBlocks() throws IOException {
		Map<String, String> layouts = new HashMap<>();
		for (Map.Entry<String, JsonObject> entry : floors().entrySet()) {
			JsonObject floor = entry.getValue();
			if (!"grand".equals(floor.get("tier").getAsString())) continue;
			StringBuilder laid = new StringBuilder();
			for (JsonElement row : floor.getAsJsonArray("pattern")) {
				for (char symbol : row.getAsJsonArray().get(0).getAsString().toCharArray()) {
					laid.append(block(floor, symbol)).append(';');
				}
			}
			String previous = layouts.put(laid.toString(), entry.getKey());
			assertNull(previous, entry.getKey() + " lays the same blocks as " + previous);
		}
	}

	private static String block(JsonObject floor, char symbol) {
		JsonObject key = floor.getAsJsonObject("key").getAsJsonObject(String.valueOf(symbol));
		return key.has("tag") ? "#" + key.get("tag").getAsString() : key.get("block").getAsString();
	}

	private static JsonObject rite(String name) throws IOException {
		return JsonParser.parseString(Files.readString(DATA.resolve("recipe/cardinal_rite/" + name + ".json")))
				.getAsJsonObject();
	}

	private static Map<String, JsonObject> floors() throws IOException {
		Map<String, JsonObject> floors = new LinkedHashMap<>();
		try (Stream<Path> files = Files.list(DATA.resolve("cardinal_rite_floor"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				String name = file.getFileName().toString();
				floors.put(name.substring(0, name.length() - 5),
						JsonParser.parseString(Files.readString(file)).getAsJsonObject());
			}
		}
		return floors;
	}
}
