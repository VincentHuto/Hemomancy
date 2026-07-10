package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerArmorTextureResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private static final String[] DISTINCT_ITEM_TEXTURES = {
			"crimson_lacquer",
			"monolith_imbued_cloth",
			"grinning_mask",
			"lodestone_faceplate",
			"velorum_mask",
			"blood_lust_helm_grinning",
			"blood_lust_helm_lodestone",
			"blood_lust_helm_velorum",
			"silent_archon_helm",
			"silent_archon_chestplate",
			"silent_archon_leggings",
			"silent_archon_boots",
			"venous_strider_sabatons",
			"covenant_mantle"
	};

	private HarbingerArmorTextureResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		for (String item : DISTINCT_ITEM_TEXTURES) {
			assertItemModelUsesOwnTexture(item);
			String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + item + ".json"));
			if (!model.contains("\"parent\": \"builtin/entity\"")) {
				assertExists("item texture for " + item,
						RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/" + item + ".png"));
			}
		}

		assertExists("Silent Archon worn layer 1",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/silent_archon_layer_1.png"));
		assertExists("Silent Archon worn layer 2",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/silent_archon_layer_2.png"));
		assertExists("Chalybeate worn layer 1",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/venous_strider_layer_1.png"));
		assertExists("Covenant mantle worn layer 1",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/covenant_mantle_layer_1.png"));

		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		assertContains("ItemInit should register the renamed monolith cloth item", itemInit,
				"monolith_imbued_cloth = BASEITEMS.register(\"monolith_imbued_cloth\"");
		assertExists("recipe for renamed monolith cloth",
				RESOURCE_ROOT.resolve("data/hemomancy/recipe/monolith_imbued_cloth.json"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		assertContains("language should use the requested monolith cloth name", language,
				"\"item.hemomancy.monolith_imbued_cloth\": \"Monolith Imbued Cloth\"");
		assertNoTextFilesContain("legacy monolith wrapped cloth id should be fully renamed in source", SOURCE_ROOT,
				"monolith_wrapped_cloth");
		assertNoTextFilesContain("legacy monolith wrapped cloth id should be fully renamed in resources", RESOURCE_ROOT,
				"monolith_wrapped_cloth");
		assertNoTextFilesContain("legacy horned mask id should be fully renamed in source", SOURCE_ROOT,
				"horned_mask");
		assertNoTextFilesContain("legacy horned mask id should be fully renamed in resources", RESOURCE_ROOT,
				"horned_mask");
		assertNoTextFilesContain("legacy horned Blood Lust helm id should be fully renamed in source", SOURCE_ROOT,
				"blood_lust_helm_horned");
		assertNoTextFilesContain("legacy horned Blood Lust helm id should be fully renamed in resources", RESOURCE_ROOT,
				"blood_lust_helm_horned");

		String armorTiers = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/shared/armor/EnumModArmorTiers.java"));
		assertContains("Silent Archon must not reuse Blood Lust worn texture", armorTiers,
				"SILENT_ARCHON(\"silent_archon\"");
		assertContains("Venous Strider sabatons must not reuse Chitinite worn texture", armorTiers,
				"VENOUS_STRIDER(\"venous_strider\"");
		assertContains("Covenant mantle must not reuse Blood Lust worn texture", armorTiers,
				"COVENANT_MANTLE(\"covenant_mantle\"");
	}

	private static void assertItemModelUsesOwnTexture(String item) throws IOException {
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + item + ".json"));
		if (item.equals("monolith_imbued_cloth")) {
			assertContains("custom renderer model for " + item, model, "\"parent\": \"builtin/entity\"");
		} else if (item.equals("blood_lust_helm_lodestone")) {
			assertContains("lodestone helm reuses its component texture", model,
					"\"layer0\": \"hemomancy:item/lodestone_faceplate\"");
		} else {
			assertContains("model texture for " + item, model, "\"layer0\": \"hemomancy:item/" + item + "\"");
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

	private static void assertNoTextFilesContain(String label, Path root, String forbidden) throws IOException {
		try (var files = Files.walk(root)) {
			Path hit = files.filter(Files::isRegularFile)
					.filter(HarbingerArmorTextureResourceTest::isTextResource)
					.filter(path -> contains(path, forbidden))
					.findFirst()
					.orElse(null);
			if (hit != null) {
				throw new AssertionError(label + ": found " + forbidden + " in " + hit);
			}
		}
	}

	private static boolean isTextResource(Path path) {
		String fileName = path.getFileName().toString();
		return fileName.endsWith(".java") || fileName.endsWith(".json") || fileName.endsWith(".mcmeta");
	}

	private static boolean contains(Path path, String text) {
		try {
			return Files.readString(path).contains(text);
		} catch (IOException e) {
			throw new AssertionError("could not read " + path, e);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
