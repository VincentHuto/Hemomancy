package com.vincenthuto.hemomancy.client.player;

public final class LivingFlailImpactClientState {
	private static final int DURATION_TICKS = 10;
	private int remainingTicks;
	private float intensity;
	private int seed;

	public void start(float charge, int seed) {
		remainingTicks = DURATION_TICKS;
		intensity = 0.7F + clamp(charge) * 2.0F;
		this.seed = seed;
	}

	public void tick() {
		if (remainingTicks > 0) remainingTicks--;
	}

	public void clear() {
		remainingTicks = 0;
		intensity = 0.0F;
		seed = 0;
	}

	public boolean isActive() {
		return remainingTicks > 0;
	}

	public float pitchShake(float partialTick) {
		if (!isActive()) return 0.0F;
		float elapsed = DURATION_TICKS - remainingTicks + clamp(partialTick);
		float envelope = 1.0F - clamp(elapsed / DURATION_TICKS);
		float direction = (seed & 1) == 0 ? 1.0F : -1.0F;
		return direction * intensity * envelope * (float) Math.cos(elapsed * Math.PI * 1.25F);
	}

	public float rollShake(float partialTick) {
		return pitchShake(partialTick) * 0.38F;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
