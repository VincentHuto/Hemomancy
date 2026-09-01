package com.vincenthuto.hemomancy.client.screen.overlay;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EquippedMorphlingOverlayTest {
	private static final int MAX_VISIBLE_RGB_COLORS = 12;
	private static final Path TEXTURE_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/morphling_overlay");
	private static final List<String> STRAINS = List.of(
			"morphling_deadmans_purse", "morphling_gravecap", "morphling_witchs_ear", "morphling_lumenlace",
			"morphling_bootlace", "morphling_irontooth", "morphling_emberfang", "morphling_winter_shroud");
	private static final Map<String, List<AnimationRegion>> FEEDING_ANIMATION_REGIONS = Map.ofEntries(
			Map.entry("bootlace", List.of(region(24, 10, 48, 44))),
			Map.entry("deadmans_purse", List.of(region(13, 12, 38, 44))),
			Map.entry("emberfang", List.of(region(17, 4, 48, 32))),
			Map.entry("lumenlace", List.of(region(13, 0, 48, 48))),
			Map.entry("gravecap", List.of(region(15, 12, 48, 44))),
			Map.entry("irontooth", List.of(region(17, 6, 48, 30))),
			Map.entry("winter_shroud", List.of(region(17, 1, 47, 34))),
			Map.entry("witchs_ear", List.of(region(14, 7, 31, 28))));

	private EquippedMorphlingOverlayTest() {
	}

	public static void main(String[] args) throws Exception {
		legacyIconPlacementRemainsAvailable();
		attachedMorphlingGripsHalfwayAcrossTheBloodBarEdge();
		mirroredSpriteKeepsFrontFacingGeometryAndReversesUvs();
		feedingAnimationAdvancesThroughSixFramesAndLoops();
		stableRenderClockAdvancesWithPartialTicks();
		bloodOverlayUsesStableRenderClock();
		bondMeterChecksVisibilityBeforeMutatingTheSharedPose();
		lumenlaceItemUsesLumenlaceHudIdentity();
		lumenlaceItemModelUsesLumenlaceTexture();
		lumenlacePaletteCarriesBlueAndGoldNeuralSignals();
		lumenlaceFramesHangFromAttachmentSide();
		lumenlaceIntermediateFramesKeepAuthoredCoreIntact();
		canonicalStrainsHaveCompleteHudVisuals();
		canonicalStrainsAnimateTheirBodiesInsteadOfTranslatingTheWholeSprite();
		canonicalStrainsMoveAnchoredPixelsWithTheirBodies();
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

	private static void feedingAnimationAdvancesThroughSixFramesAndLoops() {
		assertEquals("animation starts at frame zero", 0,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.0f));
		assertEquals("animation reaches frame one", 1,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.18f));
		assertEquals("animation reaches frame two", 2,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.36f));
		assertEquals("animation reaches frame three", 3,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.54f));
		assertEquals("animation reaches frame four", 4,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.72f));
		assertEquals("animation reaches frame five", 5,
				EquippedMorphlingOverlayPlacement.feedingFrame(0.91f));
		assertEquals("animation loops", 0,
				EquippedMorphlingOverlayPlacement.feedingFrame(1.09f));
	}

	private static void stableRenderClockAdvancesWithPartialTicks() {
		assertFloatEquals("clock starts at game tick", 5.0f,
				EquippedMorphlingOverlayPlacement.animationTimeSeconds(100L, 0.0f));
		assertFloatEquals("clock includes partial tick", 5.025f,
				EquippedMorphlingOverlayPlacement.animationTimeSeconds(100L, 0.5f));
		assertFloatEquals("clock advances one tick", 5.05f,
				EquippedMorphlingOverlayPlacement.animationTimeSeconds(101L, 0.0f));
	}

	private static void bloodOverlayUsesStableRenderClock() throws Exception {
		Path sourcePath = Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BloodVolumeOverlay.java");
		String source = Files.readString(sourcePath);
		if (source.contains("animTime += 0.016f")) {
			throw new AssertionError("Blood HUD animation must not advance from a render-call counter");
		}
		if (!source.contains("EquippedMorphlingOverlayPlacement.animationTimeSeconds")) {
			throw new AssertionError("Blood HUD animation must use the shared tick/partial-tick render clock");
		}
	}

	private static void bondMeterChecksVisibilityBeforeMutatingTheSharedPose() throws Exception {
		Path sourcePath = Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/EquippedMorphlingOverlay.java");
		String source = Files.readString(sourcePath);
		int methodStart = source.indexOf("private void renderBondMeter");
		int methodEnd = source.indexOf("\n\tprivate void renderLegacyIcon", methodStart);
		if (methodStart < 0 || methodEnd < 0) {
			throw new AssertionError("Could not locate renderBondMeter source");
		}
		String method = source.substring(methodStart, methodEnd);
		int guard = method.indexOf("if (!MorphlingItem.isPassiveUpkeepEnabled()");
		int push = method.indexOf("gfx.pose().pushPose()");
		int translate = method.indexOf("gfx.pose().translate(0,-62,0)");
		int pop = method.indexOf("gfx.pose().popPose()");
		assertTrue("bond meter visibility guard exists", guard >= 0);
		assertTrue("bond meter pushes a pose after its visibility guard", guard < push);
		assertTrue("bond meter translates only after pushing its pose", push < translate);
		assertTrue("bond meter restores its pose", pop > translate);
	}

	private static void lumenlaceItemUsesLumenlaceHudIdentity() {
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath("morphling_lumenlace");
		if (visual == null) throw new AssertionError("Missing HUD visual for morphling_lumenlace");
		if (!"lumenlace".equals(visual.textureName())) {
			throw new AssertionError("Lumenlace slot uses the wrong texture: " + visual.textureName());
		}
	}

	private static void lumenlaceItemModelUsesLumenlaceTexture() throws Exception {
		Path model = Path.of("src/main/resources/assets/hemomancy/models/item/morphling_lumenlace.json");
		String modelJson = Files.readString(model);
		if (!modelJson.contains("hemomancy:item/morphling_lumenlace")) {
			throw new AssertionError("Lumenlace item model must use the Lumenlace icon");
		}
		Path texture = Path.of("src/main/resources/assets/hemomancy/textures/item/morphling_lumenlace.png");
		if (!Files.isRegularFile(texture)) throw new AssertionError("Missing Lumenlace item texture");
		BufferedImage image = ImageIO.read(texture.toFile());
		assertEquals("Lumenlace item texture width", 16, image.getWidth());
		assertEquals("Lumenlace item texture height", 16, image.getHeight());
	}

	private static void lumenlacePaletteCarriesBlueAndGoldNeuralSignals() throws Exception {
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath("morphling_lumenlace");
		BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
		boolean blue = false;
		boolean gold = false;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				int red = (argb >>> 16) & 0xFF;
				int green = (argb >>> 8) & 0xFF;
				int blueChannel = argb & 0xFF;
				if ((argb >>> 24) != 0 && blueChannel > red + 40) blue = true;
				if ((argb >>> 24) != 0 && red > 150 && green > 120 && blueChannel < 150) gold = true;
			}
		}
		assertTrue("Lumenlace keeps a blue light body", blue);
		assertTrue("Lumenlace adds gold neural signals", gold);
	}

	private static void lumenlaceFramesHangFromAttachmentSide() throws Exception {
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath("morphling_lumenlace");
		BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
		for (int frame = 0; frame < 6; frame++) {
			int minX = image.getWidth();
			int maxX = -1;
			for (int y = 0; y < 48; y++) {
				for (int x = 0; x < 48; x++) {
					if ((image.getRGB(x, y + frame * 48) >>> 24) >= 128) {
						minX = Math.min(minX, x);
						maxX = Math.max(maxX, x);
					}
				}
			}
			if (minX < 14 || maxX != 47) {
				throw new AssertionError("Lumenlace frame must hang from the attachment side: frame " + frame
						+ " bounds " + minX + ".." + maxX);
			}
		}
	}

	private static void lumenlaceIntermediateFramesKeepAuthoredCoreIntact() throws Exception {
		MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath("morphling_lumenlace");
		BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
		for (int authoredFrame = 0; authoredFrame < 6; authoredFrame += 2) {
			int intermediateFrame = authoredFrame + 1;
			for (int y = 16; y < 32; y++) {
				for (int x = 22; x < 36; x++) {
					if (image.getRGB(x, y + authoredFrame * 48) != image.getRGB(x, y + intermediateFrame * 48)) {
						throw new AssertionError("Lumenlace in-between frame must keep its authored center pose: "
								+ intermediateFrame + " at " + x + "," + y);
					}
				}
			}
		}
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
			assertEquals("six-frame texture height for " + strain, 48 * 6, image.getHeight());
			int transparent = 0;
			int visible = 0;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int alpha = image.getRGB(x, y) >>> 24;
					if (alpha == 0) transparent++;
					if (alpha >= 128) visible++;
				}
			}
			if (transparent < 48 * 48 * 6 / 3 || visible < 64 * 6) {
				throw new AssertionError("HUD texture needs transparent space and a readable subject: " + texture);
			}
		}
	}

	private static void canonicalStrainsAnimateTheirBodiesInsteadOfTranslatingTheWholeSprite() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			// Lumenlace holds each authored core pose for its in-between slot to avoid a torn blend.
			if ("lumenlace".equals(visual.textureName())) continue;
			BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
			for (int frame = 1; frame < 6; frame++) {
				if (isRigidTranslationOfFirstFrame(image, frame)) {
					throw new AssertionError("Morphling frame must deform internally, not translate rigidly: "
							+ strain + " frame " + frame);
				}
			}
		}
	}

	private static void canonicalStrainsMoveAnchoredPixelsWithTheirBodies() throws Exception {
		for (String strain : STRAINS) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain);
			if ("lumenlace".equals(visual.textureName())) continue;
			BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(visual.textureName() + ".png").toFile());
			List<AnimationRegion> animatedRegions = FEEDING_ANIMATION_REGIONS.get(visual.textureName());
			for (int frame = 1; frame < 6; frame++) {
				int changedOutsideRegion = 0;
				for (int y = 0; y < 48; y++) {
					for (int x = 0; x < 48; x++) {
						if (image.getRGB(x, y) != image.getRGB(x, y + frame * 48)
								&& !isIntentionallyAnimated(animatedRegions, x, y)) {
							changedOutsideRegion++;
						}
					}
				}
				if (changedOutsideRegion < 8) {
					throw new AssertionError("Anchored pixels must follow the body for " + strain + " frame " + frame);
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

	private static void assertFloatEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001f) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
