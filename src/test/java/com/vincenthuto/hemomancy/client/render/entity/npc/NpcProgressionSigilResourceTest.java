package com.vincenthuto.hemomancy.client.render.entity.npc;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NpcProgressionSigilResourceTest {
	@Test
	void progressionSigilIsSmallAndTransparent() throws IOException {
		try (var stream = getClass().getResourceAsStream(
				"/assets/hemomancy/textures/entity/npc/progression_sigil.png")) {
			assertNotNull(stream);
			BufferedImage image = ImageIO.read(stream);
			assertEquals(16, image.getWidth());
			assertEquals(16, image.getHeight());
			assertEquals(0, image.getRGB(0, 0) >>> 24);
			assertTrue(image.getRGB(8, 8) >>> 24 > 0);
		}
	}

	@Test
	void unstainedSigilIsDistinctSmallAndTransparent() throws IOException {
		try (var unstainedStream = getClass().getResourceAsStream(
				"/assets/hemomancy/textures/entity/npc/unstained_progression_sigil.png");
				var harbingerStream = getClass().getResourceAsStream(
						"/assets/hemomancy/textures/entity/npc/progression_sigil.png")) {
			assertNotNull(unstainedStream);
			assertNotNull(harbingerStream);
			BufferedImage unstained = ImageIO.read(unstainedStream);
			BufferedImage harbinger = ImageIO.read(harbingerStream);
			assertEquals(16, unstained.getWidth());
			assertEquals(16, unstained.getHeight());
			assertEquals(0, unstained.getRGB(0, 0) >>> 24);
			assertTrue(unstained.getRGB(8, 8) >>> 24 > 0);
			assertNotEquals(harbinger.getRGB(8, 8), unstained.getRGB(8, 8));
		}
	}
}
