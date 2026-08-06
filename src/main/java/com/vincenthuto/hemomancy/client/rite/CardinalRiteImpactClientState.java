package com.vincenthuto.hemomancy.client.rite;

/** Short-lived camera impulse for the daemon's arrival at its caster. */
public final class CardinalRiteImpactClientState {
	private int durationTicks;
	private int remainingTicks;
	private int seed;

	public void start(int durationTicks, int seed) {
		this.durationTicks = Math.max(1, durationTicks);
		this.remainingTicks = this.durationTicks;
		this.seed = seed;
	}

	public void tick() {
		if (remainingTicks > 0) remainingTicks--;
	}

	public void clear() {
		durationTicks = 0;
		remainingTicks = 0;
		seed = 0;
	}

	public boolean isActive() {
		return remainingTicks > 0;
	}

	public float pitchShake(float partialTick) {
		if (!isActive()) return 0.0F;
		float elapsed = durationTicks - remainingTicks + clamp(partialTick);
		float envelope = 1.0F - clamp(elapsed / durationTicks);
		float direction = (seed & 1) == 0 ? 1.0F : -1.0F;
		return direction * 3.2F * envelope
				* (float) Math.cos(elapsed * Math.PI * 1.35F);
	}

	public float rollShake(float partialTick) {
		return pitchShake(partialTick) * 0.42F;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
