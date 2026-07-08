package com.vincenthuto.hemomancy.compat.jei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JeiProgressArrowSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/compat/jei");
	private static final String[] ARROW_CATEGORIES = {
			"DistillationRecipeCategory.java",
			"EnzymeFruitingRecipeCategory.java",
			"IncubatorRecipeCategory.java",
			"MycelialCrucibleRecipeCategory.java",
			"BloodStructureRecipeCategory.java",
			"ScarStationRecipeCategory.java",
			"HematicArmatureRecipeCategory.java",
			"LivingWeaponGraftRecipeCategory.java",
			"MorphicNectarRecipeCategory.java",
			"WhiteHumorPurificationRecipeCategory.java"
	};

	private JeiProgressArrowSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read("JeiProgressArrow.java");
		assertContains("shared arrow helper owns trail width", helper, "float trail = 0.34f;");
		assertContains("shared arrow helper owns animated head columns", helper,
				"drawArrowHeadColumn(gfx, x, tipX, y, color)");
		assertContains("shared arrow helper computes intensity from trail", helper,
				"float intensity = 1f - dist / trail;");

		for (String category : ARROW_CATEGORIES) {
			String source = read(category);
			assertContains(category + " uses shared progress arrow helper", source,
					"JeiProgressArrow.draw(");
		}

		assertNotContains("Distillation should not keep old JEI drawable arrow cache", read("DistillationRecipeCategory.java"),
				"IDrawableAnimated");
		assertNotContains("Mycelial Crucible should not keep old static arrow helper", read("MycelialCrucibleRecipeCategory.java"),
				"private void drawArrow(GuiGraphics gfx, int x1, int y1, int x2, int y2)");
	}

	private static String read(String fileName) throws IOException {
		Path path = SOURCE_ROOT.resolve(fileName);
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

	private static void assertNotContains(String label, String haystack, String needle) {
		if (haystack.contains(needle)) {
			throw new AssertionError(label + ": should not contain " + needle);
		}
	}
}
