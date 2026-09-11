package com.vincenthuto.hemomancy.client.screen.overlay;

final class EquippedMorphlingOverlayPlacement {
	static final int ICON_SIZE = 16;
	static final int ICON_GAP = 8;
	static final int ATTACHED_SIZE = 32;
	// Pixel centers keep the feeding pixel on the vessel edge after mirroring.
	static final float MOUTH_X = 5.5f;
	static final float MOUTH_Y = 16.5f;

	private EquippedMorphlingOverlayPlacement() {
	}

	static int iconXForBloodBar(boolean barOnLeft, int barX, int barWidth) {
		return barOnLeft ? barX + barWidth + ICON_GAP : barX - ICON_SIZE - ICON_GAP;
	}

	static int iconYForBloodBar(int barY, int barHeight) {
		return barY + barHeight / 2 - ICON_SIZE / 2;
	}

	static Attachment attachment(boolean barOnLeft, float vesselEdgeX, float vesselCenterY, float scale) {
		return new Attachment(vesselEdgeX, vesselCenterY,
				barOnLeft ? MOUTH_X : ATTACHED_SIZE - MOUTH_X, scale);
	}

	static boolean shouldMirror(boolean barOnLeft) {
		return !barOnLeft;
	}

	static float feedingPulseScale(float timeSeconds) {
		float cycleSeconds = 0.9f;
		float phase = timeSeconds - (float) Math.floor(timeSeconds / cycleSeconds) * cycleSeconds;
		return 1.0f + heartbeatPulse(phase, 0.08f, 0.032f, 0.045f)
				+ heartbeatPulse(phase, 0.21f, 0.018f, 0.035f);
	}

	static float morphlingRenderScale(float configuredScale, float timeSeconds) {
		return configuredScale * feedingPulseScale(timeSeconds);
	}

	private static float heartbeatPulse(float phase, float center, float amplitude, float width) {
		float distance = (phase - center) / width;
		return amplitude * (float) Math.exp(-(distance * distance));
	}

	static float animationTimeSeconds(long gameTime, float partialTicks) {
		float clampedPartialTicks = Math.max(0.0f, Math.min(1.0f, partialTicks));
		return (gameTime + clampedPartialTicks) / 20.0f;
	}

	static SpriteBlit spriteBlit(boolean mirrored) {
		return mirrored
				? new SpriteBlit(ATTACHED_SIZE, ATTACHED_SIZE, -ATTACHED_SIZE)
				: new SpriteBlit(ATTACHED_SIZE, 0, ATTACHED_SIZE);
	}

	record Attachment(float anchorX, float anchorY, float mouthX, float scale) {
		float left() { return anchorX - mouthX * scale; }
		float top() { return anchorY - MOUTH_Y * scale; }
		float size() { return ATTACHED_SIZE * scale; }

		int bondMeterX(boolean barOnLeft) {
			if (top() < 18) {
				return Math.round(barOnLeft ? left() + size() + 4 : left() - 36);
			}
			return Math.round(barOnLeft ? anchorX + 4 : anchorX - 36);
		}

		int bondMeterY() {
			return Math.round(top() < 18 ? Math.max(2, anchorY - 6) : top() - 16);
		}
	}

	record SpriteBlit(int width, int uOffset, int uWidth) {
	}
}
