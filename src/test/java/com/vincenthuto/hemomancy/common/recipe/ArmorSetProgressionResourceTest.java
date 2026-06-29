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
		assertFinalBloodLustLineage("barbed", "edacious_blood_lust", "tengu_mask", "fargone_proboscis");
		assertFinalBloodLustLineage("chitinite", "sheolic_blood_lust", "lodestone_faceplate", "fervent_husk");
		assertFinalBloodLustLineage("prismatic", "phantasmal_blood_lust", "grinning_mask", "mnemonic_ambergris");
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

	private static void assertFinalBloodLustLineage(String lineage, String resultPrefix,
			String helmetReagent, String armorReagent) throws IOException {
		for (String slot : SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve(lineage + "_to_" + resultPrefix + "_" + slot + ".json"));
			assertContains(resultPrefix + " " + slot + " should require the Monolithic Armature",
					recipe, "\"required_degree\": 7");
			assertContains(resultPrefix + " " + slot + " should upgrade from its sidegrade armor",
					recipe, "\"item\": \"hemomancy:" + lineage + "_" + resultSlot(slot) + "\"");
			String reagent = slot.equals("helm") ? helmetReagent : armorReagent;
			assertContains(resultPrefix + " " + slot + " should use its final ascension reagent",
					recipe, "\"item\": \"" + itemId(reagent) + "\"");
			assertContains(resultPrefix + " " + slot + " should produce final lineage armor",
					recipe, "\"id\": \"hemomancy:" + resultPrefix + "_" + bloodLustResultSlot(slot) + "\"");
		}
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
