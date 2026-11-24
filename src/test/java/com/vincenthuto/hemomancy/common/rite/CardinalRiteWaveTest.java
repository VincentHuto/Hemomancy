package com.vincenthuto.hemomancy.common.rite;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteWaveRules.Objective;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteWaveTest {
	private static final Path RITE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	@Test
	void everyWaveARecipeDealsIsInTheTable() throws IOException {
		List<String> unknown = new ArrayList<>();
		try (var paths = Files.walk(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				if (!json.has("ceremony")) continue;
				JsonObject ceremony = json.getAsJsonObject("ceremony");
				for (String field : List.of("waves", "guaranteed_waves")) {
					if (!ceremony.has(field)) continue;
					for (JsonElement wave : ceremony.getAsJsonArray(field)) {
						if (CardinalRiteWave.byId(wave.getAsString()).isEmpty()) unknown.add(path.getFileName() + ": " + wave);
					}
				}
			}
		}
		assertEquals(List.of(), unknown, "a recipe names a wave the ordeal does not know");
	}

	@Test
	void eachWaveCarriesItsObjective() {
		assertEquals(Objective.SURVIVE, CardinalRiteWave.of("bloodlicker_siphon").objective());
		assertEquals(Objective.SURVIVE, CardinalRiteWave.of("fargone_dive").objective());
		assertEquals(Objective.SURVIVE, CardinalRiteWave.of("rogue_will").objective());
		assertEquals(Objective.DEFEAT, CardinalRiteWave.of("false_omens").objective());
		assertEquals(Objective.TRACE, CardinalRiteWave.of("response_sigil").objective());
		assertEquals(Objective.TRACE, CardinalRiteWave.of("covenant_pressure").objective());
		assertEquals(Objective.TRACE, CardinalRiteWave.of("discover_suture").objective());
	}

	@Test
	void onlyWavesThatAreFoughtSpawnThreats() {
		for (CardinalRiteWave wave : CardinalRiteWave.values()) {
			assertEquals(wave.objective() != Objective.TRACE, wave.spawnsThreats(), wave.name());
		}
	}

	@Test
	void aDiscoveryWaveNamesTheSigilItWakes() {
		assertEquals(CardinalRiteWave.DISCOVER, CardinalRiteWave.of("discover_cage"));
		assertEquals("cage", CardinalRiteWave.discoveredSigil("discover_cage"));
		assertEquals(null, CardinalRiteWave.discoveredSigil("rogue_will"));
	}

	@Test
	void aMissingOrUnknownWaveFallsBackToTheBloodlickers() {
		assertEquals(CardinalRiteWave.BLOODLICKER_SIPHON, CardinalRiteWave.of(null));
		assertEquals(CardinalRiteWave.BLOODLICKER_SIPHON, CardinalRiteWave.of("no_such_wave"));
		assertTrue(CardinalRiteWave.byId("no_such_wave").isEmpty());
		assertFalse(CardinalRiteWave.byId("fargone_dive").isEmpty());
	}

	@Test
	void theFargoneDiveFromAbove() {
		assertEquals(5.0D, CardinalRiteWave.FARGONE_DIVE.spawnHeight());
		assertEquals(1.0D, CardinalRiteWave.BLOODLICKER_SIPHON.spawnHeight());
	}
}
