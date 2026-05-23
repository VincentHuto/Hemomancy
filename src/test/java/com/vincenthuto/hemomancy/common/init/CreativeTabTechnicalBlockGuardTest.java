package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class CreativeTabTechnicalBlockGuardTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	public static void main(String[] args) throws IOException {
		hiddenBlocksWithNoItemFormAreSkipped();
	}

	private static void hiddenBlocksWithNoItemFormAreSkipped() throws IOException {
		String hemomancy = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/Hemomancy.java"));

		assertContains("creative tab population should skip blocks whose item form is air",
				hemomancy, "asItem() != Items.AIR");
		assertContains("creative tab population should explicitly keep the abocipher emitter hidden",
				hemomancy, "BlockInit.abocipher_emitter.get()");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
