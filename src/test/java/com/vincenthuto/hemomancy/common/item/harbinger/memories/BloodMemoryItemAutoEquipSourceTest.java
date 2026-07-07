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

		assertContains("blood memory delegates grant and auto-equip behavior", source,
				"KnownManipulationGrantHelper.grantMemory");
		assertContains("blood memory handles equipped grant feedback", source,
				"MemoryGrantStatus.GRANTED_EQUIPPED");
		assertContains("blood memory handles learned-only feedback", source,
				"MemoryGrantStatus.GRANTED");
		assertContains("blood memory tells player when auto-equipped", source,
				"Memorized and equipped: ");
		assertContains("blood memory tells player when reliquary is needed", source,
				"Use a Mnemonic Reliquary to change equipped memories.");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
