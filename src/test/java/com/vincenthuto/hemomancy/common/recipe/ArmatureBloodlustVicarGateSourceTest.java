package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArmatureBloodlustVicarGateSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");
	private static final Path RECIPE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/armature_upgrade");
	private static final String[] BLOODLUST_VARIANT_RECIPES = {
			"barbed_to_edacious_blood_lust_helm.json",
			"barbed_to_edacious_blood_lust_chestplate.json",
			"barbed_to_edacious_blood_lust_leggings.json",
			"barbed_to_edacious_blood_lust_boots.json",
			"chitinite_to_sheolic_blood_lust_helm.json",
			"chitinite_to_sheolic_blood_lust_chestplate.json",
			"chitinite_to_sheolic_blood_lust_leggings.json",
			"chitinite_to_sheolic_blood_lust_boots.json",
			"prismatic_to_phantasmal_blood_lust_helm.json",
			"prismatic_to_phantasmal_blood_lust_chestplate.json",
			"prismatic_to_phantasmal_blood_lust_leggings.json",
			"prismatic_to_phantasmal_blood_lust_boots.json"
	};

	private ArmatureBloodlustVicarGateSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String recipe = readJava("com/vincenthuto/hemomancy/common/recipe/ArmatureUpgradeRecipe.java");
		String serializer = readJava("com/vincenthuto/hemomancy/common/recipe/serializer/ArmatureUpgradeRecipeSerializer.java");
		String jei = readJava("com/vincenthuto/hemomancy/compat/jei/HematicArmatureRecipeCategory.java");

		assertContains("recipe stores explicit required armature tier", recipe,
				"ArmatureUpgradeRules.ArmatureTier requiredArmatureTier");
		assertContains("recipe matching uses explicit required armature tier", recipe,
				"armatureTier.id() < requiredArmatureTier.id()");
		assertContains("serializer reads required armature tier", serializer,
				"required_armature_tier");
		assertContains("serializer defaults to degree-derived tier", serializer,
				"ArmatureUpgradeRules.requiredTierForDegree(requiredDegree)");
		assertContains("JEI renders explicit required armature tier", jei,
				"recipe.getRequiredArmatureTier()");

		for (String recipeFile : BLOODLUST_VARIANT_RECIPES) {
			String json = readRecipe(recipeFile);
			assertContains(recipeFile + " requires Vicar-consecrated Armature", json,
					"\"required_armature_tier\": \"vicar_consecrated\"");
		}
	}

	private static String readJava(String relativePath) throws IOException {
		return read(JAVA_ROOT.resolve(relativePath));
	}

	private static String readRecipe(String relativePath) throws IOException {
		return read(RECIPE_ROOT.resolve(relativePath));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String haystack, String needle) {
		if (!haystack.contains(needle)) {
			throw new AssertionError(label + ": missing " + needle);
		}
	}
}
