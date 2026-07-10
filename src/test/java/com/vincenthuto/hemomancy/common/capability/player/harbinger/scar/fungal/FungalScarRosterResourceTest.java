package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class FungalScarRosterResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path MAIN = ROOT.resolve("src/main/java");
	private static final Path RESOURCES = ROOT.resolve("src/main/resources");
	private static final Path RECIPES = RESOURCES.resolve("data/hemomancy/recipe/fungal_scar");
	private static final Path MODELS = RESOURCES.resolve("assets/hemomancy/models/item");
	private static final Path TEXTURES = RESOURCES.resolve("assets/hemomancy/textures/item");
	private static final Path LANG = RESOURCES.resolve("assets/hemomancy/lang/en_us.json");
	private static final List<String> ACTIVE_SCARS = List.of(
			"rhizovitta_communis",
			"talaromyces_minus",
			"noctifly_agaric",
			"antiphonomyces_resonans",
			"putrivora_resolvens",
			"oculiflora_reticularis",
			"cryostroma_perdurans",
			"saprovitta_vestigium");
	private static final Set<String> REMOVED_SCARS = Set.of(
			"respergillus",
			"lumina_devorans",
			"anastocordyceps_nexus",
			"thanomyces_resurgens",
			"sanguiflora_cadens");

	private FungalScarRosterResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		registriesAndCultivationRecipesExposeExactlyEightFungalScars();
		activeScarsHaveAssetsAndLang();
		removedScarsDoNotRemainInRegistriesResourcesOrCatalogues();
	}

	private static void registriesAndCultivationRecipesExposeExactlyEightFungalScars() throws IOException {
		String itemInit = read(MAIN.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String scarInit = read(MAIN.resolve("com/vincenthuto/hemomancy/common/init/ScarInit.java"));

		for (String id : ACTIVE_SCARS) {
			assertContains("item registry " + id, itemInit, " " + id + " = BASEITEMS.register(\"" + id + "\"");
			assertContains("scar definition " + id, scarInit, "reg(\"" + id + "\"");
			assertExists("cultivation recipe " + id, RECIPES.resolve(id + ".json"));
			assertContains("recipe result " + id, read(RECIPES.resolve(id + ".json")),
					"\"id\": \"hemomancy:" + id + "\"");
		}

		try (Stream<Path> recipes = Files.list(RECIPES)) {
			long count = recipes.filter(path -> path.toString().endsWith(".json")).count();
			assertEquals("fungal scar recipe count", ACTIVE_SCARS.size(), count);
		}
	}

	private static void activeScarsHaveAssetsAndLang() throws IOException {
		String lang = read(LANG);
		for (String id : ACTIVE_SCARS) {
			assertExists("item model " + id, MODELS.resolve(id + ".json"));
			assertExists("item texture " + id, TEXTURES.resolve(id + ".png"));
			assertContains("item lang " + id, lang, "\"item.hemomancy." + id + "\"");
			assertContains("immature scar lang " + id, lang, "\"item.hemomancy.immature_scar." + id + "\"");
		}
	}

	private static void removedScarsDoNotRemainInRegistriesResourcesOrCatalogues() throws IOException {
		String itemInit = read(MAIN.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String scarInit = read(MAIN.resolve("com/vincenthuto/hemomancy/common/init/ScarInit.java"));
		String events = read(MAIN.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java"));
		String materialsData = read(MAIN.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java"));
		String materialsSpec = read(MAIN.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java"));
		String lore = read(MAIN.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarLoreData.java"));
		String unlocks = read(MAIN.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarUnlockRegistry.java"));
		String lang = read(LANG);

		for (String id : REMOVED_SCARS) {
			assertDoesNotContain("item registry retired " + id, itemInit, "BASEITEMS.register(\"" + id + "\"");
			assertDoesNotContain("scar definition retired " + id, scarInit, "reg(\"" + id + "\"");
			assertDoesNotContain("behavior hook retired " + id, events, id);
			assertDoesNotContain("materials data retired " + id, materialsData, id);
			assertDoesNotContain("materials atlas retired " + id, materialsSpec, id);
			assertDoesNotContain("scar lore retired " + id, lore, id);
			assertDoesNotContain("scar unlock retired " + id, unlocks, id);
			assertDoesNotContain("lang retired " + id, lang, "\"item.hemomancy." + id + "\"");
			assertDoesNotContain("immature lang retired " + id, lang, "\"item.hemomancy.immature_scar." + id + "\"");
			assertMissing("retired recipe " + id, RECIPES.resolve(id + ".json"));
			assertMissing("retired model " + id, MODELS.resolve(id + ".json"));
		}
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static void assertEquals(String label, long expected, long actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
