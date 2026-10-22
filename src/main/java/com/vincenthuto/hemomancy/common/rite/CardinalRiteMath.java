package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;

/**
 * Small shared maths for Cardinal Rite visuals and rules. Free of Minecraft
 * client types so both sides and JVM tests can use it.
 */
public final class CardinalRiteMath {
	public static final double FULL_CIRCLE = Math.PI * 2.0D;

	private CardinalRiteMath() {
	}

	/** An angle wrapped into [0, 2π). */
	public static double normalizeAngle(double angle) {
		double normalized = angle % FULL_CIRCLE;
		return normalized < 0.0D ? normalized + FULL_CIRCLE : normalized;
	}

	/** The shorter unsigned distance between two angles, in [0, π]. */
	public static double angularDistance(double first, double second) {
		double difference = normalizeAngle(first - second);
		return Math.min(difference, FULL_CIRCLE - difference);
	}

	/**
	 * The rite's shared heartbeat: the floor's 36-tick lub-dub in [0,1]. Rings,
	 * sockets and the floor membrane all read this so they beat together.
	 */
	public static float heartbeat(float time) {
		return CardinalRiteFloorVisualRules.heartbeat(time);
	}
}
