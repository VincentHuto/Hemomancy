package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class ArmatureBloodlustVicarGateSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");
	private static final Path RECIPE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/armature_upgrade");
	private static final String[] BLOODLUST_VARIANT_RECIPES = {
			"blood_lust_to_edacious_blood_lust_helm.json",
			"blood_lust_to_edacious_blood_lust_chestplate.json",
			"blood_lust_to_edacious_blood_lust_leggings.json",
			"blood_lust_to_edacious_blood_lust_boots.json",
			"blood_lust_to_sheolic_blood_lust_helm.json",
			"blood_lust_to_sheolic_blood_lust_chestplate.json",
			"blood_lust_to_sheolic_blood_lust_leggings.json",
			"blood_lust_to_sheolic_blood_lust_boots.json",
			"blood_lust_to_phantasmal_blood_lust_helm.json",
			"blood_lust_to_phantasmal_blood_lust_chestplate.json",
			"blood_lust_to_phantasmal_blood_lust_leggings.json",
			"blood_lust_to_phantasmal_blood_lust_boots.json"
	};

	private ArmatureBloodlustVicarGateSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String recipe = readJava("com/vincenthuto/hemomancy/common/recipe/ArmatureUpgradeRecipe.java");
		String serializer = readJava("com/vincenthuto/hemomancy/common/recipe/serializer/ArmatureUpgradeRecipeSerializer.java");
		String jei = readJava("com/vincenthuto/hemomancy/compat/jei/HematicArmatureRecipeCategory.java");

		assertContains("recipe stores explicit required armature tier", recipe,
				"ArmatureUpgradeRules.ArmatureTier requiredArmatureTier");
		assertContains("recipe matching uses explicit required armature tier", recipe,
				"armatureTier.id() < requiredArmatureTier.id()");
		assertContains("serializer reads required armature tier", serializer,
				"required_armature_tier");
		assertContains("serializer supports lineage-gated base data", serializer,
				"required_base_data");
		assertContains("serializer defaults to degree-derived tier", serializer,
				"ArmatureUpgradeRules.requiredTierForDegree(requiredDegree)");
		assertContains("JEI renders explicit required armature tier", jei,
				"recipe.getRequiredArmatureTier()");

		for (String recipeFile : BLOODLUST_VARIANT_RECIPES) {
			String json = readRecipe(recipeFile);
			assertContains(recipeFile + " requires Monolithic Armature", json,
					"\"required_armature_tier\": \"monolithic\"");
		}
	}

	private static String readJava(String relativePath) throws IOException {
		return read(JAVA_ROOT.resolve(relativePath));
	}

	private static String readRecipe(String relativePath) throws IOException {
		return read(RECIPE_ROOT.resolve(relativePath));
	}
}
