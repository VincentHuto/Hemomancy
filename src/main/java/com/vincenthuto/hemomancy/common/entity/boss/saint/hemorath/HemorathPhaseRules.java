package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * Phases and the two roads to the ending (design sections 3 and 11). Phase 2 comes at half health or with the first
 * full meter in phase 1, which empties him instead of ending the fight (the tide, 15% of his health torn off, the meter
 * left at 30%). The Last Thirst runs once in phase 2 at 20% health and ends in overload, his fall or a forced vent after
 * 200 ticks. A full meter in phase 2, or his last health point, starts the ending. JVM-tested.
 */
public final class HemorathPhaseRules {
	public static final float PHASE_2_HEALTH = 0.5F;
	public static final float THIRST_HEALTH = 0.2F;
	public static final float EMPTIES_TEAR = 0.15F;
	public static final int THIRST_TICKS = 200;
	/** Phase 2 takes 15% off every wind-up (never under the floors) and 4 ticks off each decision delay. */
	public static final float PHASE_2_WINDUP_CUT = 0.15F;
	public static final int PHASE_2_DECISION_CUT = 4;
	/** The Last Thirst's decision delay while he is rooted. */
	public static final int THIRST_DECISION_DELAY = 30;
	/** The Spiked Halo's Crown Fall follow-up below this health. */
	public static final float CROWN_FALL_HEALTH = 0.35F;

	public enum Overload { EMPTIES, ENDS }

	public enum ThirstExit { NONE, OVERLOAD, FALL, TIMEOUT }

	private HemorathPhaseRules() {
	}

	/** What a full meter does: the first in phase 1 empties him; any later one ends the trial. */
	public static Overload overload(int phase) {
		return phase >= 2 ? Overload.ENDS : Overload.EMPTIES;
	}

	/** The health the phase-1 overload leaves: 15% of his maximum torn off, never under 1. */
	public static float tornHealth(float health, float max) {
		return Math.max(1.0F, health - max * EMPTIES_TEAR);
	}

	/** The Last Thirst is due in phase 2 at 20% health, once per fight. */
	public static boolean thirstDue(int phase, float healthFraction, boolean used) {
		return !used && phase >= 2 && healthFraction <= THIRST_HEALTH;
	}

	/** How the Last Thirst ends this tick, or NONE. */
	public static ThirstExit thirstExit(int ticksIn, boolean overloaded, boolean fell) {
		if (overloaded) return ThirstExit.OVERLOAD;
		if (fell) return ThirstExit.FALL;
		return ticksIn >= THIRST_TICKS ? ThirstExit.TIMEOUT : ThirstExit.NONE;
	}

	public static int decisionDelay(int stateDelay, int phase, boolean thirst) {
		if (thirst) return THIRST_DECISION_DELAY;
		return phase >= 2 ? Math.max(1, stateDelay - PHASE_2_DECISION_CUT) : stateDelay;
	}
}
