package com.vincenthuto.hemomancy.common.manipulation.mortem;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LignumMortisResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");

	@Test
	void organicTagAndWeavingRecipeShipWithTheManipulation() throws Exception {
		String tag = Files.readString(ROOT.resolve("data/hemomancy/tags/block/organic_structure_blocks.json"));
		String recipe = Files.readString(ROOT.resolve("data/hemomancy/recipe/memory_weaving/memory_lignum_mortis.json"));
		String lang = Files.readString(ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertTrue(tag.contains("#minecraft:logs"));
		assertTrue(tag.contains("minecraft:brown_mushroom_block"));
		assertTrue(tag.contains("minecraft:warped_wart_block"));
		assertTrue(recipe.contains("hemomancy:memory_lignum_mortis"));
		assertTrue(recipe.contains("\"animus\": 1"));
		assertTrue(recipe.contains("\"mortem\": 1"));
		assertTrue(lang.contains("hemomancy.manipulation.lignum_mortis.desc"));
	}
}
