package com.vincenthuto.hemomancy.common.data.book;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Liber's Saint pages and title cards follow the reconciled Saint canon (tendencies, titles, endings). */
class SaintLiberPagesTest {
	private static final String PAGES = "books/libersanguinium/cosmic_forces/pages/";

	private record Saint(String id, String title, String primary, String secondary, String memory) {
	}

	private static final List<Saint> SAINTS = List.of(
			new Saint("hemorath", "The First Saint", "Animus", "Ferric", "Crimson Tithe"),
			new Saint("seraphae", "The Chain Saint", "Lux", "Ductilis", "Unclosing Eye"),
			new Saint("putriciel", "The Befouled Saint", "Mortem", "Flammeus", "Bloom of Rot"),
			new Saint("velorum", "Saint of the Endless Hour", "Congeatio", "Tenebris", "Endless Hour"));

	@Test
	void eachSaintHasAPageWithItsTitleAndTendencies() {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		for (Saint saint : SAINTS) {
			JsonObject page = ResourceReader.data(PAGES + saint.id() + ".json");
			String text = page.get("text").getAsString();
			assertEquals("hutoslib:page", page.get("type").getAsString(), saint.id());
			assertEquals(saint.title(), page.get("subtitle").getAsString(), saint.id());
			assertEquals(saint.title(), lang.get("boss.hemomancy." + saint.id() + ".epithet").getAsString(), saint.id());
			assertTrue(text.contains(saint.primary()) && text.contains(saint.secondary()),
					saint.id() + " names its tendency pair");
			assertFalse(text.toLowerCase(Locale.ROOT).contains("archon"), saint.id() + " carries no Archon framing");
			assertFalse(text.toLowerCase(Locale.ROOT).contains("martyr"), saint.id() + " carries no martyr framing");
		}
		assertTrue(ResourceReader.data(PAGES + "putriciel.json").get("text").getAsString().contains("\"they\""),
				"Putriciel's page marks the change to they/them");
	}

	@Test
	void canonMemoriesAreWovenFromResiduumWithTheirSaintsPairs() {
		String memories = ResourceReader.data("books/libersanguinium/manipulations/pages/saint_memories.json")
				.get("text").getAsString();
		for (String step : List.of("Consecrated Syringe", "Vial Centrifuge", "Hallowed Residuum", "Somatic Loom"))
			assertTrue(memories.contains(step), "route names " + step);
		assertFalse(memories.contains("imprinted"), "Canon Memories are woven, not imprinted");
		for (Saint saint : SAINTS)
			assertTrue(memories.contains(saint.memory() + " (" + saint.primary() + ", " + saint.secondary() + ")"),
					saint.memory() + " lists its Saint's pair");

		String mortem = ResourceReader.data("books/libersanguinium/manipulations/pages/ruinous.json")
				.get("text").getAsString();
		assertFalse(mortem.contains("Crimson Tithe"), "Crimson Tithe is not a Mortem manipulation");
		assertTrue(ResourceReader.data("books/libersanguinium/manipulations/pages/vivacious.json")
				.get("text").getAsString().contains("Crimson Tithe"), "Crimson Tithe sits with Animus");
	}
}
