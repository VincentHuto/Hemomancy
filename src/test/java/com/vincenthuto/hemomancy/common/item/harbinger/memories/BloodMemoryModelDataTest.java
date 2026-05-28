package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodMemoryModelDataTest {
	private static final Path MODEL_ROOT = Path.of("src/main/resources/assets/hemomancy/models/item");
	private static final Path OVERLAY_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/item/memories");

	private static final String[] MEMORIES_WITH_NEW_OVERLAYS = {
			"glacial_circulation",
			"osseous_bloom"
	};

	private BloodMemoryModelDataTest() {
	}

	public static void main(String[] args) throws IOException {
		for (String memory : MEMORIES_WITH_NEW_OVERLAYS) {
			String model = Files.readString(MODEL_ROOT.resolve("memory_" + memory + ".json"))
					.replace("\r\n", "\n");
			assertContains(memory + " keeps the refined memory base", model,
					"\"layer0\": \"hemomancy:item/memories/memory_blank\"");
			assertContains(memory + " uses its matching memory overlay", model,
					"\"layer1\": \"hemomancy:item/memories/memory_" + memory + "_overlay\"");
			assertExists(memory + " overlay texture",
					OVERLAY_ROOT.resolve("memory_" + memory + "_overlay.png"));
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}
}
