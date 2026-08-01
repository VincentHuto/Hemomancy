package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class PuppeteerCardinalOrdealResourceTest {
	private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe");
	private static final List<String> RITES = List.of("veinwing_vulture", "marrow_spitter",
			"gorebound_hulk", "mnemonist_puppet");
	private static final List<String> COMPONENTS = List.of("veinwing_harness", "marrow_spitter_carriage",
			"gorebound_yoke", "mnemonist_cradle");

	@Test
	void fourUniqueSignaturesShareDominionLesserAndPreserveCrossbar() throws Exception {
		Set<String> signatures = new HashSet<>();
		for (String summon : RITES) {
			JsonObject json = json(RECIPES.resolve("cardinal_rite/puppeteer_trial_" + summon + ".json"));
			assertEquals("hemomancy:dominion_lesser", json.get("floor").getAsString());
			assertFalse(json.get("consume_medium_on_success").getAsBoolean());
			assertEquals("hemomancy:marionette_crossbar",
					json.getAsJsonObject("medium").get("item").getAsString());
			assertEquals(summon, json.getAsJsonObject("puppeteer_trial").get("summon").getAsString());
			assertEquals("living_staff", json.getAsJsonObject("ceremony").get("focus").getAsString());
			assertTrue(signatures.add(json.getAsJsonArray("brazier_signature").toString()));
		}
	}

	@Test
	void componentsHaveCraftingModelsTexturesAndNames() throws Exception {
		String language = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"));
		for (String component : COMPONENTS) {
			assertTrue(Files.isRegularFile(RECIPES.resolve(component + ".json")));
			assertTrue(Files.isRegularFile(Path.of("src/main/resources/assets/hemomancy/models/item",
					component + ".json")));
			var image = ImageIO.read(Path.of("src/main/resources/assets/hemomancy/textures/item",
					component + ".png").toFile());
			assertNotNull(image);
			assertEquals(16, image.getWidth());
			assertEquals(16, image.getHeight());
			assertTrue(language.contains("item.hemomancy." + component));
		}
	}

	@Test
	void legacyBloodCraftingTrialContentIsRetired() throws Exception {
		Path legacy = RECIPES.resolve("puppeteer_trial");
		assertTrue(!Files.exists(legacy) || Files.list(legacy).findAny().isEmpty());
		assertFalse(Files.exists(Path.of("src/main/java/com/vincenthuto/hemomancy/common/recipe/PuppeteerTrialRecipe.java")));
		assertFalse(Files.exists(Path.of("src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/PuppeteerTrialRecipeSerializer.java")));
		String parser = Files.readString(Path.of("tools/skill_tree_editor/src/server/recipeMapParser.ts"));
		assertFalse(parser.contains("registerPuppetry"));
		assertFalse(parser.contains("puppeteer_trial/"));
		String language = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"));
		assertFalse(language.contains("hemomancy.summon.trial.degree_locked"));
		assertFalse(language.contains("hemomancy.summon.trial.needs_catalyst"));
		String reference = Files.readString(Path.of("docs/HEMOMANCY_REFERENCE.md"));
		assertFalse(reference.contains("puppeteer trial Blood Crafting recipes"));
	}

	private static JsonObject json(Path path) throws Exception {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
