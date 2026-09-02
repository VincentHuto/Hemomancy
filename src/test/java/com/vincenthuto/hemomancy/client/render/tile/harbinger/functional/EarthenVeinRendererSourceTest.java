package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EarthenVeinRendererSourceTest {
	@Test
	void temporaryVeinsGrowFromTheGround() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/EarthenVeinRenderer.java"));

		assertTrue(source.contains("te.isTemporary()"), "temporary veins need a distinct render path");
		assertTrue(source.contains("pPoseStack.scale(growth, growth, growth)"),
				"temporary veins need a ground-anchored growth scale");
		assertTrue(source.contains("1.51D + (growth - 1.0F) * 1.5D"),
				"temporary growth must compensate the model root offset to stay grounded");
	}
}
