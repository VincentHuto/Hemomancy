package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * Where a world rite's daemon flies in the culmination. It first gathers over
 * the planted staff as every rite's daemon does, then a sky rite fires it
 * straight up, accelerating until it strikes the sky at the impact tick, and a
 * ground rite lifts it high over the floor, holds it rearing, and slams it
 * down onto the Focus at the impact tick. Pure maths; the engine moves the
 * daemon entity along it.
 */
public final class WorldRiteFinalePath {
	/** How far above the staff the daemon strikes the sky. */
	public static final double SKY_HEIGHT = 72.0D;
	/** How high a ground rite lifts the daemon before the slam. */
	public static final double SLAM_APEX = 9.0D;
	private static final int GATHER = CardinalRiteFinaleTiming.GROWTH_TICKS;
	private static final int IMPACT = CardinalRiteFinaleTiming.TOTAL_TICKS;
	/** The climb, the rearing hold and the slam split the time after gathering. */
	private static final int CLIMB_END = GATHER + 22;
	private static final int HOLD_END = CLIMB_END + 6;

	private WorldRiteFinalePath() {
	}

	/** Straight up, ever faster, never past the world's ceiling. */
	public static Vec3 skyPosition(double ticks, Vec3 hover, double ceilingY) {
		if (ticks <= GATHER) return hover;
		double t = HemoEasing.clamp01((ticks - GATHER) / (double) (IMPACT - GATHER));
		double top = Math.min(hover.y + SKY_HEIGHT, ceilingY);
		return new Vec3(hover.x, hover.y + (top - hover.y) * Math.pow(t, 2.6D), hover.z);
	}

	/** Up to the apex, a short rearing lift, then an accelerating slam onto {@code ground}. */
	public static Vec3 groundPosition(double ticks, Vec3 hover, Vec3 ground) {
		if (ticks <= GATHER) return hover;
		Vec3 apex = hover.add(0.0D, SLAM_APEX, 0.0D);
		if (ticks <= CLIMB_END) {
			double t = (ticks - GATHER) / (double) (CLIMB_END - GATHER);
			return hover.lerp(apex, 1.0D - Math.pow(1.0D - t, 3.0D));
		}
		if (ticks <= HOLD_END) {
			double t = (ticks - CLIMB_END) / (double) (HOLD_END - CLIMB_END);
			return apex.add(0.0D, 0.35D * Math.sin(Math.PI * t), 0.0D);
		}
		double t = HemoEasing.clamp01((ticks - HOLD_END) / (double) (IMPACT - HOLD_END));
		return apex.lerp(ground, t * t * t);
	}
}
