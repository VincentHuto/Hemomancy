package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class FerventChitiniteNetherPlacementResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private FerventChitiniteNetherPlacementResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
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
}
