package com.vincenthuto.hemomancy.compat.jei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponGraftJeiIntegrationTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private LivingWeaponGraftJeiIntegrationTest() {
	}

	public static void main(String[] args) throws IOException {
		String plugin = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/compat/jei/JEIPlugin.java"));
		String recipe = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/compat/jei/LivingWeaponGraftJeiRecipe.java"));
		String category = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/compat/jei/LivingWeaponGraftRecipeCategory.java"));
		String armatureCategory = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/compat/jei/HematicArmatureRecipeCategory.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("JEI plugin exposes living weapon graft recipe type", plugin,
				"RecipeType<LivingWeaponGraftJeiRecipe> living_weapon_graft_type");
		assertContains("JEI plugin registers living weapon graft category", plugin,
				"new LivingWeaponGraftRecipeCategory");
		assertContains("JEI plugin registers living weapon graft recipes", plugin,
				"LivingWeaponGraftJeiRecipe.all()");
		assertContains("JEI plugin makes iron brazier a catalyst", plugin,
				"new ItemStack(BlockInit.iron_brazier.get()), living_weapon_graft_type");
		assertContains("JEI plugin makes living staff a catalyst", plugin,
				"new ItemStack(ItemInit.living_staff.get()), living_weapon_graft_type");

		assertContains("JEI recipe stores form", recipe, "LivingWeaponForm form");
		assertContains("JEI recipe creates graft stack", recipe, "LivingWeaponGraftData.createStack(form)");
		assertContains("JEI recipe includes living staff input", recipe, "new ItemStack(ItemInit.living_staff.get())");
		assertContains("JEI recipe includes iron brazier input", recipe, "new ItemStack(BlockInit.iron_brazier.get())");
		assertContains("JEI recipe maps blade output", recipe, "case BLADE -> ItemInit.living_blade");
		assertContains("JEI recipe maps claws output", recipe, "case CLAWS -> ItemInit.living_baghnakh");
		assertContains("JEI recipe creates one display for each form", recipe, "Arrays.stream(LivingWeaponForm.values())");

		assertContains("JEI category renders living weapon recipes", category,
				"implements IRecipeCategory<LivingWeaponGraftJeiRecipe>");
		assertContains("JEI category uses plugin recipe type", category,
				"return JEIPlugin.living_weapon_graft_type;");
		assertContains("JEI category adds living staff input", category,
				"recipe.livingStaff()");
		assertContains("JEI category adds iron brazier input", category,
				"recipe.ironBrazier()");
		assertContains("JEI category adds graft input", category,
				"recipe.graft()");
		assertContains("JEI category adds living weapon output", category,
				"recipe.output()");
		assertContains("JEI category explains blood absorption", category,
				"hemomancy.jei.living_weapon_graft.hint");
		assertContains("JEI category places graft above brazier on the same x coordinate", category,
				"private static final int GRAFT_X = BRAZIER_X;");
		assertContains("JEI category uses independent graft y coordinate", category,
				"private static final int GRAFT_Y");
		assertContains("JEI category puts graft slot above brazier slot", category,
				"RecipeIngredientRole.INPUT, GRAFT_X + 1, GRAFT_Y + 1");
		assertContains("JEI category uses the shared animated progress arrow", category,
				"JeiProgressArrow.draw(");

		assertContains("Hematic Armature uses the shared animated progress arrow", armatureCategory,
				"JeiProgressArrow.draw(");

		assertContains("language names living weapon graft JEI category", lang,
				"\"hemomancy.jei.living_weapon_graft\": \"Living Weapon Grafts\"");
		assertContains("language uses a short living weapon graft hint", lang,
				"\"hemomancy.jei.living_weapon_graft.hint\": \"Brazier Blood Absorption\"");
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
