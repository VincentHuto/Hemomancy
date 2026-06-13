package com.vincenthuto.hemomancy.common.effect;

public final class ChummedWatersRules {
	public static final int DURATION_TICKS = 2400;
	public static final double AREA_RADIUS = 6.0D;
	public static final double PROJECTILE_VELOCITY = 0.7D;
	public static final double PROJECTILE_INACCURACY = 0.55D;
	private static final int MIN_APPROACH_TICKS = 20;
	private static final int BASE_EXTRA_LURE_TICKS = 3;
	private static final int EXTRA_LURE_TICKS_PER_AMPLIFIER = 2;

	private ChummedWatersRules() {
	}

	public static int extraLureTicksPerTick(int amplifier) {
		return BASE_EXTRA_LURE_TICKS + Math.max(0, amplifier) * EXTRA_LURE_TICKS_PER_AMPLIFIER;
	}

	public static int adjustedLureTime(int currentTimeUntilLured, int amplifier) {
		if (currentTimeUntilLured <= MIN_APPROACH_TICKS) {
			return currentTimeUntilLured;
		}
		return Math.max(MIN_APPROACH_TICKS, currentTimeUntilLured - extraLureTicksPerTick(amplifier));
	}
}
