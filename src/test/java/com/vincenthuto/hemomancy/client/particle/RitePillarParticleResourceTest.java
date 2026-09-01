package com.vincenthuto.hemomancy.client.particle;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RitePillarParticleResourceTest {
	private static final Path PARTICLE_DEFINITION = Path.of(
			"src/main/resources/assets/hemomancy/particles/rite_pillar.json");
	private static final Path PARTICLE_TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/particle/rite_blood_pillar.png");

	@Test
	void definitionUsesParticleAtlasRelativeTextureId() throws IOException {
		String definition = Files.readString(PARTICLE_DEFINITION).replace("\r\n", "\n");

		assertTrue(definition.contains("\"hemomancy:rite_blood_pillar\""),
				"particle texture IDs are relative to textures/particle and must not repeat particle/");
	}

	@Test
	void pillarTextureIsAValidTallTransparentPng() throws IOException {
		assertTrue(Files.isRegularFile(PARTICLE_TEXTURE), "rite pillar texture is missing");
		BufferedImage image = ImageIO.read(PARTICLE_TEXTURE.toFile());

		assertNotNull(image, "rite pillar texture must be a readable PNG");
		assertEquals(64, image.getWidth());
		assertEquals(256, image.getHeight());
		assertTrue(image.getColorModel().hasAlpha(), "rite pillar texture needs transparency");
	}
}
