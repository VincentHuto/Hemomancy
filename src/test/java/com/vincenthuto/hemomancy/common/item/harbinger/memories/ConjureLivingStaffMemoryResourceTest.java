package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConjureLivingStaffMemoryResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ConjureLivingStaffMemoryResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String hemomancy = read("src/main/java/com/vincenthuto/hemomancy/Hemomancy.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		Path model = ROOT.resolve("src/main/resources/assets/hemomancy/models/item/memory_conjure_living_staff.json");
		Path overlay = ROOT.resolve("src/main/resources/assets/hemomancy/textures/item/memories/memory_living_staff_overlay.png");
		Path recipe = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_conjure_living_staff.json");

		assertContains("living staff memory holder is registered", itemInit,
				"memory_conjure_living_staff = BASEITEMS.register");
		assertContains("living staff memory registry id is correct", itemInit,
				"\"memory_conjure_living_staff\"");
		assertContains("living staff memory teaches conjure staff", itemInit,
				"new BloodMemoryItem(new Item.Properties(), ManipulationInit.conjure_staff)");
		assertContains("living staff memory is hidden from creative tab", hemomancy,
				"item != ItemInit.memory_conjure_living_staff.get()");
		assertContains("living staff memory has lang entry", lang,
				"\"item.hemomancy.memory_conjure_living_staff\": \"Memory Conjure Living Staff\"");
		assertExists("living staff memory model exists", model);
		String modelJson = Files.readString(model);
		assertContains("living staff memory model uses blank memory base", modelJson,
				"\"layer0\": \"hemomancy:item/memories/memory_blank\"");
		assertContains("living staff memory model uses living staff overlay", modelJson,
				"\"layer1\": \"hemomancy:item/memories/memory_living_staff_overlay\"");
		assertExists("living staff overlay texture exists", overlay);
		assertMissing("living staff memory has no memory weaving recipe", recipe);
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + " (missing " + path + ")");
		}
	}

	private static void assertMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + " (unexpected " + path + ")");
		}
	}
}
