package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * A woken devotee's walk as pure numbers: short, stiff steps keyed to the distance walked, not to time, so a planted
 * foot never skates. Each cycle the left foot swings in its first {@link #SWING} and the right in the half after; a
 * planted foot sweeps back under the body at exactly the speed the body moves, and the hips sink by as much as the
 * planted leg's angle lifts them, so the sole stays on the floor. The knees bend only as far as the swinging foot needs to
 * clear the floor: joints that have not moved for centuries. Model units (16 to a block, the jointed rig's twelve-unit leg). JVM-tested.
 */
public final class VelorumDevoteeGait {
	/** Model units in a block. */
	public static final float BLOCK = 16.0F;
	/** The thigh and the shin to the sole, in model units. */
	public static final float THIGH = 6.0F, SHIN = 6.0F;
	/** Blocks walked per two-step cycle: short steps. */
	public static final float CYCLE = 1.0F;
	/** The share of a cycle each foot spends off the floor. */
	public static final float SWING = 0.4F;
	/** The left and right swing windows' starts. */
	public static final float LEFT = 0.0F, RIGHT = 0.5F;
	/** How far a swinging knee bends at most, radians: only as far as the foot needs to clear the floor. */
	public static final float KNEE_LIFT = 0.7F;
	/** The walk animation's position grows by this much a block (below a quarter block a tick). */
	public static final float SWING_PER_BLOCK = 4.0F;

	private VelorumDevoteeGait() {
	}

	/** The cycle's phase in [0, 1) from the walk animation's position. */
	public static float phase(float limbSwing) {
		return wrap(limbSwing / (SWING_PER_BLOCK * CYCLE));
	}

	/** How far a planted foot travels under the body, front to back, model units. */
	public static float stanceTravel() {
		return CYCLE * BLOCK * (1.0F - SWING);
	}

	/** The thigh's swing either side of plumb, radians, so the planted foot covers the stance travel. */
	public static float amplitude() {
		return (float) Math.asin(Math.min(0.9F, stanceTravel() * 0.5F / (THIGH + SHIN)));
	}

	/**
	 * A thigh's angle at phase {@code p} (positive: foot behind, as the model's x rotation swings it). It swings forward
	 * through the window starting at {@code start}, eased, then sweeps back planted, its foot keeping pace with the floor.
	 */
	public static float thigh(float p, float start) {
		float sine = (float) Math.sin(amplitude());
		float into = wrap(p - start);
		float along;
		if (into < SWING) along = HemoEasing.lerp(1.0F, -1.0F, HemoEasing.smoothstep(into / SWING));
		else along = HemoEasing.lerp(-1.0F, 1.0F, (into - SWING) / (1.0F - SWING));
		return (float) Math.asin(along * sine);
	}

	/** 0 to 1 to 0 across a foot's swing window, else 0. */
	public static float lift(float p, float start) {
		float into = wrap(p - start);
		return into < SWING ? (float) Math.sin(Math.PI * into / SWING) : 0.0F;
	}

	/** Where a planted foot is along the walk, model units ahead of the hip (negative behind), from its thigh angle. */
	public static float footAhead(float thigh) {
		return -(THIGH + SHIN) * (float) Math.sin(thigh);
	}

	/** How far below the hip a leg's sole hangs, model units, from the thigh's pitch and the knee's bend. */
	public static float reach(float thigh, float knee) {
		return THIGH * (float) Math.cos(thigh) + SHIN * (float) Math.cos(thigh + knee);
	}

	/** How far a swinging foot clears the floor at the top of its swing, model units. */
	public static final float CLEARANCE = 2.0F;

	/**
	 * How far the hips sink so the lower planted sole meets the floor: the longer leg's shortfall from straight down,
	 * where a foot lifted in its swing ({@code lift} 0 to 1) counts as shorter by its clearance, so it never drags.
	 */
	public static float sink(float leftThigh, float leftKnee, float leftLift, float rightThigh, float rightKnee,
			float rightLift) {
		float reach = Math.max(reach(leftThigh, leftKnee) - leftLift * CLEARANCE,
				reach(rightThigh, rightKnee) - rightLift * CLEARANCE);
		return Math.max(0.0F, THIGH + SHIN - reach);
	}

	static float wrap(float value) {
		float f = value - (float) Math.floor(value);
		return f >= 1.0F ? 0.0F : f;
	}
}
