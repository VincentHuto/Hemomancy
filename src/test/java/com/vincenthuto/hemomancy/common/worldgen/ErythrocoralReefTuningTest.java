package com.vincenthuto.hemomancy.common.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

final class ErythrocoralReefTuningTest {
	public static void main(String[] args) throws IOException {
		makesReefPatchesLargerButRarer();
		prioritizesCustomReefLifeOverVanillaFiller();
		keepsAquaticSpawnDepthsVisibleInDeepReefs();
		keepsDataPackSpawnTablesInSync();
		keepsWhalesTaggedForUnderwaterBreathing();
		keepsHydrothermalVentsAvailableInReefs();
	}

	private static void makesReefPatchesLargerButRarer() throws IOException {
		assertTrue(ErythrocoralReefTuning.PLACED_FEATURE_COUNT < 9,
				"reef attempts should be rarer than the original nine attempts per chunk");
		assertTrue(ErythrocoralReefTuning.PLACED_FEATURE_COUNT >= 5,
				"reef attempts should not become so rare that located reef biomes look empty");
		assertTrue(ErythrocoralReefTuning.MIN_CLUSTER_RADIUS >= 3,
				"reef clusters should start larger than the old radius-2 clusters");
		assertTrue(ErythrocoralReefTuning.maxClusterRadius() >= 4,
				"reef clusters should be able to form broader shelves");

		String placedFeature = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/worldgen/placed_feature/erythrocoral_reef.json"));
		assertTrue(placedFeature.contains("\"count\": " + ErythrocoralReefTuning.PLACED_FEATURE_COUNT),
				"placed feature JSON should use the shared reef attempt count");
	}

	private static void prioritizesCustomReefLifeOverVanillaFiller() {
		assertTrue(ErythrocoralReefTuning.BLOOD_LANTERN_JELLY_WEIGHT > ErythrocoralReefTuning.TROPICAL_FISH_WEIGHT,
				"blood lantern jellies should be the signature visible reef ambient spawn");
		assertTrue(ErythrocoralReefTuning.BARBED_URCHIN_WEIGHT >= ErythrocoralReefTuning.PUFFERFISH_WEIGHT * 2,
				"barbed urchins should not be drowned out by vanilla pufferfish");
		assertTrue(ErythrocoralReefTuning.MNEMONIC_WHALE_WEIGHT > ErythrocoralReefTuning.SQUID_WEIGHT,
				"mnemonic whales should win more water-creature rolls than squid inside their own biome");
	}

	private static void keepsAquaticSpawnDepthsVisibleInDeepReefs() {
		assertTrue(ErythrocoralReefTuning.URCHIN_MAX_DEPTH_BELOW_SEA_LEVEL >= 20,
				"urchins should be allowed down near deep reef shelves");
		assertTrue(ErythrocoralReefTuning.URCHIN_MIN_DEPTH_BELOW_SEA_LEVEL >= 3,
				"urchins should avoid only-surface water rolls");
		assertTrue(ErythrocoralReefTuning.WHALE_MIN_DEPTH_BELOW_SEA_LEVEL <= 7,
				"larger whales should not require such deep rolls that players rarely see them");
	}

	private static void keepsDataPackSpawnTablesInSync() throws IOException {
		String biome = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/worldgen/biome/erythrocoral_reef.json"));
		assertSpawnerWeight(biome, "hemomancy:blood_lantern_jelly",
				ErythrocoralReefTuning.BLOOD_LANTERN_JELLY_WEIGHT);
		assertSpawnerWeight(biome, "hemomancy:barbed_urchin", ErythrocoralReefTuning.BARBED_URCHIN_WEIGHT);
		assertSpawnerWeight(biome, "minecraft:tropical_fish", ErythrocoralReefTuning.TROPICAL_FISH_WEIGHT);
		assertSpawnerWeight(biome, "minecraft:squid", ErythrocoralReefTuning.SQUID_WEIGHT);
		assertSpawnerWeight(biome, "hemomancy:mnemonic_whale", ErythrocoralReefTuning.MNEMONIC_WHALE_WEIGHT);
	}

	private static void keepsWhalesTaggedForUnderwaterBreathing() throws IOException {
		String breathingTag = Files.readString(Path.of(
				"src/main/resources/data/minecraft/tags/entity_type/can_breathe_under_water.json"));
		assertTrue(breathingTag.contains("\"hemomancy:mnemonic_whale\""),
				"mnemonic whales should be tagged as underwater breathers");
	}

	private static void keepsHydrothermalVentsAvailableInReefs() throws IOException {
		String ventBiomeTag = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/tags/worldgen/biome/deep_ocean_vent_spawnlist.json"));
		assertTrue(ventBiomeTag.contains("\"hemomancy:erythrocoral_reef\""),
				"erythrocoral reefs should be in the hydrothermal vent spawnlist tag");
	}

	private static void assertSpawnerWeight(String biome, String entityId, int weight) {
		String pattern = "\"type\"\\s*:\\s*\"" + Pattern.quote(entityId) + "\"[\\s\\S]*?\"weight\"\\s*:\\s*" + weight;
		assertTrue(Pattern.compile(pattern).matcher(biome).find(),
				entityId + " should use shared reef spawn weight " + weight);
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
