package com.vincenthuto.hemomancy.common.entity.mob.animal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class HematicBurrowerRulesTest {
	public static void main(String[] args) throws IOException {
		requiresShallowDarkCaves();
		fleesLivingMobsAndBurrowsWithoutGriefing();
		weightedEscapeDropsFavorCommonOre();
		resourcesAreRegistered();
	}

	private static void requiresShallowDarkCaves() {
		assertTrue(HematicBurrowerRules.canNaturalSpawn(true, true, false, 6, 56, 72),
				"burrowers should spawn in dark shallow caves below forest-like surfaces");
		assertFalse(HematicBurrowerRules.canNaturalSpawn(true, true, true, 6, 56, 72),
				"burrowers should not usually spawn on exposed surface air");
		assertFalse(HematicBurrowerRules.canNaturalSpawn(true, true, false, 11, 56, 72),
				"burrowers should avoid bright cave mouths");
		assertFalse(HematicBurrowerRules.canNaturalSpawn(true, true, false, 6, 12, 72),
				"burrowers should not be deep cave mobs");
		assertFalse(HematicBurrowerRules.canNaturalSpawn(true, true, false, 6, 76, 72),
				"burrowers should stay below the surface heightmap");
	}

	private static void fleesLivingMobsAndBurrowsWithoutGriefing() {
		assertTrue(HematicBurrowerRules.shouldFlee(true, false, false, 5.0D),
				"nearby living mobs should frighten burrowers");
		assertFalse(HematicBurrowerRules.shouldFlee(false, false, false, 5.0D),
				"dead mobs should not frighten burrowers");
		assertFalse(HematicBurrowerRules.shouldFlee(true, true, false, 5.0D),
				"creative players should not force burrower escape loops");
		assertFalse(HematicBurrowerRules.shouldFlee(true, false, true, 5.0D),
				"spectators should not force burrower escape loops");
		assertFalse(HematicBurrowerRules.shouldFlee(true, false, false,
				HematicBurrowerRules.FLEE_RANGE_BLOCKS + 0.5D),
				"burrowers should only flee close threats");
		assertTrue(HematicBurrowerRules.shouldBurrowAway(HematicBurrowerRules.BURROW_AWAY_AFTER_FLEE_TICKS),
				"burrowers should despawn once chased long enough");
		assertTrue(HematicBurrowerRules.BURROW_AWAY_AFTER_FLEE_TICKS > 80,
				"burrowers should not burrow quite as often as the first tuning pass");
		assertTrue(HematicBurrowerRules.BURROW_AWAY_AFTER_FLEE_TICKS <= 105,
				"burrowers should still dig away before a chase drags on too long");
		assertTrue(HematicBurrowerRules.FLEE_NAVIGATION_SPEED <= 1.25D,
				"burrowers should scurry, but not sprint faster than players can read");
		assertFalse(HematicBurrowerRules.BREAKS_BLOCKS_ON_ESCAPE,
				"burrower escape should use particles and drops, not block griefing");
	}

	private static void weightedEscapeDropsFavorCommonOre() {
		assertEquals("drop table weight", 13, HematicBurrowerRules.totalEscapeDropWeight());
		assertEquals("coal drop weight", 7, HematicBurrowerRules.escapeDropWeight("minecraft:coal"));
		assertEquals("raw copper drop weight", 4, HematicBurrowerRules.escapeDropWeight("minecraft:raw_copper"));
		assertEquals("raw iron drop weight", 2, HematicBurrowerRules.escapeDropWeight("minecraft:raw_iron"));
		assertEquals("unknown drop weight", 0, HematicBurrowerRules.escapeDropWeight("minecraft:diamond"));
	}

	private static void resourcesAreRegistered() throws IOException {
		String entityInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String itemInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String lang = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"));

		assertContains("entity registry", entityInit, "hematic_burrower");
		assertContains("spawn egg registry", itemInit, "spawn_egg_hematic_burrower");
		assertContains("entity language", lang, "\"entity.hemomancy.hematic_burrower\": \"Hematic Burrower\"");
		assertContains("spawn egg language", lang,
				"\"item.hemomancy.spawn_egg_hematic_burrower\": \"Hematic Burrower Spawn Egg\"");
		assertSpawnEggModel("spawn_egg_hematic_burrower");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_hematic_burrower.json")),
				"hematic burrower biome modifier should exist");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/tags/worldgen/biome/hematic_burrower_spawnlist.json")),
				"hematic burrower spawnlist should exist");
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}

	private static void assertContains(String label, String content, String expected) {
		if (!content.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertSpawnEggModel(String itemId) throws IOException {
		String model = Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/models/item/" + itemId + ".json"));
		assertContains(itemId + " model parent", model, "\"parent\": \"minecraft:item/template_spawn_egg\"");
	}
}
