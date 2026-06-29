package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GrinningMaskRenameResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private GrinningMaskRenameResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String maskRecipe = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/grinning_mask.json"));
		String armatureRecipe = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/recipe/armature_upgrade/blood_lust_grinning_mask.json"));
		String phantasmalRecipe = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/recipe/armature_upgrade/prismatic_to_phantasmal_blood_lust_helm.json"));
		String silentArchonRecipe = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/recipe/armature_upgrade/blood_lust_to_silent_archon_helm.json"));

		assertContains("grinning mask item should be registered", itemInit,
				"grinning_mask = BASEITEMS.register(\"grinning_mask\"");
		assertContains("grinning Blood Lust helm should be registered", itemInit,
				"blood_lust_helm_grinning = BASEITEMS.register(\"blood_lust_helm_grinning\"");
		assertContains("grinning mask should have a lang entry", lang,
				"\"item.hemomancy.grinning_mask\": \"Grinning Mask\"");
		assertContains("grinning Blood Lust helm should have a lang entry", lang,
				"\"item.hemomancy.blood_lust_helm_grinning\": \"Blood Lust Grinning Mask\"");
		assertContains("grinning mask recipe should output renamed item", maskRecipe,
				"\"id\": \"hemomancy:grinning_mask\"");
		assertContains("Armature mask recipe should consume renamed mask", armatureRecipe,
				"\"item\": \"hemomancy:grinning_mask\"");
		assertContains("Armature mask recipe should output renamed helm", armatureRecipe,
				"\"id\": \"hemomancy:blood_lust_helm_grinning\"");
		assertContains("Phantasmal helmet should consume renamed mask", phantasmalRecipe,
				"\"item\": \"hemomancy:grinning_mask\"");
		assertContains("Silent Archon helmet accepts renamed mask variant", silentArchonRecipe,
				"\"item\": \"hemomancy:blood_lust_helm_grinning\"");

		assertExists("grinning mask model",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/grinning_mask.json"));
		assertExists("grinning mask texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/grinning_mask.png"));
		assertExists("grinning Blood Lust helm model",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/blood_lust_helm_grinning.json"));
		assertExists("grinning Blood Lust helm texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/blood_lust_helm_grinning.png"));

		assertNoTextFilesContain("legacy horned mask id should be removed from source", SOURCE_ROOT,
				"horned_mask");
		assertNoTextFilesContain("legacy horned mask id should be removed from resources", RESOURCE_ROOT,
				"horned_mask");
		assertNoTextFilesContain("legacy horned helm id should be removed from source", SOURCE_ROOT,
				"blood_lust_helm_horned");
		assertNoTextFilesContain("legacy horned helm id should be removed from resources", RESOURCE_ROOT,
				"blood_lust_helm_horned");
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
					.filter(GrinningMaskRenameResourceTest::isTextResource)
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
		return fileName.endsWith(".java") || fileName.endsWith(".json") || fileName.endsWith(".mcmeta")
				|| fileName.endsWith(".txt") || fileName.endsWith(".md");
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
