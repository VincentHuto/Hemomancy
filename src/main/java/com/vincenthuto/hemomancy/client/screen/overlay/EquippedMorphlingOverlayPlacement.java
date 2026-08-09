package com.vincenthuto.hemomancy.client.screen.overlay;

final class EquippedMorphlingOverlayPlacement {
	static final int ICON_SIZE = 16;
	static final int ICON_GAP = 8;
	static final int ATTACHED_SIZE = 48;
	static final int ATTACHED_OVERLAP = ATTACHED_SIZE / 2;

	private EquippedMorphlingOverlayPlacement() {
	}

	static int iconXForBloodBar(boolean barOnLeft, int barX, int barWidth) {
		return barOnLeft ? barX + barWidth + ICON_GAP : barX - ICON_SIZE - ICON_GAP;
	}

	static int iconYForBloodBar(int barY, int barHeight) {
		return barY + barHeight / 2 - ICON_SIZE / 2;
	}

	static int attachedXForBloodBar(boolean barOnLeft, int barX, int barWidth) {
		return barOnLeft
				? barX + barWidth - ATTACHED_OVERLAP
				: barX - ATTACHED_SIZE + ATTACHED_OVERLAP;
	}

	static int attachedYForBloodBar(int barY, int barHeight) {
		return barY + barHeight / 2 - ATTACHED_SIZE / 2;
	}

	static boolean shouldMirror(boolean barOnLeft) {
		return !barOnLeft;
	}

	static SpriteBlit spriteBlit(boolean mirrored) {
		return mirrored
				? new SpriteBlit(ATTACHED_SIZE, ATTACHED_SIZE, -ATTACHED_SIZE)
				: new SpriteBlit(ATTACHED_SIZE, 0, ATTACHED_SIZE);
	}

	record SpriteBlit(int width, int uOffset, int uWidth) {
	}
}
