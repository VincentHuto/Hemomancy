package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.world.phys.Vec3;

/**
 * The spray: a short cone. Every pulse every harmable creature in the cone takes a small hit and the school's state at
 * long duration. Low damage, heavy state: the form for setting up a payoff on a crowd. A snap drenches: one pulse now
 * at twice the damage and half again the length.
 */
public final class SprayRules {
	/** The spray reaches six tenths of the verb's reach. */
	public static final double REACH_SCALE = 0.6D;
	/** The cone's half-angle is 35 degrees. */
	public static final double MIN_DOT = Math.cos(Math.toRadians(35.0D));
	/** With the off hand fanning too (Second Strand) the cone opens to 50 degrees. */
	public static final double TWIN_MIN_DOT = Math.cos(Math.toRadians(50.0D));
	public static final float PULSE_DAMAGE = 1.5F;
	public static final float DRENCH_MULTIPLIER = 2.0F;
	public static final double DRENCH_REACH = 1.5D;

	private SprayRules() {
	}

	/** Whether {@code point} lies in the cone from {@code origin} along {@code axis}, within {@code range}. */
	public static boolean inCone(Vec3 origin, Vec3 axis, Vec3 point, double range, double minDot) {
		Vec3 to = point.subtract(origin);
		double distance = to.length();
		if (distance > range) return false;
		if (distance < 1.0E-4D) return true;
		Vec3 unit = axis.lengthSqr() < 1.0E-8D ? new Vec3(0, 0, 1) : axis.normalize();
		return unit.dot(to.scale(1.0D / distance)) >= minDot;
	}
}
