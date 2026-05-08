package com.vincenthuto.hemomancy.common.entity.mob.monster;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class BloodDrunkPuppeteerPolishTest {
	private BloodDrunkPuppeteerPolishTest() {
	}

	public static void main(String[] args) throws Exception {
		assertEquals("puppeteer summons four dolls", 4, BloodDrunkPuppeteerTuning.DOLL_COUNT);
		assertTrue("doll owner search supports combat spacing",
				BloodDrunkPuppeteerTuning.DOLL_OWNER_SEARCH_RANGE >= 48.0);
		assertTrue("summoned dolls are not a loot farm",
				BloodDrunkPuppeteerTuning.SUMMONED_DOLLS_DROP_LOOT == false);

		Set<String> offsets = new HashSet<>();
		for (int i = 0; i < BloodDrunkPuppeteerTuning.DOLL_COUNT; i++) {
			double[] offset = BloodDrunkPuppeteerTuning.dollSpawnOffset(i);
			offsets.add(offset[0] + "," + offset[2]);
		}
		assertEquals("dolls spawn with distinct offsets", BloodDrunkPuppeteerTuning.DOLL_COUNT, offsets.size());

		assertFileContains("biome modifier", Path.of(
						"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_blood_drunk_puppeteer.json"),
				"hemomancy:blood_drunk_puppeteer");
		assertFileContains("spawn biome tag", Path.of(
						"src/main/resources/data/hemomancy/tags/worldgen/biome/blood_drunk_puppeteer_spawnlist.json"),
				"minecraft:dark_forest");
		assertFileContains("puppeteer loot", Path.of(
						"src/main/resources/data/hemomancy/loot_table/entities/blood_drunk_puppeteer.json"),
				"hemomancy:puppeteering_thread");
	}

	private static void assertFileContains(String label, Path path, String needle) throws Exception {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
		String content = Files.readString(path);
		if (!content.contains(needle)) {
			throw new AssertionError(label + ": expected " + needle);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}
}
