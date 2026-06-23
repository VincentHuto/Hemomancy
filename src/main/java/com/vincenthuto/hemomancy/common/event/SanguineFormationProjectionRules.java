package com.vincenthuto.hemomancy.common.event;

public final class SanguineFormationProjectionRules {
	public static final double GENERIC_BLOOD_COST = 150.0D;
	public static final double ATTUNED_BLOOD_COST = 100.0D;
	public static final double GENERIC_FEED_MULTIPLIER = 1.0D;
	public static final double ATTUNED_FEED_MULTIPLIER = 2.0D;
	public static final double GENERIC_COLLAPSE_CHANCE = 0.25D;
	public static final double COLLAPSE_REDUCTION_PER_LEVEL = 0.05D;
	public static final int PROGRESS_TIMEOUT_TICKS = 100;

	private SanguineFormationProjectionRules() {
	}

	public static double bloodCost(boolean attuned) {
		return attuned ? ATTUNED_BLOOD_COST : GENERIC_BLOOD_COST;
	}

	public static double feedMultiplier(boolean attuned) {
		return attuned ? ATTUNED_FEED_MULTIPLIER : GENERIC_FEED_MULTIPLIER;
	}

	public static double collapseChance(boolean attuned, int skillLevel) {
		if (attuned) {
			return 0.0D;
		}
		return Math.max(0.0D, GENERIC_COLLAPSE_CHANCE - Math.max(0, skillLevel) * COLLAPSE_REDUCTION_PER_LEVEL);
	}

	public static double feedAmount(double currentProgress, boolean attuned, double availableBlood, double maxAmount) {
		double cost = bloodCost(attuned);
		double remaining = Math.max(0.0D, cost - currentProgress);
		if (remaining <= 0.0D || availableBlood <= 0.0D || maxAmount <= 0.0D) {
			return 0.0D;
		}
		double feedRate = maxAmount * feedMultiplier(attuned);
		return Math.min(remaining, Math.min(availableBlood, feedRate));
	}

	public static boolean isExpired(long gameTime, long lastFedGameTime) {
		return gameTime - lastFedGameTime > PROGRESS_TIMEOUT_TICKS;
	}
}
