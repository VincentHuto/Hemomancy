package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.util.Mth;

import java.util.EnumSet;
import java.util.Set;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

/**
 * Pure motion maths for the Evening Star's body: the gait curves, the two-joint leg chain that plants his boots, and
 * which moves leave the ground. Kept free of model state so the numbers can be tested.
 */
public final class VesperEveningStarPose {
	// Leg chain from VesperTheEveningStarModel, in model units (y down, -z forward). The knee pivot sits at
	// KNEE below the hip, and the sole of the boot at SOLE below the knee.
	static final float KNEE_Y = 10.7083F;
	static final float KNEE_Z = -1.5F;
	static final float SOLE_Y = 12.6F;
	static final float SOLE_Z = 2.9F;
	/** Root-space height of the floor the unscaled model stands on. */
	static final float FLOOR_Y = 24.0F;
	/** Thigh swing (radians) that covers half a stride at full speed; set so planted boots do not slide. */
	static final float STEP_SWING = 0.3F;
	/** The most the hips may drop into uneven ground so the lower boot can reach it, in model units. */
	static final float TERRAIN_SINK = 5.0F;
	static final float MIN_BODY_SHIFT = -4.0F;
	static final float MAX_BODY_SHIFT = 10.0F;

	/** Moves whose legs belong to a leap, kick or vault: no planting, no walk cycle under them. */
	private static final Set<VesperWeaponAction> AIRBORNE = EnumSet.of(LEAPING_CLEAVE, SKY_LANCE, SPEAR_VAULT_KICK,
			STAFF_PILLAR_VAULT, SICKLE_POUNCE, SICKLE_POUNCE_FEINT, PREDATOR_POUNCE, BORROWED_VULTURE_DIVE);

	private VesperEveningStarPose() {
	}

	public static boolean airbornePose(VesperWeaponAction action) {
		return AIRBORNE.contains(action);
	}

	/**
	 * Moves posed as a run of held beats rather than one curve. Each beat is its own pose signature, so the model
	 * crossfades between them instead of cutting.
	 */
	public static int segment(VesperWeaponAction action, float tick) {
		return switch (action) {
			case SICKLE_HARVEST -> tick < 14 ? 0 : tick < 30 ? 1 : tick < 46 ? 2 : tick < 54 ? 3 : 4;
			default -> 0;
		};
	}

	/** Wraps an angle into [-pi, pi). */
	public static float wrap(float radians) {
		float wrapped = radians % Mth.TWO_PI;
		if (wrapped >= Mth.PI) wrapped -= Mth.TWO_PI;
		if (wrapped < -Mth.PI) wrapped += Mth.TWO_PI;
		return wrapped;
	}

	/** Thigh swing for a leg at {@code phase} (cycles); positive is forward. */
	public static float legSwing(float phase) {
		return Mth.sin(phase * Mth.TWO_PI);
	}

	/** Knee lift while a leg swings through (0 in stance, 1 at mid-swing). */
	public static float legLift(float phase) {
		float c = Mth.cos(phase * Mth.TWO_PI);
		return c > 0.0F ? c * c : 0.0F;
	}

	/** Height of the sole below the hip (model units, y down) for a thigh pitch, knee bend and leg roll. */
	public static float soleDrop(float thighX, float kneeX, float roll) {
		float cosT = Mth.cos(thighX);
		float sinT = Mth.sin(thighX);
		float kneeY = KNEE_Y * cosT - KNEE_Z * sinT;
		float shinY = SOLE_Y * Mth.cos(kneeX) - SOLE_Z * Mth.sin(kneeX);
		float shinZ = SOLE_Y * Mth.sin(kneeX) + SOLE_Z * Mth.cos(kneeX);
		float soleY = shinY * cosT - shinZ * sinT;
		return (kneeY + soleY) * Mth.cos(roll);
	}

	/** How far forward of the hip the sole lies (model units, -z forward). */
	public static float soleReach(float thighX, float kneeX) {
		float cosT = Mth.cos(thighX);
		float sinT = Mth.sin(thighX);
		float kneeZ = KNEE_Y * sinT + KNEE_Z * cosT;
		float shinY = SOLE_Y * Mth.cos(kneeX) - SOLE_Z * Mth.sin(kneeX);
		float shinZ = SOLE_Y * Mth.sin(kneeX) + SOLE_Z * Mth.cos(kneeX);
		return kneeZ + shinY * sinT + shinZ * cosT;
	}

	/**
	 * The fold (radians) that lifts a sole by {@code lift} model units: the thigh comes forward by the fold and the
	 * knee bends by twice it, which keeps the boot under the hip. Bisected, since the chain is not symmetric.
	 */
	public static float foldFor(float thighX, float kneeX, float roll, float lift) {
		if (lift <= 0.0F) return 0.0F;
		float start = soleDrop(thighX, kneeX, roll);
		float target = start - lift;
		float lo = 0.0F;
		float hi = 1.2F;
		if (soleDrop(thighX - hi, kneeX + 2.0F * hi, roll) > target) return hi;
		for (int i = 0; i < 12; i++) {
			float mid = (lo + hi) * 0.5F;
			if (soleDrop(thighX - mid, kneeX + 2.0F * mid, roll) > target) lo = mid;
			else hi = mid;
		}
		return (lo + hi) * 0.5F;
	}

	/**
	 * The vertical shift (model units, + is down) that sets the body on its boots. {@code soles} are the root-space
	 * sole heights as posed and {@code floors} the root-space floor under each. With level floors the lowest boot
	 * touches; on uneven floors the hips may also sink a little so the lower boot reaches, and the higher leg folds.
	 */
	public static float bodyShift(float[] soles, float[] floors) {
		float touch = Float.MAX_VALUE;
		float highFloor = Float.MAX_VALUE;
		float lowFloor = -Float.MAX_VALUE;
		for (int i = 0; i < soles.length; i++) {
			touch = Math.min(touch, floors[i] - soles[i]);
			highFloor = Math.min(highFloor, floors[i]);
			lowFloor = Math.max(lowFloor, floors[i]);
		}
		float sink = Math.min(TERRAIN_SINK, lowFloor - highFloor);
		return Mth.clamp(touch + sink, MIN_BODY_SHIFT, MAX_BODY_SHIFT);
	}
}
