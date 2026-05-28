package com.vincenthuto.hemomancy.compat.jei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicArmatureJeiIntegrationTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HematicArmatureJeiIntegrationTest() {
	}

	public static void main(String[] args) throws IOException {
		String plugin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/compat/jei/JEIPlugin.java"));
		String category = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/compat/jei/HematicArmatureRecipeCategory.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("JEI plugin should expose an Armature recipe type", plugin,
				"RecipeType<ArmatureUpgradeRecipe> armature_upgrade_type");
		assertContains("JEI plugin should use the armature upgrade recipe id", plugin,
				"\"armature_upgrade\", ArmatureUpgradeRecipe.class");
		assertContains("JEI plugin should register the Armature category", plugin,
				"new HematicArmatureRecipeCategory");
		assertContains("JEI plugin should make the Armature block a catalyst", plugin,
				"new ItemStack(BlockInit.hematic_armature.get()), armature_upgrade_type");
		assertContains("JEI plugin should load Armature recipes from the recipe manager", plugin,
				"ArmatureUpgradeRecipe.getAllRecipes(world)");

		assertContains("Armature category should render Armature recipes", category,
				"implements IRecipeCategory<ArmatureUpgradeRecipe>");
		assertContains("Armature category should use the JEI recipe type", category,
				"return JEIPlugin.armature_upgrade_type;");
		assertContains("Armature category should add valid worn armor bases", category,
				"recipe.getValidBase().getItems()");
		assertContains("Armature category should add bowl reagents", category,
				"recipe.getReagent().getItems()");
		assertContains("Armature category should add the upgraded result", category,
				"recipeResult(recipe)");
		assertContains("Armature category should show the armor slot", category,
				"recipe.getArmorSlot().name()");
		assertContains("Armature category should show the degree gate", category,
				"recipe.getRequiredDegree()");
		assertContains("Armature category should show the blood cost", category,
				"recipe.getBloodCost()");

		assertContains("language should name the Armature JEI category", lang,
				"\"hemomancy.jei.hematic_armature\": \"Hematic Armature\"");
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
