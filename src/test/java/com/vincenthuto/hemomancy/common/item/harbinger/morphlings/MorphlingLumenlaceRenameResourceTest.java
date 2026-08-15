package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class MorphlingLumenlaceRenameResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOCS_ROOT = Path.of("docs");
	private static final Path MORPHLING_PACKAGE = SOURCE_ROOT.resolve(
			"com/vincenthuto/hemomancy/common/item/harbinger/morphlings");
	private static final Path LUMENLACE_ITEM = MORPHLING_PACKAGE.resolve("LumenlaceMorphlingItem.java");
	private static final Path LUMENLACE_RULES = MORPHLING_PACKAGE.resolve("LumenlaceCamouflageRules.java");
	private static final Path LEGACY_MIGRATION = MORPHLING_PACKAGE.resolve("MorphlingMigrationRules.java");

	private MorphlingLumenlaceRenameResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		activeItemAndHelperUseLumenlaceNames();
		activeRegistrationsAndResourcesUseLumenlaceIds();
		activeSourceAndDocumentationContainNoFoxfireName();
		legacyFoxfireIdsOnlyRemainAsExplicitMigrationInputs();
	}

	private static void activeItemAndHelperUseLumenlaceNames() throws IOException {
		assertExists("Lumenlace item class", LUMENLACE_ITEM);
		assertMissing("Foxfire item class", MORPHLING_PACKAGE.resolve("FoxfireMorphlingItem.java"));
		assertExists("Lumenlace camouflage rules", LUMENLACE_RULES);
		assertMissing("Foxfire camouflage rules", MORPHLING_PACKAGE.resolve("FoxfireCamouflageRules.java"));

		String item = read(LUMENLACE_ITEM);
		assertContains("Lumenlace item class declaration", item, "class LumenlaceMorphlingItem");
		assertContains("Lumenlace binomial key", item, "morphling.hemomancy.lumenlace.binomial");
		assertContains("Lumenlace camouflage rules", item, "LumenlaceCamouflageRules");
		assertNotContains("Lumenlace item has no Foxfire name", item, "Foxfire");

		String winterShroud = read(MORPHLING_PACKAGE.resolve("WinterShroudMorphlingItem.java"));
		assertContains("Winter Shroud uses renamed stillness rules", winterShroud, "LumenlaceCamouflageRules");
		assertNotContains("Winter Shroud has no Foxfire rule reference", winterShroud, "FoxfireCamouflageRules");
	}

	private static void activeRegistrationsAndResourcesUseLumenlaceIds() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		assertContains("Lumenlace item registration", itemInit,
				"morphling_lumenlace = BASEITEMS.register(\"morphling_lumenlace\"");
		assertContains("Lumenlace item constructor", itemInit,
				"new LumenlaceMorphlingItem(new Item.Properties().stacksTo(1))");
		assertNotContains("old Foxfire item registration", itemInit,
				"morphling_foxfire = BASEITEMS.register(\"morphling_foxfire\"");

		String lastRite = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/LastRiteHelper.java"));
		assertContains("Last Rite imports Lumenlace item", lastRite, "LumenlaceMorphlingItem");
		assertNotContains("Last Rite has no Foxfire item import", lastRite, "FoxfireMorphlingItem");

		String bestiary = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/bestiary/SpecimenBestiaryDefinitions.java"));
		assertContains("bestiary maps cuttlefish layer to Lumenlace", bestiary,
				"ItemInit.morphling_lumenlace, \"lumenlace\"");
		assertNotContains("bestiary has no Foxfire family", bestiary, "\"foxfire\"");

		String hud = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/overlay/MorphlingHudVisuals.java"));
		assertContains("HUD uses Lumenlace item ID", hud, "\"morphling_lumenlace\"");
		assertNotContains("HUD has no Foxfire item ID", hud, "morphling_foxfire");

		assertExists("Lumenlace item model", RESOURCE_ROOT.resolve(
				"assets/hemomancy/models/item/morphling_lumenlace.json"));
		assertExists("Lumenlace incubator recipe", RESOURCE_ROOT.resolve(
				"data/hemomancy/recipe/incubator/morphling_lumenlace.json"));
		assertExists("Lumenlace item texture", RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/item/morphling_lumenlace.png"));
		assertMissing("old Foxfire item model", RESOURCE_ROOT.resolve(
				"assets/hemomancy/models/item/morphling_foxfire.json"));
		assertMissing("old Foxfire incubator recipe", RESOURCE_ROOT.resolve(
				"data/hemomancy/recipe/incubator/morphling_foxfire.json"));
		assertMissing("old Foxfire item texture", RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/item/morphling_foxfire.png"));

		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		assertContains("Lumenlace item translation", language,
				"\"item.hemomancy.morphling_lumenlace\": \"Lumenlace Morphling\"");
		assertContains("Lumenlace binomial translation", language,
				"\"morphling.hemomancy.lumenlace.binomial\": \"Luminaria nervosa\"");
		assertNotContains("old Foxfire item translation", language, "item.hemomancy.morphling_foxfire");
		assertNotContains("old Foxfire binomial translation", language, "morphling.hemomancy.foxfire");
	}

	private static void activeSourceAndDocumentationContainNoFoxfireName() throws IOException {
		for (Path root : List.of(SOURCE_ROOT, RESOURCE_ROOT, DOCS_ROOT)) {
			try (Stream<Path> paths = Files.walk(root)) {
				for (Path path : paths.filter(Files::isRegularFile).toList()) {
					if (path.equals(LEGACY_MIGRATION) || !isText(path)) continue;
					String content = read(path);
					assertNotContains("active Foxfire reference in " + path, content, "foxfire");
					assertNotContains("active FoxFire reference in " + path, content, "Foxfire");
				}
			}
		}
	}

	private static void legacyFoxfireIdsOnlyRemainAsExplicitMigrationInputs() throws IOException {
		String migration = read(LEGACY_MIGRATION);
		assertContains("old item ID migrates to Lumenlace", migration,
				"entry(\"morphling_foxfire\", \"morphling_lumenlace\")");
		assertContains("old layer family migrates to Lumenlace", migration,
				"entry(\"foxfire\", \"lumenlace\")");
	}

	private static boolean isText(Path path) {
		String name = path.getFileName().toString().toLowerCase();
		return name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".md")
				|| name.endsWith(".mcmeta") || name.endsWith(".properties") || name.endsWith(".toml")
				|| name.endsWith(".txt");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) throw new AssertionError("missing " + path);
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) throw new AssertionError(label + ": missing " + path);
	}

	private static void assertMissing(String label, Path path) {
		if (Files.exists(path)) throw new AssertionError(label + ": still exists " + path);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) throw new AssertionError(label + ": missing " + expected);
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.toLowerCase().contains(unexpected.toLowerCase())) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}
}
