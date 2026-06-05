package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnzymeBrewingRecipeSourceTest {
	private static final Path EFFECT_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java");

	private EnzymeBrewingRecipeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(EFFECT_INIT).replace("\r\n", "\n");
		assertContains("uses NeoForge brewing registration event", source, "RegisterBrewingRecipesEvent");
		assertContains("registers through the NeoForge brewing event", source, "registerEnzymeBrewingRecipes(RegisterBrewingRecipesEvent event)");
		assertEnzymeMix(source, "vivacious_enzyme", "Potions.REGENERATION");
		assertEnzymeMix(source, "fervent_enzyme", "Potions.FIRE_RESISTANCE");
		assertEnzymeMix(source, "neurotic_enzyme", "Potions.SWIFTNESS");
		assertEnzymeMix(source, "incandescent_enzyme", "Potions.NIGHT_VISION");
		assertEnzymeMix(source, "ruinous_enzyme", "Potions.POISON");
		assertEnzymeMix(source, "frigid_enzyme", "Potions.SLOWNESS");
		assertEnzymeMix(source, "ferric_enzyme", "Potions.STRENGTH");
		assertEnzymeMix(source, "umbral_enzyme", "Potions.INVISIBILITY");
	}

	private static void assertEnzymeMix(String source, String enzymeId, String potionTarget) {
		assertContains(enzymeId + " is used as brewing ingredient", source, "ItemInit." + enzymeId + ".get()");
		assertContains(enzymeId + " maps to " + potionTarget, source, potionTarget);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
