package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * The split at half health, beat by beat: she hunches and strains while the skull cracks, the halves snap apart at
 * {@link #snapTick()} and swing out to hang off the sides, the pulp face swells through the gap and blooms at the push,
 * the hyphae wings bud from the shoulders and unfurl, and she settles back into her stance for phase 2.
 * Curves take fractional ticks so the client can animate between server ticks.
 */
public final class MycophantSplitRules {
	private MycophantSplitRules() {
	}

	public static int length() {
		return 80;
	}

	/** The bone gives: the halves snap apart here (the crack's sound and blood land on this tick). */
	public static int snapTick() {
		return 12;
	}

	public static int pushTick() {
		return 40;
	}

	/** The strain before the snap: a hairline seam that widens and trembles. */
	public static float crack(float t) {
		return window(t, 0, snapTick());
	}

	/** How far the halves have come open, 0..1, without the swing's overshoot. */
	public static float halves(float t) {
		return window(t, snapTick(), 30);
	}

	/**
	 * The halves' swing: flung open past their rest at the snap, then a damped bounce back to hang at 1. Starts at 0
	 * and never closes back past the seam.
	 */
	public static float hinge(float t) {
		float s = t - snapTick();
		if (s <= 0.0F) return 0.0F;
		return Math.max(0.0F, 1.0F - (float) (Math.exp(-0.3F * s) * Math.cos(0.45F * s)));
	}

	/** The halves' weight: they drop and roll down onto the sides a little behind the swing. */
	public static float droop(float t) {
		return window(t, snapTick() + 3, 34);
	}

	/** The pulp swelling through the gap, from the snap to just after the push. */
	public static float pulp(float t) {
		return window(t, snapTick(), 42);
	}

	/** The wings budding at the shoulders and unfurling to full span. */
	public static float wings(float t) {
		return window(t, 34, 70);
	}

	/** The last beat: she lowers out of the display into her phase-2 stance. */
	public static float settle(float t) {
		return window(t, 66, length());
	}

	/** Smoothstep from 0 at {@code start} to 1 at {@code end}, clamped. Shared by the split and the yield. */
	static float window(float t, float start, float end) {
		float x = Math.max(0.0F, Math.min(1.0F, (t - start) / (end - start)));
		return x * x * (3.0F - 2.0F * x);
	}
}
