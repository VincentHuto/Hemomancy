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
		theMorphlingFitsInsideTheJarWindow();
		everyStrainHasItsHudIdentity();
	}

	private static void theMorphlingFitsInsideTheJarWindow() {
		float window = BloodHudLayout.JAR_WINDOW.w();
		float scale = EquippedMorphlingOverlayPlacement.jarScale(window);
		for (int i = 0; i <= 200; i++) {
			float size = EquippedMorphlingOverlayPlacement.ATTACHED_SIZE
					* EquippedMorphlingOverlayPlacement.morphlingRenderScale(scale, i / 100.0f);
			assertTrue("the feeding pulse never pushes the Morphling through the glass", size <= window + 0.001f);
		}
		assertTrue("it still fills most of the jar", EquippedMorphlingOverlayPlacement.ATTACHED_SIZE * scale > window * 0.85f);
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
