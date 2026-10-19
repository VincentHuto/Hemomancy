package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.summon.PuppetStringsRules;

/**
 * The Recall's string timeline, shared by the server cue and the client geometry. Vesper raises his Crossbar and casts
 * three vein strings onto each puppet he is taking back; they hook its shoulders and draw taut through the wind-up
 * (the player's last chance to kill it and strike the shape), haul it up at the commit, and reel what is left of it
 * into the Crossbar. Ages are ticks since the cue; no Minecraft types, so JVM tests cover it.
 */
public final class VesperRecallRules {
	/** Strings reach the puppet here. */
	public static final double CAST_END = 6.0D;
	/** The haul: the Recall's commit tick, when the puppet starts unspooling. */
	public static final int HAUL_TICK = 20;
	/** Long enough for the haul and the reel into the Crossbar. */
	public static final int CUE_TICKS = HAUL_TICK + PuppetStringsRules.UNSPOOL_TICKS + 6;
	public static final int STRINGS = 3;
	private static final double CAST_SLACK = 1.3D;
	private static final double HOOK_SLACK = 0.18D;
	/** The raised Crossbar's tip relative to the mount's feet: above the rider's head, to his left, a little ahead. */
	private static final double CROSSBAR_UP = 5.9D;
	private static final double CROSSBAR_LEFT = 0.25D;
	private static final double CROSSBAR_FORWARD = 0.3D;

	private VesperRecallRules() {
	}

	/** How far the strings have run out from the Crossbar toward the puppet, 0 to 1. */
	public static double reach(double age) {
		return PuppetStringsRules.smooth(age / CAST_END);
	}

	/** How far the strings sag: loose as they land, drawing taut through the wind-up, rigid from the haul. */
	public static double slack(double age) {
		if (age <= CAST_END) return CAST_SLACK;
		if (age >= HAUL_TICK) return 0.0D;
		double t = (age - CAST_END) / (HAUL_TICK - CAST_END);
		return HOOK_SLACK + (CAST_SLACK - HOOK_SLACK) * (1.0D - PuppetStringsRules.smooth(t));
	}

	/** Strain before the haul: the strings tremble harder the closer it comes. */
	public static double strain(double age) {
		if (age <= CAST_END || age >= HAUL_TICK + PuppetStringsRules.SNAP_END) return 0.0D;
		return PuppetStringsRules.smooth((age - CAST_END) / (HAUL_TICK - CAST_END));
	}

	/**
	 * How far the body's last clot has been reeled up the strings into the Crossbar. It leaves when the unspool's
	 * flash swallows the hauled body.
	 */
	public static double reelIn(double age) {
		double start = HAUL_TICK + PuppetStringsRules.HAUL_END - 1.0D;
		return PuppetStringsRules.smooth((age - start) / (CUE_TICKS - 2.0D - start));
	}

	/** The flash at the Crossbar as the reeled-in remains arrive. */
	public static double arrival(double age) {
		double t = (age - (CUE_TICKS - 6.0D)) / 6.0D;
		return t <= 0.0D || t >= 1.0D ? 0.0D : Math.sin(t * Math.PI);
	}

	/** The raised Crossbar's tip as a world offset {x, y, z} from the mount's feet for a body yaw in degrees. */
	public static double[] crossbarOffset(float yawDegrees) {
		double yaw = Math.toRadians(yawDegrees);
		double forwardX = -Math.sin(yaw);
		double forwardZ = Math.cos(yaw);
		double leftX = Math.cos(yaw);
		double leftZ = Math.sin(yaw);
		return new double[] { forwardX * CROSSBAR_FORWARD + leftX * CROSSBAR_LEFT, CROSSBAR_UP,
				forwardZ * CROSSBAR_FORWARD + leftZ * CROSSBAR_LEFT };
	}
}
