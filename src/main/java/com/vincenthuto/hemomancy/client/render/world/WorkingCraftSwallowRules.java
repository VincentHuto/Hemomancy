package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftDaemonPath;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Timing for a Working floor swallowing its craft: the fill line climbs the
 * structure while it is fed, pulses once when it seals, darkens once the daemon
 * has dived inside, yanks the structure under the floor with a thud on
 * completion, and drains back down when the rite lets go. Also the writhing
 * tentacles that climb its corners. Pure maths so it can be tested without a client.
 */
public final class WorkingCraftSwallowRules {
	/** The structure shudders in the floor's grip before it is pulled. */
	public static final int ANTICIPATION_TICKS = 4;
	/** The pull itself: accelerating, so it ends hard. */
	public static final int DRAG_TICKS = 6;
	/** The tick the structure hits bottom: the thud. */
	public static final int THUD_TICK = ANTICIPATION_TICKS + DRAG_TICKS;
	public static final int SINK_TICKS = THUD_TICK;
	public static final int DRAIN_TICKS = 24;
	public static final int SEAL_PULSE_TICKS = 12;
	/** How far past its own height a completed structure sinks, so its top passes under the membrane. */
	public static final double SINK_OVERSHOOT = 0.2D;
	/** The share of the gap the displayed fill closes each frame. */
	private static final float FILL_EASE = 0.18F;

	private WorkingCraftSwallowRules() {
	}

	/** World height of the fill line over a subject whose foot is {@code bottomY}. */
	public static double fillLine(double bottomY, double height, float fill) {
		return bottomY + height * HemoEasing.clamp01(fill);
	}

	/**
	 * Where a Blood Projection into a fed structure lands: on the fill line. A beam aimed at a side face keeps
	 * the aimed spot; one aimed at the top lands on the structure's side facing the caster, where it can be seen.
	 * The structure spans blocks {@code minX..maxX}, {@code minZ..maxZ} and rises {@code height} from {@code bottomY}.
	 */
	public static Vec3 projectionLanding(Vec3 source, Vec3 hit, boolean sideFace, int minX, int minZ, int maxX,
			int maxZ, int bottomY, int height, float fill) {
		double y = Math.max(bottomY + 0.05D, Math.min(bottomY + height, fillLine(bottomY, height, fill)));
		if (sideFace) return new Vec3(hit.x, y, hit.z);
		return new Vec3(Math.max(minX, Math.min(maxX + 1, source.x)), y, Math.max(minZ, Math.min(maxZ + 1, source.z)));
	}

	/** Eases the displayed fill toward the last synced fill so it climbs smoothly between syncs. */
	public static float smoothFill(float displayed, float target) {
		float next = displayed + (target - displayed) * FILL_EASE;
		return Math.abs(target - next) < 0.002F ? target : next;
	}

	/**
	 * How far a completed structure has sunk {@code age} ticks after the floor took it: a few
	 * twitches as the floor grips it, then a cubic, accelerating yank to full depth at the thud.
	 */
	public static double sinkOffset(float age, double height) {
		if (age < ANTICIPATION_TICKS) return 0.04D * Math.abs(Math.sin(age * 2.6D));
		double t = HemoEasing.clamp01((age - ANTICIPATION_TICKS) / DRAG_TICKS);
		return t * t * t * (height + SINK_OVERSHOOT);
	}

	/** How far the daemon's possession has darkened the structure, {@code culminationTicks} into the culmination. */
	public static float possession(float culminationTicks) {
		float start = WorkingCraftDaemonPath.DIVE_TICKS - 4.0F;
		float end = WorkingCraftDaemonPath.DIVE_TICKS + 16.0F;
		return HemoEasing.smoothstep((culminationTicks - start) / (end - start));
	}

	/** The membrane's colour darkening toward clotted blood as the daemon possesses the structure. */
	public static int possessionTint(int rgb, float possession) {
		float p = HemoEasing.clamp01(possession);
		int dark = 0x3A0208;
		int r = Math.round((rgb >> 16 & 0xFF) + ((dark >> 16 & 0xFF) - (rgb >> 16 & 0xFF)) * p);
		int g = Math.round((rgb >> 8 & 0xFF) + ((dark >> 8 & 0xFF) - (rgb >> 8 & 0xFF)) * p);
		int b = Math.round((rgb & 0xFF) + ((dark & 0xFF) - (rgb & 0xFF)) * p);
		return r << 16 | g << 8 | b;
	}

	/**
	 * The spine of one tentacle climbing a structure corner at ({@code cornerX}, {@code cornerZ}), with
	 * {@code outX}/{@code outZ} (each ±1) pointing away from the structure. It flares out of the floor, writhes in
	 * a wave travelling up its length, and its tip hooks inward over the edge at the fill line ({@code top}).
	 * {@code grip} (the daemon's possession) pulls it tight against the corner and quickens it.
	 */
	public static List<Vec3> tentacleSpine(double cornerX, double cornerZ, double outX, double outZ, double top,
			double time, int seed, float grip) {
		int points = 10;
		double tight = 1.0D - 0.5D * HemoEasing.clamp01(grip);
		double speed = 0.22D + 0.18D * HemoEasing.clamp01(grip);
		List<Vec3> spine = new ArrayList<>(points);
		for (int i = 0; i < points; i++) {
			double t = i / (points - 1.0D);
			double flare = 0.34D * (1.0D - t) * (1.0D - t) * tight;
			double wave = Math.sin(t * 7.0D - time * speed + seed) * (0.10D + 0.08D * t) * tight;
			double breathe = Math.sin(t * 4.0D - time * speed * 0.7D + seed * 0.7D) * 0.05D * t;
			double hook = HemoEasing.smoothstep((float) ((t - 0.75D) / 0.25D));
			double away = flare + 0.04D + breathe - 0.45D * hook;
			spine.add(new Vec3(cornerX + outX * away + outZ * wave * 0.7D,
					0.02D + top * t + 0.25D * hook,
					cornerZ + outZ * away - outX * wave * 0.7D));
		}
		return spine;
	}

	/** The fill of a craft that ended without completing, draining back into the floor. */
	public static float drainFill(float startFill, float age) {
		return HemoEasing.clamp01(startFill) * (1.0F - HemoEasing.smoothstep(age / DRAIN_TICKS));
	}

	/** One hard pulse up the structure when it seals; {@code age} below zero means no seal is remembered. */
	public static float sealPulse(float age) {
		if (age < 0.0F) return 0.0F;
		return 1.0F - HemoEasing.smoothstep(age / SEAL_PULSE_TICKS);
	}
}
