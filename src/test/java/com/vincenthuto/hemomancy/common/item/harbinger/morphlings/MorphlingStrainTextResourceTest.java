package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.EnglishText;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Brined Court Track 4: every strain in the table has its full text (folk name, binomial, description, six maturity
 * lines, passive and ladder lines, Bestiary entry, Court lines and an Alchemist inquiry), the Liber pages list the
 * strains and the Cradle set as the table has them, and the strain text keeps the lore guardrails.
 */
final class MorphlingStrainTextResourceTest {
	private static final String LANG = "assets/hemomancy/lang/en_us.json";
	private static final String PAGES = "data/hemomancy/books/libersanguinium/the_hematic_order/pages/";
	/** Many heads belong to Hydra; the octopus stinkhorn's folk name and God or devil framing are off-limits. */
	private static final Pattern FORBIDDEN = Pattern.compile(
			"polycephalum|many-headed|many heads|\\bdevil|\\bdemon|\\bgod\\b|satan", Pattern.CASE_INSENSITIVE);

	@Test
	void everyStrainHasItsItemTextAndSixMaturityLines() {
		assertEquals(6, MorphlingStrain.STAGE_NAMES.length);
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			assertFilled("item.hemomancy." + strain.itemPath());
			assertFilled(strain.binomialKey());
			assertFilled(strain.descriptionKey());
			for (int maturity = 0; maturity < MorphlingStrain.STAGE_NAMES.length; maturity++) {
				assertFilled(strain.stageKey(maturity));
			}
			assertEquals(strain.stageKey(5), strain.stageKey(9), "a stage past Primal reads the Primal line");
			assertFilled(strain.courtKey());
		}
	}

	@Test
	void everyStrainNamesItsPassiveAndFourPowers() {
		JsonObject lang = ResourceReader.jsonObject(LANG);
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String prefix = "message.hemomancy." + strain.id() + "_morphling_item.";
			assertFilled(prefix + "passive");
			long powers = lang.keySet().stream().filter(key -> key.startsWith(prefix)).count();
			assertTrue(powers >= 5, strain.id() + " needs a passive and four ladder lines, found " + powers);
		}
	}

	@Test
	void everyStrainHasABestiaryEntryWithACourtParagraph() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String base = "bestiary.hemomancy.morphling." + strain.polypFamily().serializedName() + ".";
			for (String part : List.of("title", "description", "source", "court")) {
				assertFilled(base + part);
			}
			String name = EnglishText.of("item.hemomancy." + strain.itemPath()).replace(" Morphling", "");
			assertTrue(EnglishText.of(base + "title").startsWith(name), base + "title names " + name);
		}
	}

	@Test
	void theOrderCatalogueNamesNoHouseAndTheCourtLinesNameTheirOwn() {
		Map<CourtHouse, String> houses = Map.of(
				CourtHouse.GIVING_LIGHT, "Giving Light", CourtHouse.HOLDING_STONE, "Holding Stone",
				CourtHouse.SHARED_WAKE, "Shared Wake", CourtHouse.WOVEN_CURRENT, "Woven Current",
				CourtHouse.VEILED_FOLD, "Veiled Fold", CourtHouse.STILL_BELL, "Still Bell",
				CourtHouse.LAST_MARROW, "Last Marrow", CourtHouse.KINDLING, "Kindling");
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String house = houses.get(strain.house());
			String family = "bestiary.hemomancy.morphling." + strain.polypFamily().serializedName() + ".";
			assertTrue(EnglishText.of(strain.courtKey()).contains(house), strain.courtKey());
			assertTrue(EnglishText.of(family + "court").contains(house), family + "court");
			List<String> catalogue = new ArrayList<>(List.of(strain.descriptionKey(), family + "description",
					family + "source", "hemomancy.alchemist.item_inquiry." + strain.itemPath() + ".line1",
					"hemomancy.alchemist.item_inquiry." + strain.itemPath() + ".line2"));
			for (int maturity = 0; maturity < 6; maturity++) catalogue.add(strain.stageKey(maturity));
			for (String key : catalogue) {
				String text = EnglishText.of(key);
				for (String anyHouse : houses.values()) {
					assertFalse(text.contains(anyHouse), key + " gives the Court away before it is known: " + anyHouse);
				}
				assertFalse(text.contains("Court"), key + " names the Court");
			}
		}
	}

	@Test
	void everyStrainHasAnAlchemistInquiry() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String path = "data/hemomancy/dialogue_inquiry/alchemist/hemomancy/" + strain.itemPath() + ".json";
			assertTrue(ResourceReader.exists(path), path);
			JsonArray lines = ResourceReader.jsonObject(path).getAsJsonArray("lines");
			assertEquals(2, lines.size(), path);
			lines.forEach(line -> assertFilled(line.getAsString()));
		}
	}

	@Test
	void theLiberListsEveryStrainAndTheCradleSetFromTheTable() {
		String strains = pageText("morphling_strains.json");
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String name = EnglishText.of("item.hemomancy." + strain.itemPath()).replace(" Morphling", "");
			assertTrue(strains.contains(name + ", " + EnglishText.of(strain.binomialKey())), "strain page lists " + name);
		}
		String cradle = pageText("morphling_cradle_and_nectar.json");
		String list = cradle.substring(cradle.indexOf("support strain:"), cradle.indexOf("are refused"));
		String hosted = list.substring(0, list.indexOf(". "));
		String refused = list.substring(list.indexOf(". "));
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String name = EnglishText.of("item.hemomancy." + strain.itemPath()).replace(" Morphling", "");
			assertEquals(strain.cradlePrimal(), hosted.contains(name), name + " in the Cradle's Primal list");
			assertEquals(!strain.cradlePrimal(), refused.contains(name), name + " in the Cradle's refused list");
		}
		assertTrue(pageText("morphling_cultivation.json").contains("Morphling Polyp"));
	}

	@Test
	void strainTextKeepsTheLoreGuardrails() {
		JsonObject lang = ResourceReader.jsonObject(LANG);
		List<String> found = new ArrayList<>();
		for (String key : lang.keySet()) {
			String lower = key.toLowerCase(Locale.ROOT);
			boolean strainText = lower.startsWith("morphling.hemomancy.") || lower.startsWith("bestiary.hemomancy.morphling.")
					|| lower.contains("_morphling_item.") || lower.contains("item_inquiry.morphling_");
			if (strainText && FORBIDDEN.matcher(lang.get(key).getAsString()).find()) found.add(key);
		}
		for (String page : List.of("morphling_cultivation.json", "morphling_strains.json", "morphling_cradle_and_nectar.json")) {
			if (FORBIDDEN.matcher(pageText(page)).find()) found.add(page);
		}
		assertEquals(List.of(), found);
	}

	private static String pageText(String page) {
		return ResourceReader.jsonObject(PAGES + page).get("text").getAsString();
	}

	private static void assertFilled(String key) {
		assertFalse(EnglishText.of(key).isBlank(), key + " is blank");
	}
}
