package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArmorSetProgressionResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path ARMATURE_RECIPES = RESOURCE_ROOT.resolve("data/hemomancy/recipe/armature_upgrade");
	private static final String[] SLOTS = {"helm", "chestplate", "leggings", "boots"};

	private ArmorSetProgressionResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertSidegradeBranch("barbed", "aculeate_vitriol");
		assertSidegradeBranch("chitinite", "sclerotic_oleum");
		assertSidegradeBranch("prismatic", "chromatic_sublimate");
		assertBloodLustLineage("barbed");
		assertBloodLustLineage("chitinite");
		assertBloodLustLineage("prismatic");
		assertMaskLineage("tengu_mask", "barbed");
		assertMaskLineage("lodestone_faceplate", "chitinite");
		assertMaskLineage("grinning_mask", "prismatic");
		assertUniversalVelorumMask();
		assertFinalBloodLustChoice("edacious_blood_lust", "fargone_proboscis");
		assertFinalBloodLustChoice("sheolic_blood_lust", "fervent_husk");
		assertFinalBloodLustChoice("phantasmal_blood_lust", "mnemonic_ambergris");
		assertSilentArchonAcceptsEveryBloodLustSet();
		assertNoDirectForkToFinalRecipes();
	}

	private static void assertSidegradeBranch(String lineage, String reagent) throws IOException {
		for (String slot : SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve("hematic_iron_to_" + lineage + "_" + slot + ".json"));
			assertContains(lineage + " " + slot + " should require Degree 3",
					recipe, "\"required_degree\": 3");
			assertContains(lineage + " " + slot + " should upgrade Hematic Iron",
					recipe, "\"item\": \"hemomancy:hematic_iron_" + slot + "\"");
			assertContains(lineage + " " + slot + " should use its ecological reagent",
					recipe, "\"item\": \"" + itemId(reagent) + "\"");
			assertContains(lineage + " " + slot + " should produce the matching sidegrade",
					recipe, "\"id\": \"hemomancy:" + lineage + "_" + resultSlot(slot) + "\"");
		}
	}

	private static void assertBloodLustLineage(String lineage) throws IOException {
		for (String slot : SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve(lineage + "_to_blood_lust_" + slot + ".json"));
			assertContains(lineage + " " + slot + " should require the Vicar-upgraded Armature",
					recipe, "\"required_degree\": 5");
			assertContains(lineage + " " + slot + " should upgrade from its sidegrade armor",
					recipe, "\"item\": \"hemomancy:" + lineage + "_" + resultSlot(slot) + "\"");
			assertContains(lineage + " " + slot + " should use Crimson Lacquer",
					recipe, "\"item\": \"hemomancy:crimson_lacquer\"");
			assertContains(lineage + " " + slot + " should carry Blood Lust lineage data",
					recipe, "\"hemomancy:lineage\": \"" + lineage + "\"");
		}
	}

	private static void assertMaskLineage(String maskRecipe, String lineage) throws IOException {
		String recipe = read(ARMATURE_RECIPES.resolve("blood_lust_" + maskRecipe + ".json"));
		assertContains(maskRecipe + " should require its matching Blood Lust lineage", recipe,
				"\"hemomancy:lineage\": \"" + lineage + "\"");
	}

	private static void assertUniversalVelorumMask() throws IOException {
		String recipe = read(ARMATURE_RECIPES.resolve("blood_lust_velorum_mask.json"));
		if (recipe.contains("required_base_data")) {
			throw new AssertionError("Velorum should remain universal across Blood Lust lineages");
		}
	}

	private static void assertFinalBloodLustChoice(String resultPrefix, String reagent) throws IOException {
		for (String slot : SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve("blood_lust_to_" + resultPrefix + "_" + slot + ".json"));
			assertContains(resultPrefix + " " + slot + " should require the Monolithic Armature",
					recipe, "\"required_armature_tier\": \"monolithic\"");
			assertContains(resultPrefix + " " + slot + " should upgrade from Blood Lust",
					recipe, "\"item\": \"hemomancy:blood_lust_" + bloodLustBaseSlot(slot) + "\"");
			assertContains(resultPrefix + " " + slot + " should use one final ascension material",
					recipe, "\"item\": \"" + itemId(reagent) + "\"");
			assertContains(resultPrefix + " " + slot + " should produce final lineage armor",
					recipe, "\"id\": \"hemomancy:" + resultPrefix + "_" + bloodLustResultSlot(slot) + "\"");
		}
	}

	private static void assertNoDirectForkToFinalRecipes() {
		for (String oldPrefix : new String[] {"barbed_to_edacious_blood_lust",
				"chitinite_to_sheolic_blood_lust", "prismatic_to_phantasmal_blood_lust"}) {
			for (String slot : SLOTS) {
				if (Files.exists(ARMATURE_RECIPES.resolve(oldPrefix + "_" + slot + ".json"))) {
					throw new AssertionError("obsolete direct fork-to-final recipe remains: " + oldPrefix + "_" + slot);
				}
			}
		}
	}

	private static void assertSilentArchonAcceptsEveryBloodLustSet() throws IOException {
		for (String slot : SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve("blood_lust_to_silent_archon_" + slot + ".json"));
			String piece = bloodLustBaseSlot(slot);
			assertContains("Silent Archon " + slot + " should still accept Blood Lust", recipe,
					"\"item\": \"hemomancy:blood_lust_" + piece + "\"");
			for (String finalSet : new String[] {"edacious", "sheolic", "phantasmal"}) {
				assertContains("Silent Archon " + slot + " should accept " + finalSet + " Blood Lust", recipe,
						"\"item\": \"hemomancy:" + finalSet + "_blood_lust_" + piece + "\"");
			}
		}
	}

	private static String bloodLustBaseSlot(String slot) {
		return switch (slot) {
			case "chestplate" -> "chest";
			case "leggings" -> "legs";
			case "boots" -> "boots";
			default -> "helm";
		};
	}

	private static String bloodLustResultSlot(String slot) {
		return switch (slot) {
			case "chestplate" -> "chest";
			case "leggings" -> "legs";
			case "boots" -> "boots";
			default -> "helm";
		};
	}

	private static String resultSlot(String slot) {
		return switch (slot) {
			case "chestplate" -> "chestplate";
			case "leggings" -> "leggings";
			case "boots" -> "boots";
			default -> "helm";
		};
	}

	private static String itemId(String item) {
		return item.contains(":") ? item : "hemomancy:" + item;
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
