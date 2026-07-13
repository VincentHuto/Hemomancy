package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PuppeteersWoolResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path ASSET_ROOT = RESOURCE_ROOT.resolve("assets/hemomancy");

	private PuppeteersWoolResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertBlockIsRegistered();
		assertBlockAssetsAndLocalization();
		assertTwoByTwoThreadRecipe();
	}

	private static void assertBlockIsRegistered() throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		assertContains("puppeteers wool block registry", blockInit,
				"puppeteers_wool = BASEBLOCKS.register(\"puppeteers_wool\"");
	}

	private static void assertBlockAssetsAndLocalization() throws IOException {
		String lang = read(ASSET_ROOT.resolve("lang/en_us.json"));
		assertContains("puppeteers wool localization", lang,
				"\"block.hemomancy.puppeteers_wool\": \"Puppeteer's Wool\"");

		assertContains("puppeteers wool blockstate", read(ASSET_ROOT.resolve("blockstates/puppeteers_wool.json")),
				"\"model\": \"hemomancy:block/puppeteers_wool\"");
		assertContains("puppeteers wool block model", read(ASSET_ROOT.resolve("models/block/puppeteers_wool.json")),
				"\"all\": \"hemomancy:block/puppeteers_wool\"");
		assertContains("puppeteers wool item model", read(ASSET_ROOT.resolve("models/item/puppeteers_wool.json")),
				"\"parent\": \"hemomancy:block/puppeteers_wool\"");
		assertExists("puppeteers wool texture", ASSET_ROOT.resolve("textures/block/puppeteers_wool.png"));
	}

	private static void assertTwoByTwoThreadRecipe() throws IOException {
		String recipe = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/puppeteers_wool.json"));
		assertContains("puppeteers wool recipe type", recipe, "\"type\": \"minecraft:crafting_shaped\"");
		assertContains("puppeteers wool 2x2 pattern", recipe,
				"\"pattern\": [\n    \"PP\",\n    \"PP\"\n  ]");
		assertContains("puppeteers wool recipe thread", recipe,
				"\"item\": \"hemomancy:puppeteering_thread\"");
		assertContains("puppeteers wool recipe result", recipe,
				"\"id\": \"hemomancy:puppeteers_wool\"");
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
