package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import java.util.Arrays;

/**
 * Seraphae's pose as named channels the model lays on its joints. A frame is built in layers over these channels: the
 * idle of her phase and her irons, her glide (lean, strain, reel, turn), the playing track as deltas that start and end
 * at zero, the inertial blend out of the last shown pose, then the springs, which write the channels from
 * {@link #SECONDARY} on (how far a part trails what carries it). Angles are radians; {@link #LIFT} is model units.
 *
 * <p>Signs, as she faces: {@link #LEAN} forward, {@link #TWIST} and {@link #TURN} toward her right, {@link #SWAY} her
 * crown toward her left, {@link #ARCH} her chest open and head back, {@link #LIFT} up. An arm's raise lifts it forward,
 * out lifts it to the side, the elbow bends forward, the wrist flexes toward the palm, the hand opens (negative
 * closes). A wing's sweep carries it forward round her (negative draws it back), raise lifts it, fold closes it down
 * her back, spread fans its trunks apart, tips opens the dendrite fans. A trunk's curl bends it forward segment by
 * segment (negative arches it back).
 */
public final class SeraphaePose {
	public static final int LEAN = 0, TWIST = 1, SWAY = 2, ARCH = 3, LIFT = 4, TURN = 5, HEAD_PITCH = 6, HEAD_TURN = 7,
			HEAD_TILT = 8;
	public static final int ARM_L_RAISE = 9, ARM_L_OUT = 10, ARM_L_TWIST = 11, FORE_L = 12, WRIST_L = 13, HAND_L = 14,
			ARM_R_RAISE = 15, ARM_R_OUT = 16, ARM_R_TWIST = 17, FORE_R = 18, WRIST_R = 19, HAND_R = 20,
			WRISTS_CROSS = 21;
	public static final int WING_L_SWEEP = 22, WING_L_RAISE = 23, WING_L_FOLD = 24, WING_L_SPREAD = 25, TIPS_L = 26,
			WING_R_SWEEP = 27, WING_R_RAISE = 28, WING_R_FOLD = 29, WING_R_SPREAD = 30, TIPS_R = 31;
	/** Six of each, left upper, middle, lower, then right: see {@link #trunkCh}. */
	public static final int TRUNK_SWEEP = 32, TRUNK_RAISE = 38, TRUNK_CURL = 44;
	/** The knees folding, the feet trailing back, the palms planted on the floor (0 to 1), a seizure, a tremble. */
	public static final int KNEEL = 50, TRAIL = 51, PLANT = 52, JITTER = 53, TREMOR = 54;
	/** The first channel the springs own. */
	public static final int SECONDARY = 55;
	public static final int LAG_HEAD_X = 55, LAG_HEAD_Y = 56, LAG_HEAD_Z = 57, LAG_FORE_L = 58, LAG_FORE_R = 59,
			LAG_HAND_L = 60, LAG_HAND_R = 61, WING_LAG_L = 62, WING_LAG_R = 63, WING_DROP_L = 64, WING_DROP_R = 65;
	/** Six each: how far a trunk's outer segments trail its root, forward and up. */
	public static final int TRUNK_LAG = 66, TRUNK_DROP = 72;
	public static final int ROBE_X = 78, ROBE_Z = 79, VEIL_X = 80, VEIL_Z = 81, HAIR = 82, HALO_X = 83, HALO_Z = 84,
			CHAIN_X = 85, CHAIN_Z = 86, LEG_LAG = 87, FLINCH = 88;
	public static final int COUNT = 89;

	/** Offsets within an arm's block. */
	public static final int A_RAISE = 0, A_OUT = 1, A_TWIST = 2, A_FORE = 3, A_WRIST = 4, A_HAND = 5;
	/** Offsets within a wing's block. */
	public static final int W_SWEEP = 0, W_RAISE = 1, W_FOLD = 2, W_SPREAD = 3, W_TIPS = 4;
	/** Ticks a changed track blends in over (resolution B-8). */
	public static final float BLEND_TICKS = 5.0F;

	final float[] v = new float[COUNT];

	/** An arm channel: {@code part} is one of the {@code A_} offsets. */
	public static int armCh(boolean left, int part) {
		return (left ? ARM_L_RAISE : ARM_R_RAISE) + part;
	}

	/** A wing channel: {@code part} is one of the {@code W_} offsets. */
	public static int wingCh(boolean left, int part) {
		return (left ? WING_L_SWEEP : WING_R_SWEEP) + part;
	}

	/** A trunk's index: 0..2 on the left (upper, middle, lower), 3..5 on the right. */
	public static int trunkCh(boolean left, int place) {
		return (left ? 0 : 3) + place;
	}

	public float get(int channel) {
		return v[channel];
	}

	public SeraphaePose set(int channel, float value) {
		v[channel] = value;
		return this;
	}

	public SeraphaePose add(int channel, float value) {
		v[channel] += value;
		return this;
	}

	public SeraphaePose zero() {
		Arrays.fill(v, 0.0F);
		return this;
	}

	public SeraphaePose copyFrom(SeraphaePose other) {
		System.arraycopy(other.v, 0, v, 0, COUNT);
		return this;
	}

	/** The raw channels, for the tracks and the tests. */
	public float[] channels() {
		return v;
	}
}
