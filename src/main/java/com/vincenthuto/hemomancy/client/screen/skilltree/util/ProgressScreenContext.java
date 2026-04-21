package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import net.minecraft.client.gui.Font;

/**
 * Immutable per-frame context object shared across all tab-view helpers in
 * {@link HarbingerProgressScreen}.  Reduces the parameter count on every
 * static render / hit-test method by bundling the data that never changes
 * within a single render call.
 */
public record ProgressScreenContext(
		Font font,
		int guiLeft, int guiTop, int guiWidth, int guiHeight,
		int playerDegree) {

	/** Width of the tier sidebar used by the Rites, Crafting, and Scars tabs. */
	public static final int TIER_SIDEBAR_W = 130;

	/** X coordinate of the layer-control buttons (left edge of the 3D model area). */
	public int layerBtnX() { return guiLeft + TIER_SIDEBAR_W + 10; }

	/** Y centre of the layer-control buttons (vertically centred in the GUI). */
	public int layerBtnCenterY() { return guiTop + guiHeight / 2; }

	/** Visible height of the scrollable area inside any tier sidebar. */
	public int tierSidebarVisibleH() { return guiHeight - 42 - 4; }

	/** Returns {@code true} if the mouse position is within the tier sidebar region. */
	public boolean isOverTierSidebar(double mx, double my) {
		return mx >= guiLeft && mx <= guiLeft + TIER_SIDEBAR_W
			&& my >= guiTop  && my <= guiTop  + guiHeight;
	}
}
