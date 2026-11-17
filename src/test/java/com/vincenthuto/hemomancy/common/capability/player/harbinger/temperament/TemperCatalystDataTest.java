package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core loop Track 4: the catalyst table, one file per school. Every school lists its enzyme at 10 and its spores at 5,
 * and at least one vanilla or rendering reagent at 3; coal and charcoal feed Flammeus; no item feeds two schools.
 */
class TemperCatalystDataTest {
	private static final Map<EnumBloodTemper, String> ENZYME_PREFIX = Map.of(
			EnumBloodTemper.ANIMUS, "vivacious",
			EnumBloodTemper.FLAMMEUS, "fervent",
			EnumBloodTemper.DUCTILIS, "neurotic",
			EnumBloodTemper.LUX, "incandescent",
			EnumBloodTemper.MORTEM, "ruinous",
			EnumBloodTemper.CONGEATIO, "frigid",
			EnumBloodTemper.FERRIC, "ferric",
			EnumBloodTemper.TENEBRIS, "umbral");

	@Test
	void everySchoolHasItsEnzymeSporesAndAReagent() {
		Map<String, EnumBloodTemper> owner = new HashMap<>();
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			String name = school.name().toLowerCase(Locale.ROOT);
			JsonObject file = ResourceReader.data(TemperCatalysts.FOLDER + "/" + name + ".json");
			assertEquals(name, file.get("temper").getAsString(), "a catalyst file is named for its school");
			Map<String, Float> worth = new HashMap<>();
			for (JsonElement element : file.getAsJsonArray("catalysts")) {
				JsonObject entry = element.getAsJsonObject();
				String item = entry.get("item").getAsString();
				float value = entry.get("worth").getAsFloat();
				assertTrue(value > 0.0F, item + " is worth something");
				worth.put(item, value);
				EnumBloodTemper previous = owner.put(item, school);
				assertNull(previous, item + " feeds both " + previous + " and " + school);
			}
			String prefix = ENZYME_PREFIX.get(school);
			assertEquals(10.0F, worth.get("hemomancy:" + prefix + "_enzyme"), 1.0E-6F, name + " enzyme is worth 10");
			assertEquals(5.0F, worth.get("hemomancy:" + prefix + "_spores"), 1.0E-6F, name + " spores are worth 5");
			assertTrue(worth.values().stream().anyMatch(value -> Math.abs(value - 3.0F) < 1.0E-6F),
					name + " has a reagent at 3");
		}
		assertEquals(EnumBloodTemper.FLAMMEUS, owner.get("minecraft:coal"), "coal feeds Fervent");
		assertEquals(EnumBloodTemper.FLAMMEUS, owner.get("minecraft:charcoal"), "charcoal feeds Fervent");
		assertEquals(EnumBloodTemper.FLAMMEUS, owner.get("minecraft:blaze_powder"));
	}

	/** LP-15: coal and charcoal are worth 1 Flammeus Temper each. */
	@Test
	void coalAndCharcoalAreWorthOne() {
		JsonObject file = ResourceReader.data(TemperCatalysts.FOLDER + "/flammeus.json");
		Map<String, Float> worth = new HashMap<>();
		for (JsonElement element : file.getAsJsonArray("catalysts"))
			worth.put(element.getAsJsonObject().get("item").getAsString(), element.getAsJsonObject().get("worth").getAsFloat());
		assertEquals(1.0F, worth.get("minecraft:coal"), 1.0E-6F);
		assertEquals(1.0F, worth.get("minecraft:charcoal"), 1.0E-6F);
	}

	@Test
	void theSchoolIdParsesWithOrWithoutANamespace() {
		assertEquals(EnumBloodTemper.LUX, TemperCatalysts.school("lux"));
		assertEquals(EnumBloodTemper.LUX, TemperCatalysts.school("hemomancy:lux"));
	}
}
