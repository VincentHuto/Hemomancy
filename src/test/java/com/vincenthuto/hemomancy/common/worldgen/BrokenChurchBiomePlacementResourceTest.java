package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class BrokenChurchBiomePlacementResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BrokenChurchBiomePlacementResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String tag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/has_structure/broken_church.json"));
		String structure = read(RESOURCE_ROOT.resolve("data/hemomancy/worldgen/structure/broken_church.json"));

		assertContains("broken church structure should still use its biome tag", structure,
				"\"biomes\": \"#hemomancy:has_structure/broken_church\"");
		assertContains("broken church should spawn in fungal gardens", tag,
				"\"hemomancy:fungal_gardens\"");
		assertContains("broken church also spawns outside the optional Gardens region", tag, "\"minecraft:swamp\"");
		assertNotContains("broken church should not spawn in other fungal dimension biomes", tag,
				"\"hemomancy:fungal_isles\"");
		assertNotContains("broken church should not spawn in sporecrown thicket", tag,
				"\"hemomancy:sporecrown_thicket\"");
		assertNotContains("broken church should not spawn in mycelial depths", tag,
				"\"hemomancy:mycelial_depths\"");
		assertNotContains("broken church should not spawn in hemorrhagic plateau", tag,
				"\"hemomancy:hemorrhagic_plateau\"");
	}
}
