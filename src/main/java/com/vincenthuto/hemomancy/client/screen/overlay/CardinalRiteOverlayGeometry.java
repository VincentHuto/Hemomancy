package com.vincenthuto.hemomancy.client.screen.overlay;

public final class CardinalRiteOverlayGeometry {
	public static final int HUD_WIDTH = 230;
	public static final int INSTABILITY_BAR_OFFSET = 82;

	private CardinalRiteOverlayGeometry() {
	}

	public static int instabilityBarWidth() {
		return HUD_WIDTH - INSTABILITY_BAR_OFFSET;
	}
}
