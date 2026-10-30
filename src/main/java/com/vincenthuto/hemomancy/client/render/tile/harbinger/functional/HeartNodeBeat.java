package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;

/**
 * How a heart-node's heart beats. Every pace is locked to the spread's clock ({@link HeartNodeRules#HEARTBEAT_TICKS}):
 * the first thump of a beat peaks at {@link #THUMP_PEAK}, the moment the spread's pulse ring leaves the heart, so a
 * quick heart beats twice a pulse and a slow one every other pulse, never out of step with the ground.
 */
public final class HeartNodeBeat {
	/** Ticks into a heartbeat at which the first thump peaks: the spread shader's ring centre. */
	public static final float THUMP_PEAK = 3.0F;
	private static final float LUB_ATTACK = 1.5F;
	private static final float LUB_LENGTH = 6.0F;
	/** The second, softer thump: when it peaks after the first, and how strong it is. */
	private static final float DUB_AT = 5.5F;
	private static final float DUB_ATTACK = 1.2F;
	private static final float DUB_LENGTH = 5.0F;
	private static final float DUB_STRENGTH = 0.6F;
	/** How much a full thump swells the heart. */
	public static final float SWELL = 0.1F;
	/** The planting's three hard beats: when the first lands after planting, and the gap between them. */
	public static final float PLANT_FIRST = 8.0F;
	public static final float PLANT_GAP = 6.0F;
	public static final int PLANT_BEATS = 3;
	/** heart_node.py HEART_SCALE: the heart fills out as the node matures. */
	private static final float[] RESTING_SCALE = { 0.72F, 0.86F, 1.0F, 1.0F };

	/** How fast the heart goes, by where its owner is. */
	public enum Pace {
		/** The owner stands in the heart lobe. */
		QUICK(HeartNodeRules.HEARTBEAT_TICKS / 2),
		/** The owner is elsewhere in their envelope. */
		STEADY(HeartNodeRules.HEARTBEAT_TICKS),
		/** The owner is away, or a stranger is looking. */
		SLOW(HeartNodeRules.HEARTBEAT_TICKS * 2);

		public final int period;

		Pace(int period) {
			this.period = period;
		}
	}

	private HeartNodeBeat() {
	}

	/**
	 * The pace a viewer sees: strangers always a slow, steady beat; the owner quick in the heart lobe, steady elsewhere
	 * in the envelope, slow outside it.
	 */
	public static Pace pace(boolean owner, boolean inEnvelope, boolean inHeartLobe) {
		if (!owner || !inEnvelope) return Pace.SLOW;
		return inHeartLobe ? Pace.QUICK : Pace.STEADY;
	}

	/** Ticks into the current beat at {@code time}. */
	public static double phase(double time, Pace pace) {
		double period = pace.period;
		return time - Math.floor(time / period) * period;
	}

	/** Which beat {@code time} falls in, counted from the first thump's peak; it ticks over once per beat. */
	public static long beatIndex(double time, Pace pace) {
		return (long) Math.floor((time - THUMP_PEAK) / pace.period);
	}

	/** The double thump at {@code time}: 1 at the first thump's peak, about {@link #DUB_STRENGTH} at the second. */
	public static float thump(double time, Pace pace) {
		return doubleThump((float) phase(time, pace));
	}

	/** The double thump {@code phase} ticks into a beat. */
	public static float doubleThump(float phase) {
		float lub = HemoEasing.pulse(phase - (THUMP_PEAK - LUB_ATTACK), LUB_ATTACK, LUB_LENGTH);
		float dub = HemoEasing.pulse(phase - (THUMP_PEAK + DUB_AT - DUB_ATTACK), DUB_ATTACK, DUB_LENGTH);
		return Math.max(lub, DUB_STRENGTH * dub);
	}

	/** The planting's three hard beats, {@code age} ticks after planting: 0 outside them. */
	public static float plantingBeats(float age) {
		float strongest = 0.0F;
		for (int beat = 0; beat < PLANT_BEATS; beat++) {
			float peak = PLANT_FIRST + beat * PLANT_GAP;
			strongest = Math.max(strongest, HemoEasing.pulse(age - (peak - LUB_ATTACK), LUB_ATTACK, LUB_LENGTH));
		}
		return strongest;
	}

	/** The heart's size at rest at {@code stage}. */
	public static float restingScale(int stage) {
		return RESTING_SCALE[Math.max(0, Math.min(RESTING_SCALE.length - 1, stage))];
	}

	/** The heart's scale from its resting size with a thump of {@code thump} (a hard planting beat may pass 1). */
	public static float scale(float restingScale, float thump) {
		return restingScale * (1.0F + SWELL * thump);
	}
}
