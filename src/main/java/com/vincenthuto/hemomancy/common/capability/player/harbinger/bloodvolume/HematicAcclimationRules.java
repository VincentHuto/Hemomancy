package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public final class HematicAcclimationRules {
	static final double MAX_EXPOSURE_ML = 1500.0D;
	private static final double DECAY_PER_TICK_ML = 0.25D;

	private HematicAcclimationRules() {
	}

	public static double multiplier(double exposureMl) {
		double exposure = clampedExposure(exposureMl);
		if (exposure < 750.0D) return 1.0D;
		if (exposure < 1250.0D) return 0.5D;
		if (exposure < MAX_EXPOSURE_ML) return 0.25D;
		return 0.0D;
	}

	public static double decayedExposure(double exposureMl, long elapsedTicks) {
		return Math.max(0.0D, clampedExposure(exposureMl) - Math.max(0L, elapsedTicks) * DECAY_PER_TICK_ML);
	}

	static double clampedExposure(double exposureMl) {
		return Double.isFinite(exposureMl) ? Math.max(0.0D, Math.min(MAX_EXPOSURE_ML, exposureMl)) : 0.0D;
	}
}
