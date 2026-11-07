package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The stalk as pure numbers (design section 8): a staff-planted three-beat gait keyed to the distance he has walked,
 * not to time, so a planted foot or a planted staff never skates. Each cycle the left foot swings in its first third,
 * the right foot in its second, and the staff swings forward in its last and plants as the cycle turns over. A planted
 * foot sweeps back under him at exactly the speed the body moves forward. Also the legs' fold: the knees bend to
 * crouch him and the hips drop until the lower foot meets the floor. Model units throughout (render scale 2: eight to
 * a block). JVM-tested.
 */
public final class HemorathGait {
	/** Model units in a block at render scale 2. */
	public static final float BLOCK = 8.0F;
	/** The thigh and the shin to the sole, in model units. */
	public static final float THIGH = 6.0F;
	public static final float SHIN = 6.25F;
	/** A cycle's length in blocks walked: long hungry strides when hollow, short heavy ones when full. */
	public static final float CYCLE_SUNKEN = 1.9F;
	public static final float CYCLE_BRIMMING = 1.5F;
	/** The swing windows' starts: left foot, right foot, staff. */
	public static final float LEFT = 0.0F, RIGHT = 1.0F / 3.0F, STAFF = 2.0F / 3.0F;
	/** How high the swinging staff foot lifts, in model units. */
	public static final float STAFF_LIFT = 2.5F;
	/** The knees fold this much per unit of crouch and of slump; never past this (a full kneel). */
	public static final float FOLD_PER_CROUCH = 0.42F;
	public static final float FOLD_PER_SLUMP = 0.6F;
	public static final float FOLD_MAX = 1.45F;

	private static final float THIRD = 1.0F / 3.0F;

	private HemorathGait() {
	}

	/** Blocks walked per three-beat cycle at meter fraction {@code swell}. */
	public static float cycleBlocks(float swell) {
		return HemoEasing.lerp(CYCLE_SUNKEN, CYCLE_BRIMMING, HemoEasing.smoothstep(swell));
	}

	/**
	 * The cycle's phase in [0, 1) from the walk animation's position, which grows by four a block while he walks under
	 * a quarter of a block a tick.
	 */
	public static float phase(float limbSwing, float swell) {
		return wrap(limbSwing / (4.0F * cycleBlocks(swell)));
	}

	/** How far a planted foot travels under him, front to back, in model units. */
	public static float stanceTravel(float swell) {
		return cycleBlocks(swell) * BLOCK * (1.0F - THIRD);
	}

	/** The thigh's swing either side of plumb, radians, so the planted foot covers the stance travel. */
	public static float amplitude(float swell) {
		float sine = stanceTravel(swell) * 0.5F / (THIGH + SHIN);
		return (float) Math.asin(Math.min(0.9F, sine));
	}

	/**
	 * A leg's thigh angle at phase {@code p} (positive: foot behind). It swings forward through the window starting at
	 * {@code start}, fast through the middle, then sweeps back planted for the other two thirds.
	 */
	public static float legSwing(float p, float start, float amplitude) {
		float sine = (float) Math.sin(amplitude);
		float into = wrap(p - start);
		float along;
		if (into < THIRD) along = HemoEasing.lerp(1.0F, -1.0F, HemoEasing.smoothstep(into / THIRD));
		else along = HemoEasing.lerp(-1.0F, 1.0F, (into - THIRD) / (1.0F - THIRD));
		return (float) Math.asin(along * sine);
	}

	/** 0 to 1 to 0 across a swing window (the foot or the staff off the floor), else 0. */
	public static float lift(float p, float start) {
		float into = wrap(p - start);
		return into < THIRD ? (float) Math.sin(Math.PI * into / THIRD) : 0.0F;
	}

	/**
	 * The planted staff foot's offset along his facing (model z, negative ahead), keeping pace with the ground while
	 * planted and swinging ahead through the last third.
	 */
	public static float staffFoot(float p, float swell) {
		float half = stanceTravel(swell) * 0.5F;
		float into = wrap(p - STAFF);
		if (into < THIRD) return HemoEasing.lerp(half, -half, HemoEasing.smoothstep(into / THIRD));
		return HemoEasing.lerp(-half, half, (into - THIRD) / (1.0F - THIRD));
	}

	/** A short jolt after something lands at phase {@code land}: the body taking the weight. */
	public static float landing(float p, float land) {
		return HemoEasing.pulse(wrap(p - land) * 3.0F, 0.12F, 0.9F);
	}

	/** The knees' fold for a crouch and a slump, radians. */
	public static float fold(float crouch, float slump) {
		return Math.max(0.0F, Math.min(FOLD_MAX, crouch * FOLD_PER_CROUCH + slump * FOLD_PER_SLUMP));
	}

	/** How far below the hip a leg's sole hangs, model units, from the thigh's absolute pitch and the knee's bend. */
	public static float reach(float thigh, float knee) {
		return THIGH * (float) Math.cos(thigh) + SHIN * (float) Math.cos(thigh + knee);
	}

	static float wrap(float value) {
		float f = value - (float) Math.floor(value);
		return f >= 1.0F ? 0.0F : f;
	}
}
