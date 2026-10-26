package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * The yielding at zero health: wings and collar fold, she lowers herself, offers the Tendril on the siphon and sinks
 * into the nectar. No explosion, no death flip.
 */
public final class MycophantYieldRules {
	private MycophantYieldRules() {
	}

	public static int length() {
		return 120;
	}

	public static float fold(float t) {
		return MycophantSplitRules.window(t, 0, 20);
	}

	public static float lower(float t) {
		return MycophantSplitRules.window(t, 20, 45);
	}

	public static float offer(float t) {
		return MycophantSplitRules.window(t, 40, 75);
	}

	public static float sink(float t) {
		return MycophantSplitRules.window(t, 70, 120);
	}
}
