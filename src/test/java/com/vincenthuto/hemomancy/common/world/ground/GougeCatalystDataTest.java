package com.vincenthuto.hemomancy.common.world.ground;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 6: the gouge catalyst table, one file per function, read through Track 4's Temper catalyst table.
 * Every function but the bare glow has a file named for it; the track's table holds (glowstone and the incandescent
 * enzyme give light, coal and the fervent enzyme heat, a compass bearing, packed ice and the frigid enzyme cold, iron
 * and the ferric enzyme pull, bone meal and the vivacious enzyme feeding, ink and the umbral enzyme dark); no item
 * gives two functions.
 */
class GougeCatalystDataTest {
	private static final Map<String, String> SCHOOL = Map.of(
			"light", "lux", "hot", "flammeus", "cold", "congeatio", "pull", "ferric", "feed", "animus", "dark", "tenebris");

	private static JsonObject file(GroundFunction function) {
		return ResourceReader.data(GougeCatalysts.FOLDER + "/" + function.getSerializedName() + ".json");
	}

	/** Every item a function's file gives it: the named items and its school's Temper catalysts. */
	private static Set<String> items(GroundFunction function) {
		JsonObject file = file(function);
		Set<String> items = new HashSet<>();
		if (file.has("items")) {
			for (JsonElement element : file.getAsJsonArray("items")) {
				JsonObject entry = element.getAsJsonObject();
				assertTrue(entry.get("ticks").getAsInt() > 0, "a catalyst lights a run for some time");
				items.add(entry.get("item").getAsString());
			}
		}
		if (file.has("temper")) {
			assertTrue(file.get("ticks_per_worth").getAsInt() > 0, function + " names a school but no time per worth");
			String school = file.get("temper").getAsString().replace("hemomancy:", "");
			for (JsonElement element : ResourceReader.data("temper_catalyst/" + school + ".json").getAsJsonArray("catalysts")) {
				items.add(element.getAsJsonObject().get("item").getAsString());
			}
		}
		return items;
	}

	@Test
	void everyFunctionButTheBareGlowHasItsFile() {
		assertFalse(ResourceReader.exists("data/hemomancy/" + GougeCatalysts.FOLDER + "/none.json"), "no catalyst is the bare glow");
		for (GroundFunction function : GroundFunction.values()) {
			if (function == GroundFunction.NONE) continue;
			JsonObject file = file(function);
			assertEquals(function.getSerializedName(), file.get("function").getAsString(), "a file is named for its function");
			String school = SCHOOL.get(function.getSerializedName());
			if (school != null) assertEquals("hemomancy:" + school, file.get("temper").getAsString(), function + " reads its school");
		}
	}

	@Test
	void theTracksTableHolds() {
		assertTrue(items(GroundFunction.LIGHT).containsAll(Set.of("minecraft:glowstone_dust", "hemomancy:incandescent_enzyme",
				"hemomancy:organic_prism")));
		assertTrue(items(GroundFunction.HOT).containsAll(Set.of("minecraft:coal", "hemomancy:fervent_enzyme")));
		assertTrue(items(GroundFunction.POINT).contains("minecraft:compass"), "a compass, lodestone or not, gives a bearing");
		assertTrue(items(GroundFunction.COLD).containsAll(Set.of("minecraft:packed_ice", "hemomancy:frigid_enzyme")));
		assertTrue(items(GroundFunction.PULL).containsAll(Set.of("minecraft:iron_ingot", "hemomancy:ferric_enzyme")));
		assertTrue(items(GroundFunction.FEED).containsAll(Set.of("minecraft:bone_meal", "hemomancy:vivacious_enzyme")));
		assertTrue(items(GroundFunction.DARK).containsAll(Set.of("minecraft:ink_sac", "hemomancy:umbral_enzyme")));
	}

	@Test
	void noItemGivesTwoFunctions() {
		Map<String, GroundFunction> owner = new HashMap<>();
		for (GroundFunction function : GroundFunction.values()) {
			if (function == GroundFunction.NONE) continue;
			for (String item : items(function)) {
				GroundFunction before = owner.put(item, function);
				assertNull(before, item + " gives both " + before + " and " + function);
			}
		}
	}
}
