package com.vincenthuto.hemomancy.client.screen.overlay;

/**
 * Frame-rate-independent easing for the server-authoritative rite progress
 * shown by the HUD.
 */
final class CardinalRiteProgressSmoother {
	private static final double RESPONSE_PER_SECOND = 9.0D;
	private long riteKey = Long.MIN_VALUE;
	private double displayedProgress;

	double update(long newRiteKey, double targetProgress, double elapsedSeconds) {
		double target = clamp(targetProgress);
		if (riteKey != newRiteKey) {
			riteKey = newRiteKey;
			displayedProgress = target;
			return displayedProgress;
		}
		if (target < displayedProgress) {
			displayedProgress = target;
			return displayedProgress;
		}
		double elapsed = Math.max(0.0D, Math.min(0.10D, elapsedSeconds));
		double blend = 1.0D - Math.exp(-RESPONSE_PER_SECOND * elapsed);
		displayedProgress += (target - displayedProgress) * blend;
		if (target - displayedProgress < 0.0001D) displayedProgress = target;
		return displayedProgress;
	}

	void clear() {
		riteKey = Long.MIN_VALUE;
		displayedProgress = 0.0D;
	}

	private static double clamp(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}
}
