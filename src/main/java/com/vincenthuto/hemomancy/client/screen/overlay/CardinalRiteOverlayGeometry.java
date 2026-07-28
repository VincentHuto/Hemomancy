package com.vincenthuto.hemomancy.client.screen.overlay;

public final class CardinalRiteOverlayGeometry {
	public static final int HUD_WIDTH = 230;
	public static final int INSTABILITY_BAR_OFFSET = 82;
	public static final float HUD_SCALE = 0.72F;

	private CardinalRiteOverlayGeometry() {
	}

	public static int instabilityBarWidth() {
		return HUD_WIDTH - INSTABILITY_BAR_OFFSET;
	}

	public static int renderedHudWidth() {
		return Math.round(HUD_WIDTH * HUD_SCALE);
	}

	public static int virtualScreenWidth(int screenWidth) {
		return Math.round(screenWidth / HUD_SCALE);
	}
}
