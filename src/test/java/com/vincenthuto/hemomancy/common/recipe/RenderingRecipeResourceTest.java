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
		assertEquals(11, files.size(), "one recipe per temper enzyme, coal and charcoal for Fervent, plus the spore sac");
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

	/** CB-14: free rendering yields 25% for the four richer reagents and 10% for the other six. */
	@Test
	void renderingYieldsSitInTheTwoRuledBands() throws IOException {
		Set<String> quarter = Set.of("blaze_powder", "hematic_iron_scrap", "packed_ice", "spider_eye");
		Set<String> tenth = Set.of("bone", "charcoal", "coal", "glowstone_dust", "ink_sac", "rotten_flesh");
		for (String name : quarter) assertEquals(0.25f, enzymeChance(name), 1e-6f, name);
		for (String name : tenth) assertEquals(0.10f, enzymeChance(name), 1e-6f, name);
	}

	private static float enzymeChance(String name) throws IOException {
		JsonObject recipe = ResourceReader.data("recipe/rendering/" + name + ".json");
		for (JsonElement element : recipe.getAsJsonArray("results")) {
			JsonObject result = element.getAsJsonObject();
			if (result.get("id").getAsString().endsWith("_enzyme")) return result.get("chance").getAsFloat();
		}
		throw new AssertionError(name + " renders no enzyme");
	}

	@Test
	void everyEnzymeFruitsForOneHundredFiftyMl() throws IOException {
		List<Path> files;
		try (Stream<Path> stream = Files.list(RepoPaths.DATA.resolve("recipe/enzyme_fruiting"))) {
			files = stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
		assertEquals(8, files.size());
		for (Path file : files) {
			JsonObject recipe = ResourceReader.data("recipe/enzyme_fruiting/" + file.getFileName());
			float total = recipe.get("duration").getAsInt() * recipe.get("blood_per_tick").getAsFloat();
			assertEquals(150f, total, 1e-3f, file.getFileName() + " costs 150 mL (CB-13)");
		}
		assertEquals(150f, EnzymeFruitingRecipe.DEFAULT_DURATION * EnzymeFruitingRecipe.DEFAULT_BLOOD_PER_TICK, 1e-3f);
	}

	/**
	 * Core loop Track 4: the rendering pool and the Temper catalyst table agree. Whatever renders into a school's
	 * enzyme is that school's catalyst in {@code temper_catalyst/<school>.json}, and the reverse for vanilla reagents.
	 */
	@Test
	void everyRenderingReagentIsItsSchoolsCatalyst() throws IOException {
		List<String> schools = List.of("animus", "flammeus", "ductilis", "lux", "mortem", "congeatio", "ferric", "tenebris");
		java.util.Map<String, String> enzymeSchool = new java.util.HashMap<>();
		for (int i = 0; i < schools.size(); i++) {
			String school = schools.get(i);
			JsonObject table = ResourceReader.data("temper_catalyst/" + school + ".json");
			for (JsonElement entry : table.getAsJsonArray("catalysts")) {
				String item = entry.getAsJsonObject().get("item").getAsString();
				if (item.endsWith("_enzyme")) enzymeSchool.put(item, school);
			}
		}
		assertEquals(8, enzymeSchool.size(), "each school lists its own enzyme once");
		List<Path> files;
		try (Stream<Path> stream = Files.list(RepoPaths.DATA.resolve("recipe/rendering"))) {
			files = stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
		for (Path file : files) {
			JsonObject recipe = ResourceReader.data("recipe/rendering/" + file.getFileName());
			String input = recipe.getAsJsonObject("input").get("item").getAsString();
			for (JsonElement element : recipe.getAsJsonArray("results")) {
				String school = enzymeSchool.get(element.getAsJsonObject().get("id").getAsString());
				if (school == null) continue;
				JsonObject table = ResourceReader.data("temper_catalyst/" + school + ".json");
				boolean listed = false;
				for (JsonElement entry : table.getAsJsonArray("catalysts")) {
					listed |= entry.getAsJsonObject().get("item").getAsString().equals(input);
				}
				assertTrue(listed, input + " renders into the " + school + " enzyme but is not a " + school + " catalyst");
			}
		}
	}
}
