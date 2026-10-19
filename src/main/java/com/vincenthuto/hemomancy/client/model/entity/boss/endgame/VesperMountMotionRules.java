package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
import net.minecraft.util.Mth;

/** Pure gait, settle and wing curves for the mount's procedural animation. */
public final class VesperMountMotionRules {
	private VesperMountMotionRules() {
	}

	/** Leg index: pair 0 is the front pair, 2 the rear; each pair has a right and a left leg. */
	public static int leg(int pair, boolean left) {
		return pair * 2 + (left ? 1 : 0);
	}

	/**
	 * Alternating tripod: left-front, right-middle and left-rear step together, the other three half a cycle later.
	 * Returns the leg's phase in [0, 1); the first half is the swing (foot in the air), the second the stance.
	 */
	public static float tripodPhase(int leg, float cycle) {
		int group = (leg / 2 + leg % 2) % 2;
		return frac(cycle + group * 0.5F);
	}

	public static float footLift(float phase) {
		if (phase <= 0.0F || phase >= 0.5F) return 0.0F;
		return Mth.sin(phase * 2.0F * Mth.PI);
	}

	/** Back-ease-out: a joint swings past its mark and settles, so plants read as weight instead of hinges. */
	public static float overshoot(float t) {
		float x = Mth.clamp(t, 0.0F, 1.0F) - 1.0F;
		float c1 = 1.70158F;
		return 1.0F + (c1 + 1.0F) * x * x * x + c1 * x * x;
	}

	/** 0 = folded flat along the back, 1 = fully spread. */
	public static float wingSpread(VesperWingedFlightRules.FlightMode mode, float flightTicks) {
		return switch (mode) {
			case GROUNDED -> 0.0F;
			case WING_GROWTH -> smooth(flightTicks / VesperWingedFlightRules.WING_GROWTH_TICKS);
			default -> 1.0F;
		};
	}

	/**
	 * Where a leg's foot sits along its step, -1 fully back to +1 fully forward. The swing (first half) carries it
	 * forward on an eased arc; the stance drags it back at a constant rate so a planted foot never skates.
	 */
	public static float stepSweep(float phase) {
		float p = frac(phase);
		if (p < 0.5F) return -1.0F + 2.0F * smooth(p * 2.0F);
		return 1.0F - 4.0F * (p - 0.5F);
	}

	/** Frame-rate independent exponential follow: the fraction of the gap to close over {@code dt} ticks. */
	public static float follow(float dt, float tau) {
		if (tau <= 0.0F || dt <= 0.0F) return dt <= 0.0F ? 0.0F : 1.0F;
		return 1.0F - (float) Math.exp(-dt / tau);
	}

	// Leg geometry from vesper_mount.py, in model units (x out from the body, y down). The femur pivot sits 8 out
	// from the coxa; from it the rest foot lies at FOOT_FROM_FEMUR and the knee-to-foot run is KNEE_TO_FOOT.
	private static final float FOOT_FROM_FEMUR_X = 34.2F;
	private static final float FOOT_FROM_FEMUR_Y = 30.4F;
	private static final float KNEE_TO_FOOT_X = 18.3F;
	private static final float KNEE_TO_FOOT_Y = 52.2F;
	private static final float LEG_REACH = 42.2F;
	private static final float[] COXA_Z = { -32.0F, -21.0F, -11.0F };
	private static final float[] COXA_SWEEP_DEGREES = { 34.0F, 4.0F, -30.0F };
	private static final float COXA_X = 12.5F;

	/**
	 * Linearised two-joint IK for one leg: the femur and tibia deltas (right-leg sign, radians) that move the foot
	 * {@code down} and {@code out} model units, plus the tarsus delta that keeps the hooked foot upright.
	 */
	public static float[] footOffset(float down, float out) {
		// Columns are d(foot)/d(angle) for the femur and the tibia: rotating by a about z moves (x, y) by (-y, x) a.
		float ax = -FOOT_FROM_FEMUR_Y, ay = FOOT_FROM_FEMUR_X;
		float bx = -KNEE_TO_FOOT_Y, by = KNEE_TO_FOOT_X;
		float det = ax * by - bx * ay;
		float femur = (out * by - bx * down) / det;
		float tibia = (ax * down - out * ay) / det;
		return new float[] { femur, tibia, -(femur + tibia) };
	}

	/** Rest foot position of a leg in model units relative to the model origin: {x, z}; x is negative on the left. */
	public static float[] restFoot(int pair, boolean left) {
		float sweep = COXA_SWEEP_DEGREES[pair] * Mth.DEG_TO_RAD;
		float x = COXA_X + LEG_REACH * Mth.cos(sweep);
		float z = COXA_Z[pair] - LEG_REACH * Mth.sin(sweep);
		return new float[] { left ? -x : x, z };
	}

	/** Least-squares ground plane h = a + b x + c z through sampled points; returns {a, b, c}. */
	public static float[] fitPlane(float[] xs, float[] zs, float[] hs) {
		int n = xs.length;
		double sx = 0, sz = 0, sh = 0, sxx = 0, szz = 0, sxz = 0, sxh = 0, szh = 0;
		for (int i = 0; i < n; i++) {
			sx += xs[i];
			sz += zs[i];
			sh += hs[i];
			sxx += xs[i] * xs[i];
			szz += zs[i] * zs[i];
			sxz += xs[i] * zs[i];
			sxh += xs[i] * hs[i];
			szh += zs[i] * hs[i];
		}
		double det = n * (sxx * szz - sxz * sxz) - sx * (sx * szz - sxz * sz) + sz * (sx * sxz - sxx * sz);
		if (Math.abs(det) < 1.0E-9) return new float[] { (float) (sh / Math.max(1, n)), 0.0F, 0.0F };
		double a = (sh * (sxx * szz - sxz * sxz) - sx * (sxh * szz - sxz * szh) + sz * (sxh * sxz - sxx * szh)) / det;
		double b = (n * (sxh * szz - sxz * szh) - sh * (sx * szz - sxz * sz) + sz * (sx * szh - sxh * sz)) / det;
		double c = (n * (sxx * szh - sxh * sxz) - sx * (sx * szh - sxh * sz) + sh * (sx * sxz - sxx * sz)) / det;
		return new float[] { (float) a, (float) b, (float) c };
	}

	public static float smooth(float t) {
		float x = Mth.clamp(t, 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}

	private static float frac(float value) {
		return value - Mth.floor(value);
	}
}
