package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VesperStaffUpgradeRecipeResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private VesperStaffUpgradeRecipeResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String recipe = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/recipe/VesperStaffUpgradeRecipe.java"));
		String recipeInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/RecipeInit.java"));
		String json = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/vesper_staff_upgrade.json"));

		assertContains("recipe matches exactly one staff and one memory", recipe,
				"VesperStaffUpgradeRules.matchesUpgrade(counts.staffCount(), counts.memoryCount(), counts.otherCount())");
		assertContains("recipe preserves existing staff data by copying the stack", recipe,
				"ItemStack result = staff.copy();");
		assertContains("recipe outputs a single staff", recipe,
				"result.setCount(1);");
		assertContains("recipe adds the Vesper awakened marker to copied staff data", recipe,
				"staffData.putBoolean(LivingStaffFocusRules.VESPER_MEMORY_AWAKENED_KEY, true);");
		assertContains("serializer is registered", recipeInit,
				"vesper_staff_upgrade_serializer");
		assertContains("recipe JSON points at the custom serializer", json,
				"\"type\": \"hemomancy:vesper_staff_upgrade\"");
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
