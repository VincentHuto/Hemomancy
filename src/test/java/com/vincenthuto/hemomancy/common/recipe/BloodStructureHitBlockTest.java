package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodStructureHitBlockTest {
	private static final Path RECIPES = Path.of(
			"src/main/resources/data/hemomancy/recipe/blood_structure");

	@Test
	void everyHitBlockAppearsInItsStructurePattern() throws IOException {
		List<String> unreachable = new ArrayList<>();

		try (var paths = Files.list(RECIPES)) {
			for (Path path : paths.filter(file -> file.toString().endsWith(".json")).sorted().toList()) {
				JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				String hitBlock = recipe.get("hitBlock").getAsString();
				JsonObject key = recipe.getAsJsonObject("key");
				boolean present = usedSymbols(recipe.getAsJsonArray("pattern")).stream()
						.map(String::valueOf)
						.map(key::getAsJsonObject)
						.filter(entry -> entry != null && entry.has("block"))
						.anyMatch(entry -> hitBlock.equals(entry.get("block").getAsString()));
				if (!present) unreachable.add(path.getFileName().toString());
			}
		}

		assertTrue(unreachable.isEmpty(), "hitBlock absent from pattern: " + unreachable);
	}

	private static List<Character> usedSymbols(JsonArray pattern) {
		List<Character> symbols = new ArrayList<>();
		for (JsonElement layerElement : pattern) {
			for (JsonElement rowElement : layerElement.getAsJsonArray()) {
				for (char symbol : rowElement.getAsString().toCharArray()) {
					if (symbol != ' ' && !symbols.contains(symbol)) symbols.add(symbol);
				}
			}
		}
		return symbols;
	}
}
