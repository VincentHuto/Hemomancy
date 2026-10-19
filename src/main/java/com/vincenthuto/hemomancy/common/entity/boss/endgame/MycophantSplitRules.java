package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** The split at half health: the skull cracks and swings apart, the pulp face blooms, the wings grow. */
public final class MycophantSplitRules {
	private MycophantSplitRules() {
	}

	public static int length() {
		return 80;
	}

	public static int pushTick() {
		return 40;
	}

	public static float crack(int t) {
		return window(t, 0, 10);
	}

	public static float halves(int t) {
		return window(t, 10, 35);
	}

	public static float pulp(int t) {
		return window(t, 25, 50);
	}

	public static float wings(int t) {
		return window(t, 35, 70);
	}

	/** Smoothstep from 0 at {@code start} to 1 at {@code end}, clamped. Shared by the split and the yield. */
	static float window(float t, float start, float end) {
		float x = Math.max(0.0F, Math.min(1.0F, (t - start) / (end - start)));
		return x * x * (3.0F - 2.0F * x);
	}
}
