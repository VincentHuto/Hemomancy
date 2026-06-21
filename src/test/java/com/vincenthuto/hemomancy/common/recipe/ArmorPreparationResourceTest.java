package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArmorPreparationResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path RECIPES = RESOURCE_ROOT.resolve("data/hemomancy/recipe");
	private static final Path ARMATURE_RECIPES = RECIPES.resolve("armature_upgrade");
	private static final String[] ARMOR_SLOTS = { "helm", "chestplate", "leggings", "boots" };

	private ArmorPreparationResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertItemsRegistered();
		assertItemAssetsAndLang();
		assertMobDrops();
		assertPreparationRecipes();
		assertArmorUpgradeRecipesUsePreparations();
	}

	private static void assertItemsRegistered() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		for (String id : new String[] {
				"toxicognath",
				"fargone_proboscis",
				"queens_physogastrism",
				"cuttlefish_chromatophores",
				"sclerotic_oleum",
				"aculeate_vitriol",
				"chromatic_sublimate"
		}) {
			assertContains("item registry " + id, itemInit, " " + id + " = BASEITEMS.register(\"" + id + "\"");
		}
	}

	private static void assertItemAssetsAndLang() throws IOException {
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		assertContains("toxicognath lang", lang, "\"item.hemomancy.toxicognath\": \"Toxicognath\"");
		assertContains("fargone proboscis lang", lang,
				"\"item.hemomancy.fargone_proboscis\": \"Fargone Proboscis\"");
		assertContains("queens physogastrism lang", lang,
				"\"item.hemomancy.queens_physogastrism\": \"Queen's Physogastrism\"");
		assertContains("cuttlefish chromatophores lang", lang,
				"\"item.hemomancy.cuttlefish_chromatophores\": \"Cuttlefish Chromatophores\"");
		assertContains("sclerotic oleum lang", lang, "\"item.hemomancy.sclerotic_oleum\": \"Sclerotic Oleum\"");
		assertContains("aculeate vitriol lang", lang,
				"\"item.hemomancy.aculeate_vitriol\": \"Aculeate Vitriol\"");
		assertContains("chromatic sublimate lang", lang,
				"\"item.hemomancy.chromatic_sublimate\": \"Chromatic Sublimate\"");
		for (String id : new String[] {
				"toxicognath",
				"fargone_proboscis",
				"queens_physogastrism",
				"cuttlefish_chromatophores",
				"sclerotic_oleum",
				"aculeate_vitriol",
				"chromatic_sublimate"
		}) {
			assertContains(id + " item model", read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + id + ".json")),
					"\"layer0\": \"hemomancy:item/" + id + "\"");
			assertExists(id + " texture", RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/" + id + ".png"));
		}
	}

	private static void assertMobDrops() throws IOException {
		assertContains("centipede toxicognath drop",
				read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/venom_rib_centipede.json")),
				"\"name\": \"hemomancy:toxicognath\"");
		assertContains("fargone proboscis drop",
				read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/fargone.json")),
				"\"name\": \"hemomancy:fargone_proboscis\"");
		assertContains("queen physogastrism drop",
				read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/chthonian_queen.json")),
				"\"name\": \"hemomancy:queens_physogastrism\"");
		assertContains("cuttlefish chromatophore drop",
				read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/prism_cuttle.json")),
				"\"name\": \"hemomancy:cuttlefish_chromatophores\"");
	}

	private static void assertPreparationRecipes() throws IOException {
		assertRecipeUses("sclerotic_oleum", "hemomancy:chitinous_husk");
		assertRecipeUses("sclerotic_oleum", "hemomancy:chalybeate_sclerite");
		assertRecipeUses("sclerotic_oleum", "hemomancy:queens_physogastrism");
		assertRecipeUses("aculeate_vitriol", "hemomancy:toxicognath");
		assertRecipeUses("aculeate_vitriol", "hemomancy:fargone_proboscis");
		assertRecipeUses("aculeate_vitriol", "hemomancy:calcified_blood_spine");
		assertRecipeUses("chromatic_sublimate", "hemomancy:serpent_scale");
		assertRecipeUses("chromatic_sublimate", "hemomancy:puppeteering_thread");
		assertRecipeUses("chromatic_sublimate", "hemomancy:cuttlefish_chromatophores");
	}

	private static void assertArmorUpgradeRecipesUsePreparations() throws IOException {
		assertArmorBranch("chitinite", "sclerotic_oleum");
		assertArmorBranch("barbed", "aculeate_vitriol");
		assertArmorBranch("prismatic", "chromatic_sublimate");
	}

	private static void assertArmorBranch(String lineage, String reagent) throws IOException {
		for (String slot : ARMOR_SLOTS) {
			String recipe = read(ARMATURE_RECIPES.resolve("hematic_iron_to_" + lineage + "_" + slot + ".json"));
			assertContains(lineage + " " + slot + " should use " + reagent,
					recipe, "\"item\": \"hemomancy:" + reagent + "\"");
		}
	}

	private static void assertRecipeUses(String recipeId, String itemId) throws IOException {
		String recipe = read(RECIPES.resolve(recipeId + ".json"));
		assertContains(recipeId + " ingredient " + itemId, recipe, "\"item\": \"" + itemId + "\"");
		assertContains(recipeId + " result", recipe, "\"id\": \"hemomancy:" + recipeId + "\"");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
