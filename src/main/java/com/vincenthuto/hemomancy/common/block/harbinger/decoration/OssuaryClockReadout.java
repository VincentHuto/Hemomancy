package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

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

	/** The lunette's state (core-loop Track 10): 2 while a Blood Moon is up, 1 when tonight's chance is an omen, else 0. */
	public static int omen(boolean active, double chanceTonight) {
		if (active) return 2;
		return chanceTonight >= com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules.OMEN_CHANCE ? 1 : 0;
	}

	/** Tonight's chance as the dial says it: none, faint, possible, likely or strong. */
	public static String omenWord(double chanceTonight) {
		if (chanceTonight <= 0.0D) return "none";
		if (chanceTonight < 0.05D) return "faint";
		if (chanceTonight < 0.15D) return "possible";
		if (chanceTonight < 0.4D) return "likely";
		return "strong";
	}
}
