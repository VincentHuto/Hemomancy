package com.vincenthuto.hemomancy.common.event;

public final class BloodStructureFeedRules {
	public static final double STRUCTURE_FEED_RATE = 5.0;
	public static final double BLOOD_TILE_TRANSFER_RATE = 100.0;
	public static final double DEFAULT_FEED_RATE = STRUCTURE_FEED_RATE;
	public static final int PROGRESS_TIMEOUT_TICKS = 100;

	private BloodStructureFeedRules() {
	}

	public static double feedAmount(double currentProgress, double bloodCost, double availableBlood) {
		return feedAmount(currentProgress, bloodCost, availableBlood, DEFAULT_FEED_RATE);
	}

	public static double feedAmount(double currentProgress, double bloodCost, double availableBlood, double feedRate) {
		if (currentProgress >= bloodCost || bloodCost <= 0.0 || availableBlood <= 0.0) {
			return 0.0;
		}
		double remainingCost = Math.max(0.0, bloodCost - currentProgress);
		return Math.min(Math.max(0.0, feedRate), Math.min(remainingCost, availableBlood));
	}

	public static boolean isComplete(double currentProgress, double bloodCost) {
		return currentProgress >= bloodCost;
	}

	public static boolean isExpired(long gameTime, long lastFedGameTime) {
		return gameTime - lastFedGameTime > PROGRESS_TIMEOUT_TICKS;
	}

	public static boolean isCompletionLocked(long gameTime, long lockedUntilGameTime) {
		return gameTime <= lockedUntilGameTime;
	}
}
