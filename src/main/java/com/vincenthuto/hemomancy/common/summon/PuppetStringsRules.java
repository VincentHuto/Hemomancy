package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Timelines for a puppet arriving on and leaving by its strings. The server uses {@link #UNSPOOL_TICKS} to keep
 * the body as a shell until it has been hauled away; the client poses the body and draws the strings from the same
 * curves. Heights are blocks, angles radians, ages ticks since the cue. No Minecraft types, so JVM tests cover it.
 */
public final class PuppetStringsRules {
	public static final int DESCEND_TICKS = 14;
	/** Strings fall from above until here. */
	public static final int DROP_END = 4;
	/** The body is lowered onto its feet until here, then settles. */
	public static final int LOWER_END = 10;
	public static final int SETTLE_END = 12;
	public static final int UNSPOOL_TICKS = 12;
	/** Strings snap taut until here, then haul the body upward. */
	public static final int SNAP_END = 3;
	public static final int HAUL_END = 10;
	public static final double DESCEND_HEIGHT = 1.2D;
	public static final double HAUL_HEIGHT = 1.6D;
	/** How far above the body's head the unseen control hangs. */
	public static final double STRING_RISE = 2.5D;
	public static final int MIN_STRINGS = 3;
	public static final int MAX_STRINGS = 5;
	private static final double SETTLE_DIP = 0.06D;
	private static final double END_SCALE = 0.15D;
	private static final double MAX_TWIST = 0.9D;

	private PuppetStringsRules() {
	}

	/** Blocks above its resting place the arriving body hangs. */
	public static double descendOffset(double age) {
		if (age < DROP_END) return DESCEND_HEIGHT;
		if (age < LOWER_END) return DESCEND_HEIGHT * (1.0D - easeOut((age - DROP_END) / (LOWER_END - DROP_END)));
		if (age < SETTLE_END) return -SETTLE_DIP * Math.sin((age - LOWER_END) / (SETTLE_END - LOWER_END) * Math.PI);
		return 0.0D;
	}

	public static double descendScale(double age) {
		return 0.85D + 0.15D * HemoEasing.smoothstep(age / LOWER_END);
	}

	/** How far the falling strings have reached down to the body, 0 to 1. */
	public static double stringReach(double age) {
		return HemoEasing.smoothstep(age / DROP_END);
	}

	/** How high the knitting sinew has climbed the body, 0 to 1. */
	public static double knit(double age) {
		return HemoEasing.smoothstep((age - 2.0D) / (LOWER_END - 2.0D));
	}

	/** The arrival strings fraying away once the body stands. */
	public static double descendFray(double age) {
		return HemoEasing.smoothstep((age - LOWER_END) / (DESCEND_TICKS - LOWER_END));
	}

	/** Strings snapping taut, overshooting slightly before they hold. */
	public static double taut(double age) {
		if (age <= 0.0D) return 0.0D;
		if (age >= SNAP_END) return 1.0D;
		double t = age / SNAP_END;
		return Math.min(1.08D, HemoEasing.smoothstep(t) + 0.25D * Math.sin(t * Math.PI));
	}

	/** Blocks the departing body has been hauled up: held during the snap, then accelerating away. */
	public static double unspoolLift(double age) {
		if (age < SNAP_END) return 0.0D;
		if (age >= HAUL_END) return HAUL_HEIGHT;
		double t = (age - SNAP_END) / (HAUL_END - SNAP_END);
		return HAUL_HEIGHT * t * t;
	}

	public static double unspoolScale(double age) {
		if (age < SNAP_END) return 1.0D;
		double t = Math.min(1.0D, (age - SNAP_END) / (HAUL_END - SNAP_END));
		return 1.0D - (1.0D - END_SCALE) * t * t;
	}

	public static double unspoolTwist(double age) {
		return MAX_TWIST * HemoEasing.smoothstep((age - SNAP_END) / (HAUL_END - SNAP_END));
	}

	/** The body fraying into sinew as it rises, 0 to 1. */
	public static double unspoolFray(double age) {
		return HemoEasing.smoothstep((age - SNAP_END) / (HAUL_END - SNAP_END));
	}

	/** The flash that swallows the last of the body where it meets its strings. */
	public static double flash(double age) {
		double t = (age - (HAUL_END - 1.0D)) / 4.0D;
		return t <= 0.0D || t >= 1.0D ? 0.0D : Math.sin(t * Math.PI);
	}

	/** The puppeteer's control thread lets go as the strings above take the body. */
	public static double controlThreadFade(double age) {
		return 1.0D - HemoEasing.smoothstep(age / HAUL_END);
	}

	/** String count is carried in the cue's count, which must stay at least 1. */
	public static int strings(int count) {
		return Math.max(MIN_STRINGS, Math.min(MAX_STRINGS, count));
	}

	private static double easeOut(double t) {
		double c = Math.max(0.0D, Math.min(1.0D, t));
		return 1.0D - Math.pow(1.0D - c, 3.0D);
	}
}
