package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class CorticalDriftBiomeRegistrationSourceTest {
	private static String biomeInit() throws IOException {
		return Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java"));
	}

	@Test
	void biomeKeyIsRegisteredAndBootstrapped() throws IOException {
		String source = biomeInit();
		assertTrue(source.contains("CORTICAL_DRIFT = register(\"cortical_drift\")"),
				"the biome key must exist");
		assertTrue(source.contains("register(context, CORTICAL_DRIFT, corticalDrift("),
				"the biome must be bootstrapped like its siblings");
	}

	@Test
	void registeredOnlyAsAnIslandBiome() throws IOException {
		String source = biomeInit();
		assertTrue(source.contains("EndBiomeRegistry.registerIslandBiome(BiomeInit.CORTICAL_DRIFT"),
				"island zones are mostly void, where CorticalArchipelagoFeature builds every island itself");
		assertFalse(source.contains("EndBiomeRegistry.registerHighlandsBiome(BiomeInit.CORTICAL_DRIFT"),
				"highlands would reskin vanilla continents, which the archipelago redesign rejects");
		assertFalse(source.contains("EndBiomeRegistry.registerMidlandsBiome(BiomeInit.CORTICAL_DRIFT"),
				"midlands would reskin vanilla continents, which the archipelago redesign rejects");
		assertTrue(source.contains("EndBiomeRegistry.registerEdgeBiome(BiomeInit.CORTICAL_DRIFT"),
				"TerraBlender 4.1.0.8 builds islandsArea from the EDGE list, so the End's void zones only ever "
						+ "draw edge-registered biomes; without this the biome never generates at all");
	}

	@Test
	void endSurfaceRulesAreInstalled() throws IOException {
		assertTrue(biomeInit().contains(
				"SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.END"),
				"END-category surface rules must be installed alongside the OVERWORLD and NETHER ones");
	}

	@Test
	void registrationRespectsTheConfigToggle() throws IOException {
		String source = biomeInit();
		int index = source.indexOf("EndBiomeRegistry.registerIslandBiome");
		assertTrue(source.lastIndexOf("ENABLE_CORTICAL_DRIFT_END_REGION", index) > 0,
				"End registration must sit behind the config toggle");
	}

	@Test
	void biomeJsonExistsAndMatchesTheBuilder() throws IOException {
		Path biomeJson = Path.of("").toAbsolutePath()
				.resolve("src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json");
		assertTrue(Files.exists(biomeJson),
				"bootstrapBiomes is dead code in this repo - without this JSON file, "
						+ "hemomancy:cortical_drift does not exist at runtime no matter what BiomeInit says");

		JsonObject biome;
		try (var reader = Files.newBufferedReader(biomeJson)) {
			biome = JsonParser.parseReader(reader).getAsJsonObject();
		}

		var monsters = biome.getAsJsonObject("spawners").getAsJsonArray("monster");
		assertEquals(1, monsters.size(), "the Cortical Drift should only configure its cable-native monster");
		assertEquals("hemomancy:myelin_borer", monsters.get(0).getAsJsonObject().get("type").getAsString(),
				"the biome JSON's only monster spawner should be the Myelin Borer");
		assertFalse(monsters.toString().contains("hemomancy:abhorent_thought"),
				"Abhorent Thought must not spawn in Cortical Drift");

		String source = biomeInit();
		int builderStart = source.indexOf("private static Biome corticalDrift(");
		int builderEnd = source.indexOf("public static void commonSetup", builderStart);
		String builder = source.substring(builderStart, builderEnd);
		assertFalse(builder.contains("EntityInit.abhorent_thought"),
				"the corticalDrift() builder should also exclude Abhorent Thought");
		assertTrue(builder.contains("EntityInit.myelin_borer"),
				"the corticalDrift() builder should retain Myelin Borer");

		assertFalse(biome.get("has_precipitation").getAsBoolean(),
				"cortical_drift must have has_precipitation=false in the JSON, matching the "
						+ "corticalDrift() builder's hasPrecipitation(false) - the builder is dead code so "
						+ "only the JSON value actually applies at runtime");
	}
}
