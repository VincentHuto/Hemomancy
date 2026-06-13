package com.vincenthuto.hemomancy.common.block.shared;

public final class HumoralBarometerReadout {
	private HumoralBarometerReadout() {
	}

	public static String weatherState(boolean raining, boolean thundering) {
		if (thundering) {
			return "thunder";
		}
		return raining ? "rain" : "clear";
	}

	public static String bloodMoonPressure(boolean active, long remainingTicks) {
		if (!active) {
			return "quiet";
		}
		long remainingMinutes = Math.max(1L, (remainingTicks + 1199L) / 1200L);
		return "active_" + remainingMinutes + "m";
	}
}
