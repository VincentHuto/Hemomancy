package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 10: the meat table. Each file names items or a tag, never both; meat is worth more cooked than raw;
 * Hemomancy's own cooked meats are worth more than vanilla's, tainted meat the most per point; no item is named twice.
 */
class BloodFoodDataTest {
	private static double worth(String file) {
		return ResourceReader.data(BloodFoods.FOLDER + "/" + file + ".json").get("ml_per_nutrition").getAsDouble();
	}

	@Test
	void everyFileNamesItemsOrATagAndNoItemTwice() throws IOException {
		Path folder = RepoPaths.resolve("src/main/resources/data/hemomancy/" + BloodFoods.FOLDER);
		List<Path> files;
		try (Stream<Path> listed = Files.list(folder)) {
			files = listed.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
		assertTrue(files.size() >= 6, "the table covers raw and cooked meat and fish and the mod's meats");
		Map<String, String> owner = new HashMap<>();
		for (Path path : files) {
			String name = path.getFileName().toString().replace(".json", "");
			JsonObject file = ResourceReader.data(BloodFoods.FOLDER + "/" + name + ".json");
			boolean items = file.has("items") && file.getAsJsonArray("items").size() > 0;
			assertNotEquals(items, file.has("tag"), name + " names items or a tag");
			assertTrue(file.get("ml_per_nutrition").getAsDouble() > 0.0D, name + " is worth something");
			if (file.has("temper")) BloodFoods.school(file.get("temper").getAsString());
			if (items) {
				for (JsonElement item : file.getAsJsonArray("items")) {
					assertNull(owner.put(item.getAsString(), name), item + " is named twice");
				}
			}
		}
	}

	@Test
	void cookedIsWorthMoreAndTaintedMeatPaysTheMostPerPoint() {
		assertTrue(worth("cooked_meat") > worth("raw_meat"));
		// CB-3, NQ-02-8: raw fish 4, cooked fish 5, so cooking still pays.
		assertEquals(4.0D, worth("raw_fish"));
		assertEquals(5.0D, worth("cooked_fish"));
		assertTrue(worth("cooked_fish") > worth("raw_fish"));
		assertTrue(worth("venison") > worth("cooked_meat"), "the mod's cooked meat gains its job");
		// Tainted meat 10 a point still beats venison per point, though not per item (30 mL against a steak's 48).
		assertEquals(10.0D, worth("tainted_meat"));
		assertTrue(worth("tainted_meat") > worth("venison"));
		assertEquals(48.0D, BloodRegenRules.mealBlood(8, worth("cooked_meat")), "a steak is 48 mL");
	}
}
