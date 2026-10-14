package com.vincenthuto.hemomancy.common.rite;

/**
 * How hard each woken ring lands. Every factor rises with the ring's depth: 0 for the first ring that wakes after
 * the Focus's own, 1 for the rite's last. The server plays the sting and bursts from it; the client the tear, the
 * shock and the surge. All ring-opening tuning lives here.
 */
public final class RingOpeningMath {
	/** A sleeping anchor's sore, as a share of its full size: a small closed scar. */
	public static final float SCAR_SCALE = 0.32F;

	private RingOpeningMath() {
	}

	public static float depth(int rank, int rings) {
		if (rings <= 2) return 1.0F;
		return clamp((rank - 1) / (float) (rings - 2));
	}

	public static int burstCells(float depth) {
		return Math.round(lerp(depth, 12.0F, 40.0F));
	}

	public static float tearTicks(float depth) {
		return lerp(depth, 30.0F, 50.0F);
	}

	/** How far a waking sore has torn open, from its scar (0) to full (1); fully open with no beat remembered. */
	public static float tear(float age, float depth) {
		if (age < 0.0F) return 1.0F;
		float t = clamp(age / tearTicks(depth));
		return t * t * (3.0F - 2.0F * t);
	}

	public static float shockScale(float depth) {
		return lerp(depth, 0.7F, 1.4F);
	}

	public static float shockReach(float depth) {
		return lerp(depth, 0.8F, 1.5F);
	}

	public static float stingPitch(float depth) {
		return lerp(depth, 0.9F, 0.55F);
	}

	public static float stingVolume(float depth) {
		return lerp(depth, 1.0F, 1.6F);
	}

	public static float fogSurge(float depth) {
		return lerp(depth, 0.35F, 1.0F);
	}

	private static float lerp(float t, float from, float to) {
		return from + (to - from) * clamp(t);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
