package com.vincenthuto.hemomancy.client.screen.overlay;

public final class SanguineOmenOverlayState {
	private int durationTicks;
	private int remainingTicks;
	private float peakAlpha;
	private int seed;

	public void start(int durationTicks, float peakAlpha, int seed) {
		this.durationTicks = Math.max(1, durationTicks);
		this.remainingTicks = this.durationTicks;
		this.peakAlpha = clamp(peakAlpha, 0.0F, 1.0F);
		this.seed = seed;
	}

	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
	}

	public void clear() {
		durationTicks = 0;
		remainingTicks = 0;
		peakAlpha = 0.0F;
		seed = 0;
	}

	public boolean isActive() {
		return remainingTicks > 0;
	}

	public float alpha(float partialTicks) {
		if (!isActive()) {
			return 0.0F;
		}

		float progress = progress(partialTicks);
		float envelope;
		if (progress < 0.12F) {
			envelope = smooth(progress / 0.12F);
		} else if (progress < 0.68F) {
			envelope = 1.0F;
		} else {
			envelope = 1.0F - smooth((progress - 0.68F) / 0.32F);
		}

		float pulse = 0.94F + 0.06F * (float) Math.sin(progress * Math.PI * 8.0F);
		return clamp(peakAlpha * envelope * pulse, 0.0F, peakAlpha);
	}

	public float progress(float partialTicks) {
		if (durationTicks <= 0) {
			return 1.0F;
		}
		float elapsed = durationTicks - remainingTicks + clamp(partialTicks, 0.0F, 1.0F);
		return clamp(elapsed / durationTicks, 0.0F, 1.0F);
	}

	public int seed() {
		return seed;
	}

	private static float smooth(float value) {
		float clamped = clamp(value, 0.0F, 1.0F);
		return clamped * clamped * (3.0F - 2.0F * clamped);
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
}
