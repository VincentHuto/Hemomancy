package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderingRecipeResourceTest {
	private static final List<String> ENZYMES = List.of("vivacious", "ruinous", "neurotic", "ferric", "fervent",
			"frigid", "incandescent", "umbral");

	@Test
	void everyRenderingRecipeIsWellFormedAndEveryEnzymeHasASource() throws IOException {
		Set<String> outputs = new HashSet<>();
		List<Path> files;
		try (Stream<Path> stream = Files.list(RepoPaths.DATA.resolve("recipe/rendering"))) {
			files = stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
		assertEquals(9, files.size(), "one recipe per tendency enzyme, plus the spore sac");
		for (Path file : files) {
			String name = file.getFileName().toString();
			JsonObject recipe = ResourceReader.data("recipe/rendering/" + name);
			assertEquals("hemomancy:rendering", recipe.get("type").getAsString(), name);
			assertTrue(recipe.has("input"), name + " has an input");
			assertTrue(recipe.getAsJsonArray("results").size() > 0, name + " has results");
			assertTrue(!recipe.has("render_time") || recipe.get("render_time").getAsInt() > 0, name);
			for (JsonElement element : recipe.getAsJsonArray("results")) {
				JsonObject result = element.getAsJsonObject();
				float chance = result.has("chance") ? result.get("chance").getAsFloat() : 1f;
				assertTrue(chance > 0f && chance <= 1f, name + " chance in (0,1]: " + chance);
				assertFalse(result.get("id").getAsString().isBlank(), name);
				outputs.add(result.get("id").getAsString());
			}
		}
		for (String enzyme : ENZYMES) {
			assertTrue(outputs.contains("hemomancy:" + enzyme + "_enzyme"), enzyme + " enzyme has a rendering source");
		}
	}
}
