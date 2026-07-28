package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Keeps successive rite-pillar particles visually continuous without letting
 * translucent copies stack over one another.
 */
public final class CardinalRitePillarTiming {
	public static final int SPAWN_INTERVAL_TICKS = 2;
	public static final int LIFETIME_TICKS = SPAWN_INTERVAL_TICKS;
	private static final float OPACITY = 0.82F;

	private CardinalRitePillarTiming() {
	}

	public static boolean isVisibleAtAge(int ageTicks) {
		return ageTicks >= 0 && ageTicks < LIFETIME_TICKS;
	}

	public static float opacityAtAge(int ageTicks) {
		return isVisibleAtAge(ageTicks) ? OPACITY : 0.0F;
	}
}
