package com.vincenthuto.hemomancy.compat.jei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** JEI lists every Harbinger rite under its purpose, beside the floorless Blood Structure category. */
final class JeiPurposeCategorySourceTest {
	private static final Path JEI = Path.of("src/main/java/com/vincenthuto/hemomancy/compat/jei");

	@Test
	void everyPurposeGetsItsOwnRiteCategory() throws IOException {
		String plugin = Files.readString(JEI.resolve("JEIPlugin.java"));
		String category = Files.readString(JEI.resolve("BloodStructureRecipeCategory.java"));
		assertTrue(plugin.contains("for (CardinalRitePurpose purpose : CardinalRitePurpose.values())"),
				"JEI registers one rite category per purpose");
		assertTrue(plugin.contains("new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper(), purpose)"));
		assertTrue(plugin.contains("recipe.isCataloguedUnder(purpose)"), "rites are filtered by their own purpose");
		assertTrue(plugin.contains("SuccessionRites.isRecipe"), "Succession keeps its own category");
		assertTrue(category.contains("public BloodStructureRecipeCategory(IGuiHelper guiHelper, CardinalRitePurpose purpose)"));
		assertTrue(category.contains("if (!recipe.getResult().isEmpty())"),
				"rites without a result item still lay out without an empty output ingredient");
	}

	@Test
	void ritePreviewsShowTheirSubjectThenRequiredStructureThenFloor() throws IOException {
		String recipe = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/CraftingMapRecipe.java"));
		String category = Files.readString(JEI.resolve("BloodStructureRecipeCategory.java"));
		int subject = recipe.indexOf("craft.getSubject() != null ? craft.getSubject()");
		int structure = recipe.indexOf("craft.getRequiredStructure() != null ? craft.getRequiredStructure()");
		assertTrue(subject >= 0 && structure > subject, "a rite's preview falls back to its required structure before its floor");
		assertTrue(category.contains("recipe.showsStructure()"), "a required structure is listed as blocks to build");
	}

	@Test
	void everyPurposeCategoryHasItsText() throws IOException {
		JsonObject lang = JsonParser.parseString(Files.readString(
				Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
		for (String key : List.of("hemomancy.jei.threshold_rite", "hemomancy.jei.dominion_rite",
				"hemomancy.jei.working_craft", "hemomancy.jei.communion_rite", "hemomancy.jei.craft.steps.self",
				"hemomancy.jei.craft.steps.world", "hemomancy.jei.craft.steps.bond")) {
			assertTrue(lang.has(key), "missing " + key);
		}
		for (String purpose : List.of("self", "world", "bond")) {
			String steps = lang.get("hemomancy.jei.craft.steps." + purpose).getAsString();
			assertTrue(steps.contains("its floor") && steps.contains("structure"),
					"special rites have their own floors, and many rites need a structure: " + steps);
		}
	}
}
