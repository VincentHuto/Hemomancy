package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** FX-20: the Living Bestiary keeps its creature preview at GUI scale 3. */
class BestiaryTabLayoutTest {
	/** The progress screen's 16 px margin on each side. */
	private static int guiWidth(int framebufferWidth, int scale) {
		return (int) Math.ceil(framebufferWidth / (double) scale) - 32;
	}

	@Test
	void thePreviewShowsAtScaleThreeOnA1280Screen() {
		int gui = guiWidth(1280, 3);
		assertTrue(BestiaryTabView.detailHoldsPreview(BestiaryTabView.detailWidth(gui)),
				"no room for the preview at 1280 by 720, scale 3 (list " + BestiaryTabView.listWidth(gui) + ")");
		assertEquals(130, BestiaryTabView.listWidth(gui));
	}

	@Test
	void theListKeepsItsWidthWhereThePreviewAlreadyFits() {
		assertEquals(196, BestiaryTabView.listWidth(guiWidth(1920, 3)));
		assertEquals(220, BestiaryTabView.listWidth(guiWidth(1920, 1)));
		assertTrue(BestiaryTabView.detailHoldsPreview(BestiaryTabView.detailWidth(guiWidth(1920, 3))));
	}
}
