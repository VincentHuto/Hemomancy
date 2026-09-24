package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class CorticalArchipelagoFeatureSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String FEATURE =
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/CorticalArchipelagoFeature.java";

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void islandsComeFromThePlanAndShape() throws IOException {
		String source = read(FEATURE);
		assertTrue(source.contains("CorticalArchipelagoPlan.plan"), "islands must come from the orbital plan");
		assertTrue(source.contains("CorticalIslandShape.sample"), "island blocks must come from the pure shape");
		assertTrue(source.contains("CorticalArchipelagoPlan.cableEndpoints"), "cables must run rim to rim");
		// Terrain height may only be used to REJECT spots over vanilla land (isSolid), never to place islands.
		int uses = source.split(java.util.regex.Pattern.quote("getBaseHeight("), -1).length - 1;
		int isSolid = source.indexOf("private static boolean isSolid(");
		int sampled = source.indexOf("getBaseHeight(");
		assertTrue(uses == 1 && isSolid >= 0 && sampled > isSolid && sampled < source.indexOf("}", isSolid),
				"terrain sampling is allowed only inside isSolid (rejection); islands must never be inferred from it");
	}

	@Test
	void orbitCentreUsesBiomeSourceAndMindPlacement() throws IOException {
		String source = read(FEATURE);
		assertTrue(source.contains("getNoiseBiome"), "footprint check must query the biome source");
		assertTrue(source.contains("RandomSpreadStructurePlacement"), "orbit centres must use the Mind placement grid");
		assertTrue(source.contains("getPotentialStructureChunk"), "Mind candidates come from the placement");
	}

	@Test
	void orbitalPlansAreCachedAndClearedOnServerStop() throws IOException {
		String source = read(FEATURE);
		assertTrue(source.contains("REGION_CACHE.computeIfAbsent(orbitSeed"),
				"plans are cached by their central Mind's orbit seed");
		assertTrue(source.contains("ServerStoppedEvent") && source.contains("REGION_CACHE.clear()"),
				"the cache must be dropped when the server stops");
	}

	@Test
	void placedFeatureHasNoBiomeFilter() throws IOException {
		String placed = read("src/main/resources/data/hemomancy/worldgen/placed_feature/cortical_archipelago.json");
		assertTrue(placed.contains("hemomancy:cortical_archipelago"));
		assertFalse(placed.contains("minecraft:biome"),
				"chunks at patch edges must still draw islands the filter already validated");
	}

	@Test
	void archipelagoRunsOnceInEveryEndChunk() throws IOException {
		JsonObject modifier = JsonParser.parseString(read(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_cortical_archipelago.json"))
				.getAsJsonObject();
		assertEquals("neoforge:add_features", modifier.get("type").getAsString());
		assertEquals("#minecraft:is_end", modifier.get("biomes").getAsString(),
				"every End chunk must invoke the feature so cross-biome islands and cables cannot be sliced");
		assertEquals("hemomancy:cortical_archipelago", modifier.get("features").getAsString());
		assertEquals("surface_structures", modifier.get("step").getAsString());

		JsonObject endTag = JsonParser.parseString(
				read("src/main/resources/data/minecraft/tags/worldgen/biome/is_end.json")).getAsJsonObject();
		assertTrue(endTag.getAsJsonArray("values").asList().stream()
				.anyMatch(value -> value.getAsString().equals("hemomancy:cortical_drift")),
				"the custom biome must join minecraft:is_end so its own chunks receive the global feature");

		JsonObject biome = JsonParser.parseString(
				read("src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json")).getAsJsonObject();
		for (var step : biome.getAsJsonArray("features")) {
			assertFalse(step.getAsJsonArray().asList().stream()
					.anyMatch(value -> value.getAsString().equals("hemomancy:cortical_archipelago")),
					"direct biome registration would run the same archipelago twice in Cortical Drift chunks");
		}
	}

	@Test
	void nerveBridgeIsGoneFromMain() throws IOException {
		try (Stream<Path> files = Files.walk(ROOT.resolve("src/main"))) {
			for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
				String name = file.getFileName().toString();
				assertFalse(name.toLowerCase().contains("nerve_bridge") || name.contains("NerveBridge"), file.toString());
				if (name.endsWith(".java") || name.endsWith(".json")) {
					String text = Files.readString(file);
					assertFalse(text.contains("nerve_bridge") || text.contains("NerveBridge")
							|| text.contains("NERVE_BRIDGE"), "stale nerve bridge reference in " + file);
				}
			}
		}
	}

	@Test
	void islandsAvoidVanillaTerrain() throws IOException {
		String source = java.nio.file.Files.readString(java.nio.file.Path.of("").toAbsolutePath().resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/CorticalArchipelagoFeature.java"));
		org.junit.jupiter.api.Assertions.assertTrue(source.contains("isSolid(") && source.contains("getBaseHeight("),
				"edge registration covers vanilla continent rims; the spot filter must reject footprints over "
						+ "existing vanilla terrain or islands fuse into continents");
	}
}
