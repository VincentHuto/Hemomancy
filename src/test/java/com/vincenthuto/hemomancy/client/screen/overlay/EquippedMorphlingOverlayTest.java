package com.vincenthuto.hemomancy.client.screen.overlay;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

public final class EquippedMorphlingOverlayTest {
	private static final int MAX_VISIBLE_RGB_COLORS = 12;
	private static final Path TEXTURE_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/morphling_overlay");
	private static final List<String> STRAINS = List.of(
			"morphling_deadmans_purse", "morphling_gravecap", "morphling_witchs_ear", "morphling_foxfire",
			"morphling_bootlace", "morphling_irontooth", "morphling_emberfang", "morphling_winter_shroud");
	private static final Map<String, List<AnimationRegion>> FEEDING_ANIMATION_REGIONS = Map.ofEntries(
			Map.entry("bootlace", List.of(region(24, 10, 48, 44))),
			Map.entry("deadmans_purse", List.of(region(13, 12, 38, 44))),
			Map.entry("emberfang", List.of(region(17, 4, 48, 32))),
			Map.entry("foxfire", List.of(region(16, 14, 48, 44))),
			Map.entry("gravecap", List.of(region(18, 13, 48, 43))),
			Map.entry("irontooth", List.of(region(17, 6, 48, 30))),
			Map.entry("winter_shroud", List.of(region(17, 1, 47, 34))),
			Map.entry("witchs_ear", List.of(region(14, 7, 31, 28))));

	private EquippedMorphlingOverlayTest() {
	}

	public static void main(String[] args) throws Exception {
		legacyIconPlacementRemainsAvailable();
		attachedMorphlingGripsHalfwayAcrossTheBloodBarEdge();
		mirroredSpriteKeepsFrontFacingGeometryAndReversesUvs();
		feedingAnimationAdvancesThroughFiveFramesAndLoops();
		canonicalStrainsHaveCompleteHudVisuals();
		canonicalStrainsAnimateTheirBodiesInsteadOfTranslatingTheWholeSprite();
		canonicalStrainsKeepNonAnimatedPixelsByteExact();
		canonicalStrainsUseOneTwelveColorPaletteAcrossAllFrames();
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

	private static void feedingAnimationAdvancesThroughFiveFramesAndLoops() {
		assertEquals("animation starts at frame zero", 0,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.0f));
		assertEquals("animation reaches frame one", 1,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.16f));
		assertEquals("animation reaches final frame", 4,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.64f));
		assertEquals("animation loops", 0,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.80f));
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
			assertEquals("five-frame texture height for " + strain, 48 * 5, image.getHeight());
			int transparent = 0;
			int visible = 0;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int alpha = image.getRGB(x, y) >>> 24;
					if (alpha == 0) transparent++;
					if (alpha >= 128) visible++;
				}
			}
			if (transparent < 48 * 48 * 5 / 3 || visible < 64 * 5) {
				throw new AssertionError("HUD texture needs transparent space and a readable subject: " + texture);
			}
		}
	}

	private static void canonicalStrainsAnimateTheirBodiesInsteadOfTranslatingTheWholeSprite() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
			for (int frame = 1; frame < 5; frame++) {
				if (isRigidTranslationOfFirstFrame(image, frame)) {
					throw new AssertionError("Morphling frame must deform internally, not translate rigidly: "
							+ strain + " frame " + frame);
				}
			}
		}
	}

	private static boolean isRigidTranslationOfFirstFrame(BufferedImage image, int frame) {
		for (int dy = -3; dy <= 3; dy++) {
			for (int dx = -3; dx <= 3; dx++) {
				boolean matches = true;
				for (int y = 0; y < 48 && matches; y++) {
					for (int x = 0; x < 48; x++) {
						int sourceX = x - dx;
						int sourceY = y - dy;
						int expected = sourceX >= 0 && sourceX < 48 && sourceY >= 0 && sourceY < 48
								? image.getRGB(sourceX, sourceY)
								: 0;
						if (image.getRGB(x, y + frame * 48) != expected) {
							matches = false;
							break;
						}
					}
				}
				if (matches) return true;
			}
		}
		return false;
	}

	private static void canonicalStrainsKeepNonAnimatedPixelsByteExact() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
			List<AnimationRegion> animatedRegions = FEEDING_ANIMATION_REGIONS.get(visual.textureName());
			if (animatedRegions == null) {
				throw new AssertionError("Missing feeding animation region for " + strain);
			}
			for (int frame = 1; frame < 5; frame++) {
				int changedInsideRegion = 0;
				for (int y = 0; y < 48; y++) {
					for (int x = 0; x < 48; x++) {
						if (image.getRGB(x, y) == image.getRGB(x, y + frame * 48)) continue;
						boolean intentionallyAnimated = isIntentionallyAnimated(animatedRegions, x, y);
						if (!intentionallyAnimated) {
							throw new AssertionError("Non-animated pixel changed for " + strain + " frame " + frame
									+ " at (" + x + "," + y + ")");
						}
						changedInsideRegion++;
					}
				}
				if (changedInsideRegion < 16) {
					throw new AssertionError("Feeding frame needs a real localized redraw for " + strain
							+ " frame " + frame + ": only " + changedInsideRegion + " pixels changed");
				}
			}
		}
	}

	private static void canonicalStrainsUseOneTwelveColorPaletteAcrossAllFrames() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
			Set<Integer> visibleRgbColors = new HashSet<>();
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int argb = image.getRGB(x, y);
					if ((argb >>> 24) != 0) {
						visibleRgbColors.add(argb & 0x00FFFFFF);
					}
				}
			}
			if (visibleRgbColors.size() > MAX_VISIBLE_RGB_COLORS) {
				throw new AssertionError("HUD texture exceeds shared 12-color palette for " + strain + ": "
						+ visibleRgbColors.size());
			}
		}
	}

	private static AnimationRegion region(int minX, int minY, int maxX, int maxY) {
		return new AnimationRegion(minX, minY, maxX, maxY);
	}

	private static boolean isIntentionallyAnimated(List<AnimationRegion> regions, int x, int y) {
		for (AnimationRegion region : regions) {
			if (region.contains(x, y)) return true;
		}
		return false;
	}

	private record AnimationRegion(int minX, int minY, int maxX, int maxY) {
		private boolean contains(int x, int y) {
			return x >= minX && x < maxX && y >= minY && y < maxY;
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
