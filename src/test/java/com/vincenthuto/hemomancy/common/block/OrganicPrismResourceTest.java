package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OrganicPrismResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path ASSET_ROOT = RESOURCE_ROOT.resolve("assets/hemomancy");

	private OrganicPrismResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertBlockIsRegistered();
		assertBlockAssetsAndLocalization();
		assertOrganicPrismRecipe();
	}

	private static void assertBlockIsRegistered() throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		assertContains("organic prism block registry", blockInit,
				"organic_prism = BASEBLOCKS.register(\"organic_prism\"");
	}

	private static void assertBlockAssetsAndLocalization() throws IOException {
		String lang = read(ASSET_ROOT.resolve("lang/en_us.json"));
		assertContains("organic prism localization", lang,
				"\"block.hemomancy.organic_prism\": \"Organic Prism\"");

		assertContains("organic prism blockstate", read(ASSET_ROOT.resolve("blockstates/organic_prism.json")),
				"\"model\": \"hemomancy:block/organic_prism\"");
		assertContains("organic prism block model", read(ASSET_ROOT.resolve("models/block/organic_prism.json")),
				"\"all\": \"hemomancy:block/organic_prism\"");
		assertContains("organic prism item model", read(ASSET_ROOT.resolve("models/item/organic_prism.json")),
				"\"parent\": \"hemomancy:block/organic_prism\"");
		assertExists("organic prism texture", ASSET_ROOT.resolve("textures/block/organic_prism.png"));
	}

	private static void assertOrganicPrismRecipe() throws IOException {
		String recipe = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/organic_prism.json"));
		assertContains("organic prism recipe type", recipe, "\"type\": \"minecraft:crafting_shapeless\"");
		assertContains("organic prism recipe conscious mass", recipe,
				"\"item\": \"hemomancy:conscious_mass\"");
		assertContains("organic prism recipe hyphae block", recipe,
				"\"item\": \"hemomancy:hyphae_block\"");
		assertContains("organic prism recipe erythrocoral block", recipe,
				"\"item\": \"hemomancy:erythrocoral_block\"");
		assertContains("organic prism recipe result", recipe,
				"\"id\": \"hemomancy:organic_prism\"");
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

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
