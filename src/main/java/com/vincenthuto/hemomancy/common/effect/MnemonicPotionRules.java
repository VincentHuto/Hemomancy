package com.vincenthuto.hemomancy.common.effect;

public final class MnemonicPotionRules {
	public static final int WHISPERS_DURATION_TICKS = 1200;
	public static final int SCREAMS_DURATION_TICKS = 1200;
	private static final double WHISPERS_COOLDOWN_MULTIPLIER = 0.75D;
	private static final double SCREAMS_COST_MULTIPLIER = 1.5D;

	private MnemonicPotionRules() {
	}

	public static double manipulationCooldownMultiplier(boolean hasMnemonicWhispers) {
		return hasMnemonicWhispers ? WHISPERS_COOLDOWN_MULTIPLIER : 1.0D;
	}

	public static double manipulationCostMultiplier(boolean hasMnemonicScreams) {
		return hasMnemonicScreams ? SCREAMS_COST_MULTIPLIER : 1.0D;
	}

	public static boolean shouldApplyScreamsOnWhispersDrink(boolean drinkingMnemonicWhispers,
			boolean alreadyHasMnemonicWhispers) {
		return drinkingMnemonicWhispers && alreadyHasMnemonicWhispers;
	}
}
