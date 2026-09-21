package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DynastyCastleWorldgenSourceTest {

	private static final Path STRUCTURE = Path.of("src/main/resources/data/hemomancy/worldgen/structure/dynasty_castle.json");
	private static final Path SET = Path.of("src/main/resources/data/hemomancy/worldgen/structure_set/dynasty_castle.json");
	private static final Path BIOME_TAG = Path.of("src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json");
	private static final Path OLD_FEATURE = Path.of("src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DynastyCastleFeature.java");
	private static final Path STRUCTURE_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java");

	@Test
	void structureIsRegisteredAndSpaced() throws Exception {
		assertTrue(Files.readString(STRUCTURE).contains("hemomancy:dynasty_castle"));
		assertTrue(Files.readString(STRUCTURE).contains("beard_thin"));
		String set = Files.readString(SET);
		assertTrue(set.contains("random_spread"));
		assertTrue(set.contains("\"separation\""));
		assertTrue(Files.readString(BIOME_TAG).contains("hemomancy:fungal_gardens"));
		assertTrue(Files.readString(STRUCTURE_INIT).contains("dynasty_castle"));
	}

	@Test
	void oldFeatureIsGone() {
		assertFalse(Files.exists(OLD_FEATURE));
	}
}
