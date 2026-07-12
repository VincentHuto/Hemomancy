package com.vincenthuto.hemomancy.common.lore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettledLoreSynchronizationSourceTest {
	private SettledLoreSynchronizationSourceTest() {}

	public static void main(String[] args) throws IOException {
		String lore = read("docs/LORE_REFERENCE.md");
		String mechanics = read("docs/HEMOMANCY_REFERENCE.md");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String annettaBook = read("src/main/resources/data/hemomancy/books/fanesanguinium/cosmic_forces/pages/annetta_knowles.json");
		String bloodMoonBook = read("src/main/resources/data/hemomancy/books/fanesanguinium/cosmic_forces/pages/blood_moons.json");
		String remembrance = read("src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java");
		String remembranceRecipe = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/silthmeres_remembrance.json");
		String armor = read("src/main/java/com/vincenthuto/hemomancy/common/item/shared/armor/EnumModArmorTiers.java");
		String wiki = read("wiki/Harbinger-Path.md");

		assertContains(lore, "Blood Moons are fungal surges");
		assertContains(bloodMoonBook, "fungal surge");
		assertNotContains(bloodMoonBook, "it is Our Lady expending");

		assertContains(lore, "Tooth Pecks sought her out");
		assertNotContains(lore, "a **Chthonian**");
		assertContains(annettaBook, "Tooth Peck");
		assertNotContains(annettaBook, "Chthonian");

		assertNotContains(lore, "Fungal Podium becomes accessible (gated at Votary+)");
		assertNotContains(lore, "Apotheosis forces the Fungal Spine");

		assertContains(lore, "Silthmere is a liturgical title of Our Lady");
		assertNotContains(remembranceRecipe, "who walked the path");
		assertContains(remembrance, "EffectInit.verdigris_aura");
		assertNotContains(remembrance, "REMEMBRANCE_SILVER_WARD_DURATION");

		assertNotContains(lang, "Coat warhammer, glaive, dagger");
		assertNotContains(lang, "warhammer, glaive, dagger, pickaxe");
		assertContains(armor, "UNSTAINED(\"unstained\", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,\n\t\t\t() -> Ingredient.of(ItemInit.consecrated_copper_ingot.get()))");

		assertContains(lore, "Flexible Envelope");
		assertNotContains(lore, "5×5 chunk");
		assertNotContains(lang, "Five chunks in every direction");
		assertNotContains(mechanics, "pale silver found at the bottom of a forgotten river");

		assertNotContains(wiki, "Degree 1: Pledged");
		assertNotContains(wiki, "Degree 5: Apostle");
		assertNotContains(wiki, "Degree 6: Archon");
		assertContains(wiki, "Degree 6: Sanctified");

		assertContains(lore, "Broken Wills are remnants of failed former Harbingers");
		assertContains(lore, "Sent Wills are purpose-built by the Fungal Entity");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}

	private static void assertNotContains(String source, String forbidden) {
		if (source.contains(forbidden)) throw new AssertionError("still contains " + forbidden);
	}
}
