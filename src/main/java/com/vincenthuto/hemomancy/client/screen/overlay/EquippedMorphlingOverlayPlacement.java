package com.vincenthuto.hemomancy.client.screen.overlay;

final class EquippedMorphlingOverlayPlacement {
	static final int ICON_SIZE = 16;
	static final int ICON_GAP = 8;

	private EquippedMorphlingOverlayPlacement() {
	}

	static int iconXForBloodBar(boolean barOnLeft, int barX, int barWidth) {
		return barOnLeft ? barX + barWidth + ICON_GAP : barX - ICON_SIZE - ICON_GAP;
	}

	static int iconYForBloodBar(int barY, int barHeight) {
		return barY + barHeight / 2 - ICON_SIZE / 2;
	}
}
