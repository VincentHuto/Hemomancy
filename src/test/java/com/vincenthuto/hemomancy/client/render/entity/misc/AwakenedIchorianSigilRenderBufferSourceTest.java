package com.vincenthuto.hemomancy.client.render.entity.misc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AwakenedIchorianSigilRenderBufferSourceTest {
	private static final Path SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/"
					+ "AwakenedIchorianSigilGeometryRenderer.java");

	@Test
	void eachSharedBufferLayerIsRenderedBeforeTheNextLayerIsRequested() throws IOException {
		String source = Files.readString(SOURCE);

		int glowPass = source.indexOf(
				"renderGlowPass(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW)");
		int corePass = source.indexOf(
				"renderCorePass(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE)");

		assertTrue(glowPass >= 0, "the glow consumer must be acquired at its complete render pass");
		assertTrue(corePass > glowPass, "the core layer must be requested only after the glow pass returns");
		assertFalse(source.contains("VertexConsumer glow = buffers.getBuffer"),
				"a shared-buffer consumer must not be retained while another RenderType is requested");
		assertFalse(source.contains("VertexConsumer core = buffers.getBuffer"),
				"a shared-buffer consumer must not be retained while another RenderType is requested");
	}

	@Test
	void coloredLandmarksRemainVisibleThroughoutUnfolding() throws IOException {
		String source = Files.readString(SOURCE);

		assertFalse(source.contains(
				"landmark.activation() <= 0.001F && pose.migration() > 0.001F"),
				"migrating ground nodes must not disappear before quickening begins");
	}
}
