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

	public static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public static double clamp01(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}

	/** Hermite smoothstep of a value clamped to [0,1]. */
	public static float smoothstep(float value) {
		float clamped = clamp01(value);
		return clamped * clamped * (3.0F - 2.0F * clamped);
	}

	/** Smoothstep of {@code value} between two edges. */
	public static float smoothstep(float edge0, float edge1, float value) {
		if (edge1 == edge0) return value < edge0 ? 0.0F : 1.0F;
		return smoothstep((value - edge0) / (edge1 - edge0));
	}

	public static float lerp(float amount, float start, float end) {
		return start + (end - start) * amount;
	}

	public static float easeOutCubic(float value) {
		float inverse = 1.0F - clamp01(value);
		return 1.0F - inverse * inverse * inverse;
	}

	public static float easeInCubic(float value) {
		float clamped = clamp01(value);
		return clamped * clamped * clamped;
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

	/** A one-shot envelope: rises over {@code attack} ticks, then decays to zero by {@code length}. */
	public static float pulse(float age, float attack, float length) {
		if (age < 0.0F || age >= length) return 0.0F;
		if (age < attack) return smoothstep(age / Math.max(0.0001F, attack));
		float decay = 1.0F - (age - attack) / Math.max(0.0001F, length - attack);
		return decay * decay;
	}
}
