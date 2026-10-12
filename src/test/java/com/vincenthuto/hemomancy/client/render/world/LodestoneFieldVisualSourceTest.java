package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class LodestoneFieldVisualSourceTest {
	private static final Path SRC = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void axeFieldUsesItsOwnFerricGeometryInBothPasses() throws IOException {
		String renderer = read("client/render/world/ManipulationVisualRenderer.java");
		int field = renderer.indexOf("if(LodestoneFieldGeometry.handles(packet.form()))");
		int shared = renderer.indexOf("AnimusMortemGeometry.draw(");
		assertTrue(field > 0 && (shared < 0 || field < shared), "The field must be drawn before the shared Animus/Mortem branch");
		assertTrue(renderer.contains("&& !LodestoneFieldGeometry.handles(packet.form())"),
				"The solid pass must admit the field's iron");
		assertTrue(read("client/render/world/ManipulationMaterials.java").contains("LODESTONE_FIELD -> FERRIC"));
		String visuals = read("common/manipulation/ManipulationVisuals.java");
		assertTrue(visuals.contains("NERVE_COMMAND, LODESTONE_FIELD"), "Forms are ordinal-encoded: append new ones");
	}

	@Test
	void serverSendsTheSeededFieldInsteadOfTheGenericMagnet() throws IOException {
		String fields = read("common/item/harbinger/tool/living/LivingAxeLodestoneFields.java");
		assertTrue(fields.contains("Form.LODESTONE_FIELD"));
		assertTrue(!fields.contains("Form.MAGNET"), "The generic magnet ring is replaced by the authored field");
		assertTrue(fields.contains("1 + level.random.nextInt("), "Each field needs a non-zero seed");
	}

	private static String read(String path) throws IOException {
		return Files.readString(SRC.resolve(path)).replace("\r\n", "\n");
	}
}
