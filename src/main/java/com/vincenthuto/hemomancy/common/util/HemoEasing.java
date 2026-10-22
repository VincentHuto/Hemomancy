package com.vincenthuto.hemomancy.common.util;

/**
 * The shared easing and interpolation maths, seeded from {@code CardinalRiteMath}. Free of Minecraft client types so
 * both sides and JVM tests can use it. Every function has a float and a double form that compute in their own
 * precision; the two are not interchangeable bit for bit.
 *
 * <p>{@link #smoothstep(float)} is the cubic Hermite {@code t*t*(3-2t)} of a clamped input. Never swap in
 * {@code Mth.smoothstep}: in 1.21.1 it is the unclamped quintic {@code t*t*t*(t*(6t-15)+10)}, which is
 * {@link #smootherstep(float)} here.
 *
 * <p>{@link #lerp(float, float, float)} takes {@code (start, end, amount)}. {@code Mth.lerp} takes the amount first.
 *
 * <p>The {@code Unclamped} variants skip the input clamp. They match the clamped forms inside [0,1] and overshoot
 * outside it; keep them where a caller relies on that or already guarantees the range.
 */
public final class HemoEasing {
	private HemoEasing() {
	}

	public static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public static double clamp01(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}

	/** Cubic Hermite smoothstep of a value clamped to [0,1]. */
	public static float smoothstep(float value) {
		float clamped = clamp01(value);
		return clamped * clamped * (3.0F - 2.0F * clamped);
	}

	public static double smoothstep(double value) {
		double clamped = clamp01(value);
		return clamped * clamped * (3.0D - 2.0D * clamped);
	}

	/** Cubic smoothstep of {@code value} between two edges. Equal edges give a hard step. */
	public static float smoothstep(float edge0, float edge1, float value) {
		if (edge1 == edge0) return value < edge0 ? 0.0F : 1.0F;
		return smoothstep((value - edge0) / (edge1 - edge0));
	}

	public static double smoothstep(double edge0, double edge1, double value) {
		if (edge1 == edge0) return value < edge0 ? 0.0D : 1.0D;
		return smoothstep((value - edge0) / (edge1 - edge0));
	}

	/** The cubic {@code t*t*(3-2t)} without the input clamp. */
	public static float smoothstepUnclamped(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	public static double smoothstepUnclamped(double value) {
		return value * value * (3.0D - 2.0D * value);
	}

	/** The quintic {@code t*t*t*(t*(6t-15)+10)} of a value clamped to [0,1]: flatter ends than smoothstep. */
	public static float smootherstep(float value) {
		float clamped = clamp01(value);
		return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
	}

	/** {@code start + (end - start) * amount}, unclamped. */
	public static float lerp(float start, float end, float amount) {
		return start + (end - start) * amount;
	}

	public static double lerp(double start, double end, double amount) {
		return start + (end - start) * amount;
	}

	/**
	 * {@code from * (1 - weight) + to * weight}: a weighted blend of two values. It rounds differently from
	 * {@link #lerp(float, float, float)}, so the two are not substitutes for each other.
	 */
	public static float crossfade(float from, float to, float weight) {
		return from * (1.0F - weight) + to * weight;
	}

	public static double crossfade(double from, double to, double weight) {
		return from * (1.0D - weight) + to * weight;
	}

	public static float easeOutCubic(float value) {
		float inverse = 1.0F - clamp01(value);
		return 1.0F - inverse * inverse * inverse;
	}

	public static double easeOutCubic(double value) {
		double inverse = 1.0D - clamp01(value);
		return 1.0D - inverse * inverse * inverse;
	}

	public static float easeInCubic(float value) {
		float clamped = clamp01(value);
		return clamped * clamped * clamped;
	}

	public static double easeInCubic(double value) {
		double clamped = clamp01(value);
		return clamped * clamped * clamped;
	}

	public static double easeInOutCubic(double value) {
		double clamped = clamp01(value);
		return clamped < 0.5D ? 4.0D * clamped * clamped * clamped : 1.0D - Math.pow(-2.0D * clamped + 2.0D, 3.0D) / 2.0D;
	}

	/** A one-shot envelope: rises over {@code attack} ticks, then decays to zero by {@code length}. */
	public static float pulse(float age, float attack, float length) {
		if (age < 0.0F || age >= length) return 0.0F;
		if (age < attack) return smoothstep(age / Math.max(0.0001F, attack));
		float decay = 1.0F - (age - attack) / Math.max(0.0001F, length - attack);
		return decay * decay;
	}
}
