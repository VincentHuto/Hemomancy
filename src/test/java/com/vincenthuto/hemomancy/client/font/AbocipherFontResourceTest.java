package com.vincenthuto.hemomancy.client.font;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class AbocipherFontResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private AbocipherFontResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String fontDefinition = read(RESOURCE_ROOT.resolve("assets/hemomancy/font/abocipher.json"));

		assertContains("abocipher font should preserve vanilla spacing",
				fontDefinition, "\"id\": \"minecraft:include/space\"");
		assertContains("abocipher font should use a stitched bitmap atlas",
				fontDefinition, "\"type\": \"bitmap\"");
		assertContains("abocipher font should point at the stitched atlas",
				fontDefinition, "\"file\": \"hemomancy:font/abocipher.png\"");
		assertContains("abocipher font should align like the blood font",
				fontDefinition, "\"ascent\": 7");
		assertContains("abocipher font should map lowercase letters",
				fontDefinition, "\"" + LOWERCASE + "\"");
		assertContains("abocipher font should map uppercase letters",
				fontDefinition, "\"" + UPPERCASE + "\"");

		for (char letter : LOWERCASE.toCharArray()) {
			assertPathExists("source glyph " + letter,
					RESOURCE_ROOT.resolve("assets/hemomancy/textures/font/abocipher/" + letter + ".png"));
		}

		Path atlas = RESOURCE_ROOT.resolve("assets/hemomancy/textures/font/abocipher.png");
		assertPngHeader("stitched abocipher atlas should be a real PNG", atlas);
		BufferedImage image = ImageIO.read(atlas.toFile());
		if (image == null) {
			throw new AssertionError("stitched abocipher atlas should be readable as a PNG");
		}
		assertEquals("stitched abocipher atlas width", LOWERCASE.length() * 24, image.getWidth());
		assertEquals("stitched abocipher atlas height", 48, image.getHeight());
	}

	private static void assertPathExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertPngHeader(String label, Path path) throws IOException {
		assertPathExists(label, path);
		byte[] header = Files.readAllBytes(path);
		byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		for (int i = 0; i < pngHeader.length; i++) {
			if (header.length <= i || header[i] != pngHeader[i]) {
				throw new AssertionError(label + ": invalid PNG header");
			}
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
