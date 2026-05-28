package com.vincenthuto.hemomancy.common.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BrokenChurchBiomePlacementResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BrokenChurchBiomePlacementResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String tag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/has_structure/broken_church.json"));
		String structure = read(RESOURCE_ROOT.resolve("data/hemomancy/worldgen/structure/broken_church.json"));

		assertContains("broken church structure should still use its biome tag", structure,
				"\"biomes\": \"#hemomancy:has_structure/broken_church\"");
		assertContains("broken church should spawn in fungal gardens", tag,
				"\"hemomancy:fungal_gardens\"");
		assertDoesNotContain("broken church should not spawn in vanilla overworld biomes", tag,
				"\"minecraft:");
		assertDoesNotContain("broken church should not spawn in other fungal dimension biomes", tag,
				"\"hemomancy:fungal_isles\"");
		assertDoesNotContain("broken church should not spawn in sporecrown thicket", tag,
				"\"hemomancy:sporecrown_thicket\"");
		assertDoesNotContain("broken church should not spawn in mycelial depths", tag,
				"\"hemomancy:mycelial_depths\"");
		assertDoesNotContain("broken church should not spawn in hemorrhagic plateau", tag,
				"\"hemomancy:hemorrhagic_plateau\"");
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

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}
}
