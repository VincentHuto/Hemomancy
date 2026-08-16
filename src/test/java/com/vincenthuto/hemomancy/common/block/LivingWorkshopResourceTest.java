package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWorkshopResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");
	private static final Path SOURCES = Path.of("src/main/java");

	private LivingWorkshopResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String lantern = readResource("data/hemomancy/recipe/blood_structure/mycelial_lantern.json");
		String tap = readResource("data/hemomancy/recipe/gourdvine_tap.json");
		String salve = readResource("data/hemomancy/recipe/sanguine_salve.json");
		String poultice = readResource("data/hemomancy/recipe/vascular_poultice.json");
		String language = readResource("assets/hemomancy/lang/en_us.json");
		String atlas = Files.readString(SOURCES.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java"));
		String lanternMenu = Files.readString(SOURCES.resolve(
				"com/vincenthuto/hemomancy/common/menu/tile/crafting/MycelialLanternMenu.java"));

		assertContains("Lantern authoritative Degree-3 gate", lantern, "\"required_degree\": 3");
		assertContains("Atlas Degree-3 gate", atlas, "entryAt(\"mycelial_lantern\", h, \"spores_cultures\", d(3)");
		assertContains("Tap uses Hemomancy flora", tap, "hemomancy:infested_wood");
		assertContains("Tap uses field reagent", tap, "hemomancy:foul_paste");
		assertContains("Salve uses Bleeding Bulb", salve, "hemomancy:bleeding_bulb");
		assertContains("Poultice no longer duplicates the Salve", poultice, "\"count\": 1");
		assertContains("all brewing outcomes are taught", language, "Umbral turns Night Vision into Invisibility");
		assertContains("First Culture guidance names cost", language, "600 blood over 2,400 ticks");
		assertContains("First Culture requires a recorded tendency", lanternMenu, "hasRecordedEnzyme(serverPlayer, stack)");
	}

	private static String readResource(String relative) throws IOException {
		return Files.readString(RESOURCES.resolve(relative));
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) throw new AssertionError(label + " missing: " + needle);
	}
}
