package com.vincenthuto.hemomancy.client.screen.overlay;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

public final class EquippedMorphlingOverlayTest {
	private static final Path TEXTURE_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/morphling_overlay");
	private static final List<String> STRAINS = List.of(
			"morphling_deadmans_purse", "morphling_gravecap", "morphling_witchs_ear", "morphling_foxfire",
			"morphling_bootlace", "morphling_irontooth", "morphling_emberfang", "morphling_winter_shroud");

	private EquippedMorphlingOverlayTest() {
	}

	public static void main(String[] args) throws Exception {
		legacyIconPlacementRemainsAvailable();
		attachedMorphlingGripsHalfwayAcrossTheBloodBarEdge();
		mirroredSpriteKeepsFrontFacingGeometryAndReversesUvs();
		canonicalStrainsHaveCompleteHudVisuals();
	}

	private static void legacyIconPlacementRemainsAvailable() {
		assertEquals("left-side blood bar places morphling icon to the right", 58,
				EquippedMorphlingOverlayPlacement.iconXForBloodBar(true, 4, 46));
		assertEquals("right-side blood bar places morphling icon to the left", 176,
				EquippedMorphlingOverlayPlacement.iconXForBloodBar(false, 200, 46));
		assertEquals("morphling icon is vertically centered on blood bar", 33,
				EquippedMorphlingOverlayPlacement.iconYForBloodBar(4, 75));
	}

	private static void attachedMorphlingGripsHalfwayAcrossTheBloodBarEdge() {
		assertEquals("left attached x overlaps half the sprite", 26,
				EquippedMorphlingOverlayPlacement.attachedXForBloodBar(true, 4, 46));
		assertEquals("right attached x overlaps half the sprite", 176,
				EquippedMorphlingOverlayPlacement.attachedXForBloodBar(false, 200, 46));
		assertEquals("attached y", 17,
				EquippedMorphlingOverlayPlacement.attachedYForBloodBar(4, 75));
		assertFalse("left HUD uses authored orientation",
				EquippedMorphlingOverlayPlacement.shouldMirror(true));
		assertTrue("right HUD mirrors authored orientation",
				EquippedMorphlingOverlayPlacement.shouldMirror(false));
	}

	private static void mirroredSpriteKeepsFrontFacingGeometryAndReversesUvs() {
		EquippedMorphlingOverlayPlacement.SpriteBlit normal =
				EquippedMorphlingOverlayPlacement.spriteBlit(false);
		assertEquals("normal geometry width", 48, normal.width());
		assertEquals("normal u origin", 0, normal.uOffset());
		assertEquals("normal u direction", 48, normal.uWidth());

		EquippedMorphlingOverlayPlacement.SpriteBlit mirrored =
				EquippedMorphlingOverlayPlacement.spriteBlit(true);
		assertEquals("mirrored geometry remains front-facing", 48, mirrored.width());
		assertEquals("mirrored u origin starts at right edge", 48, mirrored.uOffset());
		assertEquals("mirrored u direction is reversed", -48, mirrored.uWidth());
	}

	private static void canonicalStrainsHaveCompleteHudVisuals() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			if (visual == null) {
				throw new AssertionError("Missing HUD visual registration for " + strain);
			}
			assertEquals("mouth x in authored edge for " + strain, 5, visual.mouthX());
			if (visual.mouthY() < 8 || visual.mouthY() > 40) {
				throw new AssertionError("Mouth anchor outside sprite for " + strain + ": " + visual.mouthY());
			}

			Path texture = TEXTURE_ROOT.resolve(visual.textureName() + ".png");
			if (!Files.isRegularFile(texture)) {
				throw new AssertionError("Missing HUD texture " + texture);
			}
			BufferedImage image = ImageIO.read(texture.toFile());
			assertEquals("texture width for " + strain, 48, image.getWidth());
			assertEquals("texture height for " + strain, 48, image.getHeight());
			int transparent = 0;
			int visible = 0;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int alpha = image.getRGB(x, y) >>> 24;
					if (alpha == 0) transparent++;
					if (alpha >= 128) visible++;
				}
			}
			if (transparent < 48 * 48 / 3 || visible < 64) {
				throw new AssertionError("HUD texture needs transparent space and a readable subject: " + texture);
			}
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
