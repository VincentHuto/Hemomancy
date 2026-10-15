package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerRecipeMapDefinitionsTest {
	@Test
	void everyBuiltInHarbingerEntryHasAnAuthoredFamily() {
		assertEquals(52, HarbingerRecipeMapDefinitions.ritePaths().size());
		// 8 floorless Blood Structures and 20 Working crafts.
		assertEquals(28, HarbingerRecipeMapDefinitions.craftingPaths().size());
		assertEquals(4, HarbingerRecipeMapDefinitions.puppetryPaths().size());
		assertEquals("Order", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/initiate_rite"));
		assertEquals("Qliphoth/Forbidden", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/bloom_of_qliphoth"));
		assertEquals("Foundations", HarbingerRecipeMapDefinitions.craftingFamily("blood_structure/living_staff"));
		assertEquals("Puppetry", HarbingerRecipeMapDefinitions.riteFamily(
				"cardinal_rite/puppeteer_trial_gorebound_hulk"));
	}

	@Test
	void miscellaneousIsSplitIntoNamedFamilies() {
		for (String upgrade : List.of("first_condensation", "sanguine_athanor", "steady_separation", "second_fraction",
				"true_groove", "enduring_pattern", "armature_consecration", "monolithic_armature",
				"eightfold_script", "palimpsest", "ganglionic_weave", "cortical_weave")) {
			assertEquals("Station Upgrades", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/" + upgrade), upgrade);
		}
		for (String trial : List.of("cinder_bellows", "sanguine_hound", "scarlet_mummer")) {
			assertEquals("Trials", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/puppeteer_trial_" + trial), trial);
		}
		assertEquals("Bloodline/Fane", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/hematic_succession"));
		assertEquals("Bloodline/Fane", HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/mnemonic_restoration"));
		for (String station : List.of("enzymatic_scriptorium", "resonant_forge", "scarlet_vanity", "warp_chair",
				"fungal_implantation_pylon")) {
			assertEquals("Stations", HarbingerRecipeMapDefinitions.craftingFamily("blood_structure/" + station), station);
		}
		assertEquals("Constructs/Effigies", HarbingerRecipeMapDefinitions.craftingFamily("blood_structure/vacant_effigy"));
		assertEquals("Ritual Infrastructure", HarbingerRecipeMapDefinitions.craftingFamily("blood_structure/hematic_stake"));
		assertEquals(RecipeMapLayout.MISC_FAMILY, HarbingerRecipeMapDefinitions.riteFamily("cardinal_rite/addon_rite"),
				"unregistered rites still fall back to Miscellaneous");
	}

	@Test
	void displayLinksPointOutwardWithoutDuplicates() {
		assertFalse(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "pallid_vessel_rite", "crimson_vessel_rite",
				RecipeMapLink.Kind.CONCEPTUAL), "the conceptual copy of the Vessel progression link is gone");
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "pallid_vessel_rite", "crimson_vessel_rite",
				RecipeMapLink.Kind.PROGRESSION));
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "rooted_vein", "exsanguination",
				RecipeMapLink.Kind.CONCEPTUAL));
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "puppeteer_trial_marrow_spitter",
				"puppeteer_trial_gorebound_hulk", RecipeMapLink.Kind.CONCEPTUAL));
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "puppeteer_trial_veinwing_vulture",
				"puppeteer_trial_gorebound_hulk", RecipeMapLink.Kind.CONCEPTUAL));
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.riteLinks(), "initiate_rite", "sanguine_brotherhood",
				RecipeMapLink.Kind.CONCEPTUAL), "the D2 ceremony does not grant D3");
		assertTrue(hasLink(HarbingerRecipeMapDefinitions.craftingLinks(), "sanguine_monolith", "covenant_throne",
				RecipeMapLink.Kind.PROGRESSION));
		Set<String> seen = new HashSet<>();
		for (RecipeMapLink link : HarbingerRecipeMapDefinitions.riteLinks()) {
			assertTrue(seen.add(link.from().id() + ">" + link.to().id()), "duplicate link " + link);
		}
	}

	private static boolean hasLink(List<RecipeMapLink> links, String from, String to, RecipeMapLink.Kind kind) {
		return links.stream().anyMatch(link -> link.kind() == kind
				&& link.from().id().getPath().endsWith("/" + from) && link.to().id().getPath().endsWith("/" + to));
	}

	@Test
	void progressionAndConceptualLinksUseDifferentKinds() {
		assertTrue(HarbingerRecipeMapDefinitions.riteLinks().stream().anyMatch(link ->
				link.kind() == RecipeMapLink.Kind.PROGRESSION
						&& link.from().id().getPath().endsWith("sanguine_brotherhood")
						&& link.to().id().getPath().endsWith("illuminatus_rite")));
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
