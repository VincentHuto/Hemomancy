package com.vincenthuto.hemomancy.common.entity.mob.monster;

/**
 * An Abhorrent Thought is a hazard to route around for a possessing will: its gaze drains Concentration, and its
 * grab breaks the will outright. Toward everyone else it is the same thought it always was.
 */
public final class AbhorrentGazeRules {
	public static final double GAZE_RANGE = 10.0;
	public static final int GAZE_TICKS = 20;
	public static final float GAZE_COST = 1f;
	public static final int GAZE_DARKNESS_TICKS = 40;
	public static final int GRAB_MIND_BROKEN_TICKS = 12000;

	public enum Grab { TOTEM, EJECT }

	private AbhorrentGazeRules() {}

	/** A grab breaks the will outright, unless a totem offering is still unspent: it saves the visit once. */
	public static Grab grab(boolean totem) {
		return totem ? Grab.TOTEM : Grab.EJECT;
	}

	public static boolean gazes(boolean possessed, double distanceSqr, boolean sight) {
		return possessed && sight && distanceSqr <= GAZE_RANGE * GAZE_RANGE;
	}
}
