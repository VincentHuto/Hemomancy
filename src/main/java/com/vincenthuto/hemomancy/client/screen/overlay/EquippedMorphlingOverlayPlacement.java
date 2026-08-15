package com.vincenthuto.hemomancy.client.screen.overlay;

final class EquippedMorphlingOverlayPlacement {
	static final int ICON_SIZE = 16;
	static final int ICON_GAP = 8;
	static final int ATTACHED_SIZE = 48;
	static final int ATTACHED_OVERLAP = ATTACHED_SIZE / 2;
	static final int FEEDING_FRAME_COUNT = 6;
	static final int FEEDING_TEXTURE_HEIGHT = ATTACHED_SIZE * FEEDING_FRAME_COUNT;
	private static final float FEEDING_FRAME_SECONDS = 0.18f;
	private static final float FEEDING_PULSE_CYCLE_SECONDS = 0.9f;

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

	static int feedingFrame(float timeSeconds) {
		return feedingFrame(timeSeconds, true);
	}

	static int feedingFrame(float timeSeconds, boolean animationEnabled) {
		if (!animationEnabled) {
			return 0;
		}
		int elapsedFrames = (int) Math.floor(timeSeconds / FEEDING_FRAME_SECONDS);
		return Math.floorMod(elapsedFrames, FEEDING_FRAME_COUNT);
	}

	static float feedingPulseScale(float timeSeconds) {
		float phase = timeSeconds
				- (float) Math.floor(timeSeconds / FEEDING_PULSE_CYCLE_SECONDS) * FEEDING_PULSE_CYCLE_SECONDS;
		float primaryBeat = heartbeatPulse(phase, 0.08f, 0.032f, 0.045f);
		float secondaryBeat = heartbeatPulse(phase, 0.21f, 0.018f, 0.035f);
		return 1.0f + primaryBeat + secondaryBeat;
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

	record SpriteBlit(int width, int uOffset, int uWidth) {
	}
}
