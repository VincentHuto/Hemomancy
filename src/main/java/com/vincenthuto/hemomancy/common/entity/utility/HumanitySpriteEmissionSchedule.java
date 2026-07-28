package com.vincenthuto.hemomancy.common.entity.utility;

/**
 * Staggers a full Humanity point-cloud refresh across consecutive ticks.
 */
public final class HumanitySpriteEmissionSchedule {
	private HumanitySpriteEmissionSchedule() {
	}

	public static boolean isDue(int pointIndex, int tick, int intervalTicks) {
		int interval = Math.max(1, intervalTicks);
		return Math.floorMod(pointIndex, interval) == Math.floorMod(tick, interval);
	}
}
