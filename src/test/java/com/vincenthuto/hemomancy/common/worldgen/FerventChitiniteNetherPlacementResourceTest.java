package com.vincenthuto.hemomancy.common.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FerventChitiniteNetherPlacementResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private FerventChitiniteNetherPlacementResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String modifier = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/neoforge/biome_modifier/add_fervent_chitinite_cave.json"));
		String netherTag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/fervent_chitinite_nether_spawnlist.json"));
		String caveTag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/chitinite_cave_spawnlist.json"));

		assertContains("Fervent Chitinite spawns should use the Nether spawn tag", modifier,
				"\"biomes\": \"#hemomancy:fervent_chitinite_nether_spawnlist\"");
		assertContains("Fervent Chitinite Nether tag should include Basalt Deltas", netherTag,
				"\"minecraft:basalt_deltas\"");
		assertNotContains("Fervent Chitinite should no longer share ordinary Chitinite cave placement",
				caveTag, "fervent_chitinite");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
