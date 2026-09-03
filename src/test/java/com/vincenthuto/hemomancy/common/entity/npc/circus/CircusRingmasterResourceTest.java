package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusRingmasterResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void ringmasterShipsWithRuntimeAndEditableAssets() throws Exception {
		Path texture = RESOURCES.resolve("assets/hemomancy/textures/entity/circus/ringmaster.png");
		Path glow = RESOURCES.resolve("assets/hemomancy/textures/entity/circus/ringmaster_glow.png");
		Path bbmodel = RESOURCES.resolve("assets/hemomancy/models/entity/bbmodel/CircusRingmasterModel.bbmodel");
		assertTrue(Files.isRegularFile(texture));
		assertTrue(Files.isRegularFile(glow));
		assertTrue(Files.isRegularFile(bbmodel));
		assertEquals(128, ImageIO.read(texture.toFile()).getWidth());
		assertEquals(128, ImageIO.read(texture.toFile()).getHeight());
		String editable = Files.readString(bbmodel);
		assertTrue(editable.contains("\"top_hat\""));
		assertTrue(editable.contains("\"living_staff\""));
		assertTrue(Files.readString(Path.of("src/generated/resources/assets/hemomancy/lang/en_us.json"))
				.contains("\"entity.hemomancy.circus_ringmaster\": \"Ringmaster\""));

		String model = source("client/model/entity/npc/CircusRingmasterModel.java");
		assertTrue(model.contains("LayerDefinition.create(mesh, 128, 128)"));
		assertFalse(model.contains("addBoxPart("));
	}

	private static String source(String relativePath) throws Exception {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}
