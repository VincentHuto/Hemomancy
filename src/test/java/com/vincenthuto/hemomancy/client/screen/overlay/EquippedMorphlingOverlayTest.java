package com.vincenthuto.hemomancy.client.screen.overlay;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public final class EquippedMorphlingOverlayTest {
	private static final Path TEXTURE_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/morphling_overlay");

	@Test
	void existingHudContractsRemainValid() throws Exception {
		main(new String[0]);
	}

	public static void main(String[] args) throws Exception {
		legacyIconPlacementRemainsAvailable();
		stableRenderClockAdvancesWithPartialTicks();
		bloodOverlayUsesStableRenderClock();
		lumenlaceItemUsesLumenlaceHudIdentity();
		lumenlaceItemModelUsesLumenlaceTexture();
		lumenlacePaletteCarriesBlueAndGoldNeuralSignals();
	}

	private static void legacyIconPlacementRemainsAvailable() {
		assertEquals("left-side blood bar places morphling icon to the right", 58,
				EquippedMorphlingOverlayPlacement.iconXForBloodBar(true, 4, 46));
		assertEquals("right-side blood bar places morphling icon to the left", 176,
				EquippedMorphlingOverlayPlacement.iconXForBloodBar(false, 200, 46));
		assertEquals("morphling icon is vertically centered on blood bar", 33,
				EquippedMorphlingOverlayPlacement.iconYForBloodBar(4, 75));
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
