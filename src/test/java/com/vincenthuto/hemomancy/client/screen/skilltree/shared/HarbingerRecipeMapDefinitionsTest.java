package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerRecipeMapDefinitionsTest {
	@Test
	void everyBuiltInHarbingerEntryHasAnAuthoredFamily() {
		assertEquals(37, HarbingerRecipeMapDefinitions.ritePaths().size());
		assertEquals(22, HarbingerRecipeMapDefinitions.craftingPaths().size());
		assertEquals(4, HarbingerRecipeMapDefinitions.puppetryPaths().size());
		assertEquals("Order", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/sanguine_initiation"));
		assertEquals("Qliphoth/Forbidden", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/bloom_of_qliphoth"));
		assertEquals("Foundations", HarbingerRecipeMapDefinitions.craftingFamily("blood_structure/living_staff"));
		assertEquals("Puppetry", HarbingerRecipeMapDefinitions.riteFamily(
				"cardinal_rite/puppeteer_trial_gorebound_hulk"));
	}

	@Test
	void progressionAndConceptualLinksUseDifferentKinds() {
		assertTrue(HarbingerRecipeMapDefinitions.riteLinks().stream().anyMatch(link ->
				link.kind() == RecipeMapLink.Kind.PROGRESSION
						&& link.from().id().getPath().endsWith("sanguine_initiation")
						&& link.to().id().getPath().endsWith("votary_rite")));
		assertTrue(HarbingerRecipeMapDefinitions.riteLinks().stream().anyMatch(link ->
				link.kind() == RecipeMapLink.Kind.CONCEPTUAL));
		assertTrue(HarbingerRecipeMapDefinitions.craftingLinks().stream().anyMatch(link ->
				link.kind() == RecipeMapLink.Kind.CONCEPTUAL));
	}

	@Test
	void missingDisplayOverridesPreserveRecipeResultFallbacks() {
		assertEquals("recipe result", HarbingerRecipeMapDefinitions.resolveDisplayOverride(
				null, "recipe result"));
		assertEquals("authored item", HarbingerRecipeMapDefinitions.resolveDisplayOverride(
				() -> "authored item", "recipe result"));
	}
}
