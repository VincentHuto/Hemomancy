package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class CardinalRiteSampleRecipesTest {
	private static final Path ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	@Test
	void sampleRitesCoverTheInteractiveCeremonyProgression() throws IOException {
		JsonObject circulation = read("sample_circulation.json");
		JsonObject inscription = read("sample_inscription.json");
		JsonObject bloodline = read("sample_bloodline_vigil.json");
		JsonObject gauntlet = read("sample_grand_gauntlet.json");

		assertCeremony(circulation, "abbreviated", "cardinal", 0, false);
		assertCeremony(inscription, "full", "diagonal", 1, false);
		assertCeremony(bloodline, "full", "crooked", 4, true);
		assertCeremony(gauntlet, "full", "serpentine", 6, true);

		assertEquals("simulacrum_wound",
				bloodline.getAsJsonObject("ceremony").get("signature").getAsString(),
				"bloodline signature");
		assertEquals("trifold_judgment",
				gauntlet.getAsJsonObject("ceremony").get("signature").getAsString(),
				"grand signature");
	}

	@Test
	void grandGauntletForcesEveryOrdealFeature() throws IOException {
		JsonObject ceremony = read("sample_grand_gauntlet.json").getAsJsonObject("ceremony");
		Set<String> waves = strings(ceremony.getAsJsonArray("waves"));
		waves.addAll(strings(ceremony.getAsJsonArray("guaranteed_waves")));

		assertTrue(waves.contains("bloodlicker_siphon"), "bloodlicker siphon wave");
		assertTrue(waves.contains("fargone_dive"), "fargone dive wave");
		assertTrue(waves.contains("rogue_will"), "rogue will wave");
		assertTrue(waves.contains("false_omens"), "false omen wave");
		assertTrue(waves.contains("response_sigil"), "response sigil wave");
		assertTrue(waves.stream().anyMatch(wave -> wave.startsWith("discover_")),
				"sigil discovery wave");
	}

	@Test
	void everySampleIsCheapRepeatableAndHasExplicitTestSockets() throws IOException {
		for (String name : new String[] {
				"sample_circulation.json",
				"sample_inscription.json",
				"sample_bloodline_vigil.json",
				"sample_grand_gauntlet.json"
		}) {
			JsonObject recipe = read(name);
			assertEquals(0, recipe.get("bloodCost").getAsInt(), name + " legacy blood cost");
			assertTrue(recipe.has("floor"), name + " names a reusable floor");
			assertTrue(recipe.has("brazier_signature"), name + " has a brazier signature");
			if (recipe.has("required_structure")) {
				assertTrue(!recipe.getAsJsonObject("required_structure")
						.has("consume_on_success")
						|| !recipe.getAsJsonObject("required_structure")
								.get("consume_on_success").getAsBoolean(),
						name + " preserves its upper structure");
			}
			JsonObject ceremony = recipe.getAsJsonObject("ceremony");
			assertTrue(ceremony.getAsJsonArray("support_sockets").size() >= 2,
					name + " support sockets");
			assertTrue(ceremony.getAsJsonArray("fragile_offsets").size() >= 1,
					name + " fragile offsets");
		}
	}

	private static void assertCeremony(JsonObject recipe, String profile, String layout,
			int requiredDegree, boolean rankup) {
		assertEquals(requiredDegree, recipe.get("required_degree").getAsInt(), "required degree");
		assertEquals(rankup, recipe.has("rankup") && recipe.get("rankup").getAsBoolean(), "rankup");
		JsonObject ceremony = recipe.getAsJsonObject("ceremony");
		assertEquals(profile, ceremony.get("profile").getAsString(), "profile");
		assertEquals(layout, ceremony.get("layout").getAsString(), "layout");
	}

	private static JsonObject read(String name) throws IOException {
		return JsonParser.parseString(Files.readString(ROOT.resolve(name))).getAsJsonObject();
	}

	private static Set<String> strings(JsonArray array) {
		Set<String> values = new HashSet<>();
		array.forEach(element -> values.add(element.getAsString()));
		return values;
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) throw new AssertionError("Missing " + label);
	}
}
