package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import net.minecraft.util.Mth;

public final class SporiticThuribleRules {
	public static final int REQUIRED_DEGREE = 4;
	public static final int DEFAULT_BURN_TICKS = 20 * 60 * 5;
	public static final int UPKEEP_INTERVAL_TICKS = 20;
	public static final int AMBIENT_PARTICLE_INTERVAL_TICKS = 4;
	public static final int AURA_INTERVAL_TICKS = 40;
	public static final int RESONANCE_DURATION_TICKS = 60;
	public static final int BLOOD_LOSS_DURATION_TICKS = 4;
	public static final int WITHER_DURATION_TICKS = 45;
	private static final double BASE_BLOOD_DRAIN_PER_SECOND = 4.0;
	private static final double SWING_BLOOD_DRAIN_PER_SECOND = 12.0;
	private static final double BASE_AURA_RADIUS = 2.5;
	private static final double SWING_AURA_RADIUS = 2.5;

	private SporiticThuribleRules() {
	}

	public static double bloodDrainPerSecond(double swingIntensity) {
		double clamped = Mth.clamp(swingIntensity, 0.0, 1.0);
		return BASE_BLOOD_DRAIN_PER_SECOND + SWING_BLOOD_DRAIN_PER_SECOND * clamped;
	}

	public static double auraRadius(double swingIntensity) {
		double clamped = Mth.clamp(swingIntensity, 0.0, 1.0);
		return BASE_AURA_RADIUS + SWING_AURA_RADIUS * clamped;
	}

	public static double burnFraction(int burnTicks, int maxBurnTicks) {
		if (maxBurnTicks <= 0) {
			return 0.0;
		}
		return Mth.clamp(burnTicks / (double) maxBurnTicks, 0.0, 1.0);
	}

	public static int remainingBurnTicks(long currentGameTime, long burnEndGameTime) {
		return (int) Math.max(0L, burnEndGameTime - currentGameTime);
	}
}
