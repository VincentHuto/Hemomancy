package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CrudeMemoryModelDataTest {
	private static final Path MODEL_ROOT = Path.of("src/main/resources/assets/hemomancy/models/item");

	private static final String[] CRUDE_MEMORIES = {
			"blood_lamp",
			"blood_rush",
			"blood_shot",
			"crimson_harvest",
			"deadly_gaze",
			"glacial_grasp",
			"hemorrhage",
			"sanguine_ignition",
			"sanguine_mending",
			"void_shroud"
	};

	private CrudeMemoryModelDataTest() {
	}

	public static void main(String[] args) throws IOException {
		for (String memory : CRUDE_MEMORIES) {
			String model = Files.readString(MODEL_ROOT.resolve("crude_memory_" + memory + ".json"));
			assertContains(memory + " uses the memory blank base", model,
					"\"layer0\": \"hemomancy:item/memories/memory_blank\"");
			assertContains(memory + " uses its matching memory overlay", model,
					"\"layer1\": \"hemomancy:item/memories/memory_" + memory + "_overlay\"");
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
