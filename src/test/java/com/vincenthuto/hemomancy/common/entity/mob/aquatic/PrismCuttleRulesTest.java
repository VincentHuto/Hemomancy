package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class PrismCuttleRulesTest {
	public static void main(String[] args) throws IOException {
		spawnsOnlyInOpenWarmWater();
		maintainsSwimmingDepthBelowSurface();
		defensiveFlashIsBriefAndPlayerScoped();
		blockColorsQuantizeToCamouflageVariants();
		resourcesAreRegistered();
	}

	private static void maintainsSwimmingDepthBelowSurface() {
		assertTrue(PrismCuttleRules.verticalDepthCorrection(62, 60.5D) < 0.0D,
				"prism cuttles drifting near the surface should swim downward");
		assertTrue(PrismCuttleRules.verticalDepthCorrection(62, 52.0D) <= 0.0D,
				"prism cuttles at their preferred depth should not float upward");
		assertTrue(PrismCuttleRules.verticalDepthCorrection(62, 41.0D) > 0.0D,
				"prism cuttles far below their band can gently rise");
	}

	private static void spawnsOnlyInOpenWarmWater() {
		assertTrue(PrismCuttleRules.canNaturalSpawn(true, true, 62, 48),
				"prism cuttles should spawn in open warm ocean water");
		assertFalse(PrismCuttleRules.canNaturalSpawn(false, true, 62, 48),
				"prism cuttles require water at spawn");
		assertFalse(PrismCuttleRules.canNaturalSpawn(true, false, 62, 48),
				"prism cuttles need open water around the spawn position");
		assertFalse(PrismCuttleRules.canNaturalSpawn(true, true, 62, 55),
				"prism cuttles should prefer real swimming depth below sea level");
	}

	private static void defensiveFlashIsBriefAndPlayerScoped() {
		assertTrue(PrismCuttleRules.shouldFlash(true, false, false, 0,
				PrismCuttleRules.FLASH_RANGE_BLOCKS - 0.25D),
				"nearby survival players should trigger the defensive flash");
		assertFalse(PrismCuttleRules.shouldFlash(true, true, false, 0, 1.0D),
				"creative players should not trigger the flash");
		assertFalse(PrismCuttleRules.shouldFlash(true, false, true, 0, 1.0D),
				"spectators should not trigger the flash");
		assertFalse(PrismCuttleRules.shouldFlash(false, false, false, 0, 1.0D),
				"dead players should not trigger the flash");
		assertFalse(PrismCuttleRules.shouldFlash(true, false, false, 40, 1.0D),
				"flash should respect cooldown");
		assertTrue(PrismCuttleRules.BLINDNESS_DURATION_TICKS <= 80,
				"defensive blindness should stay brief");
		assertTrue(PrismCuttleRules.FLASH_COOLDOWN_TICKS > PrismCuttleRules.BLINDNESS_DURATION_TICKS,
				"flash cooldown should be longer than the status effect");
	}

	private static void blockColorsQuantizeToCamouflageVariants() {
		assertEquals("sand color", PrismCuttleVariant.SAND.id(),
				PrismCuttleVariant.fromBlockColor(0xD8C988).id());
		assertEquals("coral red color", PrismCuttleVariant.CORAL.id(),
				PrismCuttleVariant.fromBlockColor(0xC94C5E).id());
		assertEquals("kelp color", PrismCuttleVariant.KELP.id(),
				PrismCuttleVariant.fromBlockColor(0x355E3B).id());
		assertEquals("deep blue color", PrismCuttleVariant.DEEP.id(),
				PrismCuttleVariant.fromBlockColor(0x254C7B).id());
	}

	private static void resourcesAreRegistered() throws IOException {
		String entityInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String itemInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String lang = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"));

		assertContains("entity registry", entityInit, "prism_cuttle");
		assertContains("spawn egg registry", itemInit, "spawn_egg_prism_cuttle");
		assertContains("entity language", lang, "\"entity.hemomancy.prism_cuttle\": \"Prism Cuttle\"");
		assertContains("spawn egg language", lang,
				"\"item.hemomancy.spawn_egg_prism_cuttle\": \"Prism Cuttle Spawn Egg\"");
		assertSpawnEggModel("spawn_egg_prism_cuttle");
		String entity = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PrismCuttleEntity.java"));
		String model = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/PrismCuttleModel.java"));
		assertContains("cuttlefish uses aquatic base", entity, "extends WaterAnimal");
		assertContains("cuttlefish imports water animal", entity, "net.minecraft.world.entity.animal.WaterAnimal");
		assertContains("tentacles angle downward", model, "PrismCuttleRules.TENTACLE_REST_X_ROT");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_prism_cuttle.json")),
				"prism cuttle biome modifier should exist");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/tags/worldgen/biome/prism_cuttle_spawnlist.json")),
				"prism cuttle spawnlist should exist");
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
