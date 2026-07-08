package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class LivingWeaponGraftRecipeResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path GRAFT_RECIPES = ROOT.resolve("src/main/resources/data/hemomancy/recipe/living_weapon_graft");
	private static final Path OLD_MEMORY_RECIPES = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving");
	private static final Path RECIPE_ADVANCEMENTS = ROOT.resolve("src/main/resources/data/hemomancy/advancement/recipe/hemomancy/living_weapon_graft");

	private static final Map<String, String> CATALYSTS = Map.of(
			"blade", "hematic_iron_powder",
			"axe", "chalybeate_sclerite",
			"spear", "calcified_blood_spine",
			"claws", "chitinous_husk",
			"crossbow", "puppeteering_thread",
			"torch", "fervent_spores",
			"flail", "frigid_spores");

	private static final Map<String, String> ENZYMES = Map.of(
			"blade", "vivacious_enzyme",
			"axe", "ruinous_enzyme",
			"spear", "incandescent_enzyme",
			"claws", "umbral_enzyme",
			"crossbow", "neurotic_enzyme",
			"torch", "fervent_enzyme",
			"flail", "frigid_enzyme");

	private LivingWeaponGraftRecipeResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		for (String form : CATALYSTS.keySet()) {
			assertDirectGraftRecipe(form);
			assertRecipeUnlockAdvancement(form);
			assertOldMemoryRecipeRemoved(form);
		}
		assertNoVesperGraftRecipe();
		assertNoVesperRecipeUnlockAdvancement();
	}

	private static void assertDirectGraftRecipe(String form) throws IOException {
		String json = read(GRAFT_RECIPES.resolve(form + ".json")).replaceAll("\\s+", "");
		assertContains("recipe type for " + form, json, "\"type\":\"minecraft:crafting_shapeless\"");
		assertContains("recipe uses hematic memory for " + form, json, "\"item\":\"hemomancy:hematic_memory\"");
		assertContains("recipe uses sanguine formation for " + form, json, "\"item\":\"hemomancy:sanguine_formation\"");
		assertContains("recipe uses catalyst for " + form, json, "\"item\":\"hemomancy:" + CATALYSTS.get(form) + "\"");
		assertContains("recipe uses enzyme for " + form, json, "\"item\":\"hemomancy:" + ENZYMES.get(form) + "\"");
		assertContains("recipe outputs dynamic graft for " + form, json, "\"id\":\"hemomancy:living_weapon_graft\"");
		assertContains("recipe stores form component for " + form, json, "\"hemomancy:living_weapon_graft_data\"");
		assertContains("recipe stores specific form for " + form, json, "\"form\":\"" + form + "\"");
	}

	private static void assertRecipeUnlockAdvancement(String form) throws IOException {
		String json = read(RECIPE_ADVANCEMENTS.resolve(form + ".json")).replaceAll("\\s+", "");
		assertContains("advancement rewards recipe for " + form, json,
				"\"recipes\":[\"hemomancy:living_weapon_graft/" + form + "\"]");
		assertContains("advancement is programmatic for " + form, json, "\"trigger\":\"minecraft:impossible\"");
	}

	private static void assertOldMemoryRecipeRemoved(String form) {
		Path path = OLD_MEMORY_RECIPES.resolve("memory_living_" + form + ".json");
		if (Files.exists(path)) {
			throw new AssertionError("old survival recipe should be removed: " + path);
		}
	}

	private static void assertNoVesperGraftRecipe() {
		Path path = GRAFT_RECIPES.resolve("vesper.json");
		if (Files.exists(path)) {
			throw new AssertionError("memory_of_vesper should be absorbed directly, not crafted into a graft: " + path);
		}
	}

	private static void assertNoVesperRecipeUnlockAdvancement() {
		Path path = RECIPE_ADVANCEMENTS.resolve("vesper.json");
		if (Files.exists(path)) {
			throw new AssertionError("memory_of_vesper should not unlock a Vesper graft recipe: " + path);
		}
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + ROOT.relativize(path));
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
