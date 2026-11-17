package com.vincenthuto.hemomancy.client.texture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** FX-6: the three item sprites that were off size are 16 by 16, cropped or padded rather than stretched. */
class ItemSpriteSizeTest {
	@Test
	void theResizedSpritesAreSixteenSquare() throws IOException {
		for (String name : new String[] { "barbed_splinter", "incandescent_enzyme", "hematic_condenser_kit" }) {
			BufferedImage image = ImageIO.read(RepoPaths.ASSETS.resolve("textures/item/" + name + ".png").toFile());
			assertNotNull(image, name);
			assertEquals(16, image.getWidth(), name + " width");
			assertEquals(16, image.getHeight(), name + " height");
		}
	}
}
