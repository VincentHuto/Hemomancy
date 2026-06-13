package com.vincenthuto.hemomancy.common.effect;

public final class MnemonicCandleRules {
	public static final int AURA_INTERVAL_TICKS = 80;
	public static final int AURA_DURATION_TICKS = 120;
	public static final double AURA_RADIUS = 6.0D;

	private static final double COOLDOWN_MULTIPLIER = 0.90D;
	private static final double BLOOD_REGEN_PER_TICK = 1.5D;

	private MnemonicCandleRules() {
	}

	public static double manipulationCooldownMultiplier(boolean hasAura) {
		return hasAura ? COOLDOWN_MULTIPLIER : 1.0D;
	}

	public static double bonusBloodRegenPerTick(boolean hasAura) {
		return hasAura ? BLOOD_REGEN_PER_TICK : 0.0D;
	}
}
