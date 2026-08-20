package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BuildingBlockResourceCoverageTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private static final String[] BUILDING_BLOCKS = {
			"pallid_silver_chain",
			"hematic_iron_chain",
			"hematic_iron_bars",
			"pale_silver_bars",
			"venous_stone_wall",
			"polished_venous_stone_brick_wall",
			"hematic_iron_door",
			"hematic_iron_trapdoor"
	};

	private BuildingBlockResourceCoverageTest() {
	}

	public static void main(String[] args) throws IOException {
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String pickaxeMineable = read(RESOURCE_ROOT.resolve("data/minecraft/tags/block/mineable/pickaxe.json"));

		for (String block : BUILDING_BLOCKS) {
			assertExists("blockstate for " + block,
					RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/" + block + ".json"));
			assertExists("block model for " + block,
					RESOURCE_ROOT.resolve("assets/hemomancy/models/block/" + block + ".json"));
			assertExists("item model for " + block,
					RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + block + ".json"));
			assertExists("loot table for " + block,
					RESOURCE_ROOT.resolve("data/hemomancy/loot_table/blocks/" + block + ".json"));
			assertExists("crafting recipe for " + block,
					RESOURCE_ROOT.resolve("data/hemomancy/recipe/" + block + ".json"));
			assertContains("language key for " + block, lang, "\"block.hemomancy." + block + "\"");
			assertContains("pickaxe mineable tag for " + block, pickaxeMineable, "\"hemomancy:" + block + "\"");
		}
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
