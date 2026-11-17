package com.vincenthuto.hemomancy.client.screen.overlay;

final class EquippedMorphlingOverlayPlacement {
	static final int ICON_SIZE = 16;
	static final int ATTACHED_SIZE = 32;
	/** The feeding pulse's largest swell, so a Morphling sized to its jar never pushes through the glass. */
	private static final float MAX_PULSE = 1.0f + 0.032f + 0.018f;

	private EquippedMorphlingOverlayPlacement() {
	}

	/** The sprite scale that fits the Morphling, at the top of its pulse, inside a jar window {@code size} wide. */
	static float jarScale(float size) {
		return size / (ATTACHED_SIZE * MAX_PULSE);
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

	static SpriteBlit spriteBlit(boolean mirrored) {
		return mirrored
				? new SpriteBlit(ATTACHED_SIZE, ATTACHED_SIZE, -ATTACHED_SIZE)
				: new SpriteBlit(ATTACHED_SIZE, 0, ATTACHED_SIZE);
	}

	record SpriteBlit(int width, int uOffset, int uWidth) {
	}
}
