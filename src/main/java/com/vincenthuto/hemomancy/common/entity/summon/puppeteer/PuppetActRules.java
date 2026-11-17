package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

/** The reaches, durations and cooldown arithmetic of the puppet acts. Starting values, tuned in play (D-37). */
public final class PuppetActRules {
	public static final double SPOTLIGHT_RADIUS = 12.0D;
	public static final double TRAPEZE_RANGE = 24.0D;
	public static final int TRAPEZE_GLIDE_TICKS = 40;
	public static final double FEINT_RADIUS = 10.0D;
	public static final double SMOKE_RADIUS = 6.0D;
	public static final int SMOKE_TICKS = 60;
	public static final double SCENT_RADIUS = 24.0D;
	public static final int SCENT_TICKS = 200;
	public static final double LIFT_REACH = 6.0D;
	public static final double LIFT_FORWARD = 1.1D;
	public static final double LIFT_UP = 0.9D;
	public static final int LIFT_GLIDE_TICKS = 40;
	/** The shortest wait between two act requests from one player, as the rate limiter counts it. */
	public static final int REQUEST_INTERVAL_TICKS = 5;

	private PuppetActRules() {
	}

	public static boolean isReady(long now, long readyAt) {
		return now >= readyAt;
	}

	/** Whole seconds left before {@code readyAt}, rounded up; at least 1 while waiting. */
	public static int secondsLeft(long now, long readyAt) {
		return readyAt <= now ? 0 : (int) ((readyAt - now + 19) / 20);
	}

	public static boolean withinTrapeze(double distanceSqr) {
		return distanceSqr <= TRAPEZE_RANGE * TRAPEZE_RANGE;
	}
}
