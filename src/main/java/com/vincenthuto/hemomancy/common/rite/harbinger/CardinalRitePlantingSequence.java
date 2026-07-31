package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.util.Mth;

/** Shared timing and easing for the Living Staff's Cardinal Rite planting motion. */
public final class CardinalRitePlantingSequence {
	public static final int DURATION_TICKS = 22;
	public static final int IMPACT_TICK = 14;
	private static final int WINDUP_END_TICK = 7;

	private CardinalRitePlantingSequence() {
	}

	public static boolean isAnimating(int elapsedTicks) {
		return elapsedTicks >= 0 && elapsedTicks < DURATION_TICKS;
	}

	public static boolean isPlanted(int elapsedTicks) {
		return elapsedTicks >= IMPACT_TICK;
	}

	public static float windupProgress(float elapsedTicks) {
		float progress = Mth.clamp(elapsedTicks / WINDUP_END_TICK, 0.0F, 1.0F);
		return smoothstep(progress);
	}

	public static float strikeProgress(float elapsedTicks) {
		if (elapsedTicks <= WINDUP_END_TICK) return 0.0F;
		float progress = Mth.clamp(
				(elapsedTicks - WINDUP_END_TICK) / (IMPACT_TICK - WINDUP_END_TICK),
				0.0F, 1.0F);
		return progress * progress * progress;
	}

	public static float recoveryProgress(float elapsedTicks) {
		if (elapsedTicks <= IMPACT_TICK) return 0.0F;
		float progress = Mth.clamp(
				(elapsedTicks - IMPACT_TICK) / (DURATION_TICKS - IMPACT_TICK),
				0.0F, 1.0F);
		return smoothstep(progress);
	}

	public static float cameraPitchShake(float elapsedTicks) {
		float sinceImpact = elapsedTicks - IMPACT_TICK;
		if (sinceImpact < 0.0F || elapsedTicks >= DURATION_TICKS) return 0.0F;
		return (float) (Math.exp(-0.32D * sinceImpact) * Math.cos(1.7D * sinceImpact));
	}

	private static float smoothstep(float value) {
		return value * value * (3.0F - 2.0F * value);
	}
}
