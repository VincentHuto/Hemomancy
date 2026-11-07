package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

/**
 * Arrested Pulse's pure rules (design revision 2026-11-06, R.5): the claw closes as round a heart and the challenger's
 * blood slows and crystallises. At 30, 60 and 90 ticks after the mark it beats: a challenger who averaged walking pace
 * over the 10 ticks before and stands under 70 stillness sheds the frost; anyone else has cruor spines burst out of them
 * (5, +15, Slowness I for 20), and anyone within 1.5 of them takes 5 and +10. A beat inside an opening passes. Searing,
 * another challenger's held Projection for 10 fed ticks, or a preservation's release ends the mark. JVM-tested.
 */
public final class VelorumPulseRules {
	/** Ticks after the mark that the beats strike, and the tell that shows each one first. */
	public static final int[] BEATS = {30, 60, 90};
	public static final int TELL = 12;
	/** Walking pace, averaged over the ticks before a beat: a sneak (about 0.065 a tick) never makes it. */
	public static final double PACE = 0.13D;
	public static final int PACE_WINDOW = 10;
	/** At or over 70 stillness the frost holds whatever the pace. */
	public static final float STILL_LIMIT = 70.0F;
	public static final float BURST_DAMAGE = 5.0F;
	public static final float BURST_STILLNESS = 15.0F;
	public static final int BURST_SLOW = 20;
	public static final double SPLASH = 1.5D;
	public static final float SPLASH_DAMAGE = 5.0F;
	public static final float SPLASH_STILLNESS = 10.0F;
	/** Another challenger's held Projection on the marked for this many fed ticks warms the mark out. */
	public static final int WARM_TICKS = 10;
	/** The mark's pressure: +12 on a target at 40 stillness or more. */
	public static final float COLD_AT = 0.4F;
	public static final int COLD_BONUS = 12;

	/** What a beat does. */
	public enum Beat { NONE, TELL, PASS, BURST }

	private VelorumPulseRules() {
	}

	/** The beat index (0 to 2) that strikes {@code sinceMark} ticks after the mark, or -1. */
	public static int beatAt(int sinceMark) {
		for (int i = 0; i < BEATS.length; i++) if (BEATS[i] == sinceMark) return i;
		return -1;
	}

	/** The beat index whose 12-tick tell begins {@code sinceMark} ticks after the mark, or -1. */
	public static int tellAt(int sinceMark) {
		for (int i = 0; i < BEATS.length; i++) if (BEATS[i] - TELL == sinceMark) return i;
		return -1;
	}

	/** Whether {@code sinceMark} falls inside a beat's 12-tick tell (the beat itself not included). */
	public static boolean telling(int sinceMark) {
		for (int beat : BEATS) if (sinceMark >= beat - TELL && sinceMark < beat) return true;
		return false;
	}

	/** The mark has run its course after its last beat. */
	public static boolean spent(int sinceMark) {
		return sinceMark >= BEATS[BEATS.length - 1];
	}

	/** Whether a beat passes the marked harmlessly: inside an opening, or walking under 70. */
	public static boolean passes(double averagePace, float stillness, boolean opening) {
		if (opening) return true;
		return averagePace >= PACE - 1.0E-9D && stillness < STILL_LIMIT;
	}

	/** What happens {@code sinceMark} ticks after the mark, for a challenger at this pace and stillness. */
	public static Beat beat(int sinceMark, double averagePace, float stillness, boolean opening) {
		if (beatAt(sinceMark) >= 0) return passes(averagePace, stillness, opening) ? Beat.PASS : Beat.BURST;
		if (tellAt(sinceMark) >= 0) return Beat.TELL;
		return Beat.NONE;
	}

	/** The mean of the last travel distances (blocks a tick), over at most the pace window. */
	public static double averagePace(double[] travel, int count) {
		int n = Math.min(Math.min(count, travel.length), PACE_WINDOW);
		if (n <= 0) return 0.0D;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) sum += travel[i];
		return sum / n;
	}

	/** Two marks in phase 3 with two or more challengers, else one. */
	public static int marks(int phase, int challengers) {
		return phase >= 3 && challengers >= 2 ? 2 : 1;
	}

	/** The mark ends on Searing, a finished warming, or a preservation's release. */
	public static boolean ends(boolean searing, int warmedTicks, boolean released) {
		return searing || warmedTicks >= WARM_TICKS || released;
	}

	/** The +12 the Pulse takes on a target at 40 or more (the meter as a fraction of its maximum). */
	public static int coldPressure(float targetMeter) {
		return targetMeter >= COLD_AT ? COLD_BONUS : 0;
	}
}
