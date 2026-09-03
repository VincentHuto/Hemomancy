package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusTextureUvTest {
	private static final Path TEXTURES = Path.of(
			"src/main/resources/assets/hemomancy/textures/entity/circus");

	@Test
	void baseTexturesPaintUvIslandsInsteadOfTheWholeAtlas() throws Exception {
		String[] names = {
				"fire_eater_0.png", "fire_eater_1.png", "stilt_walker_0.png", "stilt_walker_1.png",
				"acrobat_0.png", "acrobat_1.png", "knife_thrower_0.png", "knife_thrower_1.png",
				"ringmaster.png", "carousel.png"
		};
		for (String name : names) {
			var image = ImageIO.read(TEXTURES.resolve(name).toFile());
			int transparent = 0;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					if ((image.getRGB(x, y) >>> 24) == 0) transparent++;
				}
			}
			assertTrue(transparent > image.getWidth() * image.getHeight() / 10,
					name + " must leave unused UV space transparent");
			assertTrue((image.getRGB(1, image.getHeight() == 256 ? 11 : 9) >>> 24) != 0,
					name + " must paint its first model face");
		}
	}
}
