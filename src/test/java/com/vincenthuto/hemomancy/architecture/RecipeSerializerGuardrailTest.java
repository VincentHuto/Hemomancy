package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * L-8: a recipe codec is a {@code RecordCodecBuilder} codec or the one JSON bridge in {@code RecipeCodecJson}; recipes
 * that only stations read extend {@code StationRecipe}. {@code CustomRecipe} is left to real crafting-grid recipes.
 */
class RecipeSerializerGuardrailTest {
	private static final List<String> CRAFTING_GRID_RECIPES = List.of("CopyBloodGourdRecipe.java",
			"CopyMorphlingJarRecipe.java", "FillBloodGourdRecipe.java", "ScaleGripBindingRecipe.java");

	@Test
	void recipeCodecsAreNotHandWrittenAgain() throws Exception {
		List<String> handWritten = new ArrayList<>();
		List<String> customRecipes = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.mainJava("common/recipe"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
				String source = SourceAssertions.read(file);
				String name = file.getFileName().toString();
				if (source.contains("new MapCodec<") && !name.equals("RecipeCodecJson.java")) handWritten.add(name);
				if (source.contains("extends CustomRecipe") || source.contains("extends net.minecraft.world.item.crafting.CustomRecipe"))
					customRecipes.add(name);
			}
		}
		assertEquals(List.of(), handWritten, "Use RecordCodecBuilder, or RecipeCodecJson.bridge for a JSON schema");
		assertEquals(CRAFTING_GRID_RECIPES, customRecipes, "A recipe only a station reads extends StationRecipe");
	}
}
