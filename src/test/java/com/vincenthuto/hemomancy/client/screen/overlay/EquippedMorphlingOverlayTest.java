package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
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
		everyStrainHasItsHudIdentity();
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

	private static void everyStrainHasItsHudIdentity() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			MorphlingHudVisuals.Visual visual = MorphlingHudVisuals.forItemPath(strain.itemPath());
			if (visual == null) throw new AssertionError("Missing HUD visual for " + strain.itemPath());
			assertTrue(strain.id() + " HUD texture", strain.hudTexture().equals(visual.textureName()));
			assertTrue(strain.id() + " HUD accent", strain.accentColor() == visual.accentColor());
			assertTrue(strain.id() + " HUD sprite", Files.isRegularFile(TEXTURE_ROOT.resolve(visual.textureName() + ".png")));
		}
		assertTrue("unknown items have no HUD visual", MorphlingHudVisuals.forItemPath("morphling_jar") == null);
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
