package com.vincenthuto.hemomancy.common.block.shared;

public final class OssuaryClockReadout {
	private static final String[] MOON_PHASES = {
			"full_moon", "waning_gibbous", "last_quarter", "waning_crescent",
			"new_moon", "waxing_crescent", "first_quarter", "waxing_gibbous"
	};

	private OssuaryClockReadout() {
	}

	public static String timeBand(long dayTime) {
		long time = Math.floorMod(dayTime, 24000L);
		if (time < 1000L) {
			return "dawn";
		}
		if (time < 12000L) {
			return "day";
		}
		if (time < 14000L) {
			return "dusk";
		}
		return "night";
	}

	public static String moonPhase(long dayCount) {
		return MOON_PHASES[(int) Math.floorMod(dayCount, MOON_PHASES.length)];
	}
}
