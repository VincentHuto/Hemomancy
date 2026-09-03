package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CircusCarouselResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void carouselHasRuntimeAndEditableAssets() throws Exception {
		Path texture = RESOURCES.resolve("assets/hemomancy/textures/entity/circus/carousel.png");
		Path glow = RESOURCES.resolve("assets/hemomancy/textures/entity/circus/carousel_glow.png");
		Path bbmodel = RESOURCES.resolve("assets/hemomancy/models/entity/bbmodel/CircusCarouselModel.bbmodel");
		assertTrue(Files.isRegularFile(texture));
		assertTrue(Files.isRegularFile(glow));
		assertTrue(Files.isRegularFile(bbmodel));
		assertEquals(256, ImageIO.read(texture.toFile()).getWidth());
		assertEquals(256, ImageIO.read(texture.toFile()).getHeight());
		String editable = Files.readString(bbmodel);
		assertTrue(editable.contains("\"turntable\""));
		assertTrue(editable.contains("\"horse_0\""));
		assertTrue(editable.contains("\"horse_1\""));
		assertTrue(editable.contains("\"horse_2\""));

		String entityInit = source("common/init/EntityInit.java");
		String model = source("client/model/entity/npc/CircusCarouselModel.java");
		String client = source("client/event/ClientEvents.java");
		assertTrue(entityInit.contains("circus_carousel"));
		assertTrue(client.contains("CircusCarouselRenderer"));
		assertTrue(model.contains("LayerDefinition.create(mesh, 256, 256)"));
		assertFalse(model.contains("addBoxPart("));
	}

	private static String source(String relativePath) throws Exception {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}
