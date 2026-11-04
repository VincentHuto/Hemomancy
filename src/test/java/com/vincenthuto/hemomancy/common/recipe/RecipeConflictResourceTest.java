package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Two crafting-table recipes with the same grid or the same shapeless ingredients can only ever produce one result;
 * the other items become uncraftable (cleanup audit RS-14 / A-21). Every shaped pattern plus key, and every shapeless
 * ingredient set, is unique across the mod's recipes.
 */
class RecipeConflictResourceTest {
	@Test
	void craftingRecipesDoNotShareAGrid() throws Exception {
		Map<String, List<String>> byInput = new TreeMap<>();
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("recipe"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject recipe = JsonParser.parseString(Files.readString(file).replace("﻿", "")).getAsJsonObject();
				String type = recipe.has("type") ? recipe.get("type").getAsString() : "";
				String signature;
				if (type.equals("minecraft:crafting_shaped")) {
					signature = "shaped " + recipe.get("pattern") + " " + recipe.get("key");
				} else if (type.equals("minecraft:crafting_shapeless")) {
					signature = "shapeless " + recipe.get("ingredients");
				} else {
					continue;
				}
				byInput.computeIfAbsent(signature, ignored -> new ArrayList<>()).add(file.getFileName().toString());
			}
		}
		List<String> conflicts = new ArrayList<>();
		for (var entry : byInput.entrySet()) {
			if (entry.getValue().size() > 1) conflicts.add(String.join(" = ", entry.getValue()));
		}
		assertEquals(List.of(), conflicts, "recipes that share a grid");
	}
}
