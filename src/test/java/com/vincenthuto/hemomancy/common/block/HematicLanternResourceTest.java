package com.vincenthuto.hemomancy.common.block;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HematicLanternResourceTest {
	private static final Path TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/block/hematic_lantern.png");

	@Test
	void animatedTextureKeepsSixteenPixelUvFrames() throws Exception {
		var image = ImageIO.read(TEXTURE.toFile());
		assertEquals(16, image.getWidth());
		assertEquals(48, image.getHeight());
		var metadata = Path.of(TEXTURE + ".mcmeta");
		assertTrue(Files.exists(metadata));
		assertTrue(Files.readString(metadata).contains("\"animation\""));
	}
}
