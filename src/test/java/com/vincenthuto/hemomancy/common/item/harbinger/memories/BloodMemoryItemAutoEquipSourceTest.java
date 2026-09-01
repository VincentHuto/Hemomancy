package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodMemoryItemAutoEquipSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private BloodMemoryItemAutoEquipSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java");

		assertNotContains("blood memory cannot be learned by right click", source,
				"KnownManipulationGrantHelper.grantMemory");
		assertContains("blood memory directs players to the brazier rite", source,
				"Burn this memory in a lit Iron Brazier");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) throw new AssertionError(label + " (found '" + unexpected + "')");
	}
}
