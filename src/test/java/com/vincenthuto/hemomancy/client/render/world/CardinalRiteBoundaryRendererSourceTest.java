package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CardinalRiteBoundaryRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/"
					+ "CardinalRiteBoundaryRenderer.java");

	@Test
	void transparentStainsUseTheColorOnlyPassInsteadOfWritingDepth() throws IOException {
		String source = Files.readString(RENDERER).replace("\r\n", "\n");

		assertTrue(source.contains("drawBoundaryFloorStain(\n"
						+ "\t\t\t\t\t\tglowVC"),
				"boundary stain uses the color-only glow consumer");
		assertTrue(source.contains("drawSocketStain(glowVC"),
				"socket stain uses the color-only glow consumer");
		assertFalse(source.contains("drawBoundaryFloorStain(\n"
						+ "\t\t\t\t\t\tcoreVC"),
				"transparent boundary feather must not write depth");
		assertFalse(source.contains("drawSocketStain(coreVC"),
				"transparent socket feather must not write depth");
	}

	@Test
	void removedRecipeFromAnOldWorldFallsBackToPlainBoundaryGeometry() throws IOException {
		String source = Files.readString(RENDERER).replace("\r\n", "\n");

		assertTrue(source.contains("CardinalRiteCeremonyDefinition ceremony = recipe == null"),
				"renderer must tolerate an active rite whose recipe was removed by a datapack update");
		assertTrue(source.contains("hasAnchorSockets ? segmentClearances("),
				"missing ceremony should use zero-clearance boundary arcs");
		assertTrue(source.contains("if (!legacy && glowVC != null && hasAnchorSockets)"),
				"ceremony-authored floor stains must be skipped when recipe data is unavailable");
	}
}
