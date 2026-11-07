package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The 100-tick absolution ending (design section 11, resolution B-10: a reload resumes it). The beats are pure
 * envelopes of the ending tick, read by the server for its sounds and lines and by the client for the body.
 * <pre>
 *   0..20   the rot stills: kneel, the growths blacken (char to 0.9)
 *  20..50   the last cleansing: crimson fire climbs (ember), the growths burn to stubs, the man's face shows
 *  50..70   the fire dies: ember to soot, spores and fine threads lift
 *  70..100  rest: the body sinks into the sarcophagus; the line
 *  100      endTrial once; the arena restores the chamber as built
 * </pre>
 */
public final class PutricielEndingRules {
	public static final int END_TRIAL = 100;
	public static final int LENGTH = 101;
	public static final int KNEEL_END = 20;
	public static final int BURN_START = 20;
	public static final int BURN_END = 50;
	public static final int SOOT_END = 70;
	public static final int SINK_START = 70;
	public static final int LINE_TICK = 70;
	/** Hazards and faithful burn out over this many ticks from the ending's start. */
	public static final int BURN_OUT = 10;

	public static final SaintEnding.Spec SPEC = new SaintEnding.Spec(END_TRIAL, LENGTH, false, true,
			SaintArena.RestoreOrder.IMMEDIATE, 0);

	private PutricielEndingRules() {
	}

	public static float kneel(float t) {
		return HemoEasing.ramp(t, 0.0F, KNEEL_END);
	}

	/** The char layer's alpha: up to 0.9 by tick 20, gone with the soot by 70. */
	public static float charAlpha(float t) {
		return 0.9F * HemoEasing.ramp(t, 0.0F, KNEEL_END) * (1.0F - 0.6F * HemoEasing.ramp(t, BURN_END, SOOT_END));
	}

	/** The ember layer: rising through the burn, fading to soot. */
	public static float ember(float t) {
		return HemoEasing.window(t, BURN_START - 2.0F, BURN_START + 10.0F, BURN_END, SOOT_END);
	}

	/** The shelves, sacs and mushrooms burn down to stubs. */
	public static float growth(float t) {
		return 1.0F - 0.8F * HemoEasing.ramp(t, BURN_START, BURN_END);
	}

	/** The man's face, held still for 30 ticks. */
	public static float face(float t) {
		return HemoEasing.window(t, BURN_START, BURN_START + 6.0F, BURN_END - 4.0F, BURN_END + 2.0F);
	}

	/** The sink into the sarcophagus, 0 to 1. */
	public static float sink(float t) {
		return HemoEasing.ramp(t, SINK_START, END_TRIAL);
	}

	/** The chamber's pyres flare tall in the hot palette through the burn. */
	public static float pyreFlare(float t) {
		return HemoEasing.window(t, BURN_START, BURN_START + 8.0F, BURN_END, SOOT_END);
	}
}
