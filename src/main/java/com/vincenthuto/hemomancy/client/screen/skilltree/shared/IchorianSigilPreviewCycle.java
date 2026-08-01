package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public final class IchorianSigilPreviewCycle {
	private static final int LOOP_TICKS = 160;

	private IchorianSigilPreviewCycle() {
	}

	static Sample sample(long elapsedTicks) {
		return sample(elapsedTicks, 0.0F);
	}

	public static Sample sample(long elapsedTicks, float partialTick) {
		float animationAge = Math.max(0.0F, elapsedTicks + partialTick);
		float phase = animationAge % LOOP_TICKS;
		float morphAge;
		if (phase < 20.0F) {
			morphAge = 0.0F;
		} else if (phase < 60.0F) {
			morphAge = phase - 20.0F;
		} else if (phase < 100.0F) {
			morphAge = 40.0F;
		} else if (phase < 140.0F) {
			morphAge = 140.0F - phase;
		} else {
			morphAge = 0.0F;
		}
		return new Sample(morphAge, animationAge);
	}

	public record Sample(float morphAgeTicks, float animationAgeTicks) {
	}
}
