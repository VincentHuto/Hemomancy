package com.vincenthuto.hemomancy.client.render.cinematic;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Keyframe helpers for scene tracks. */
public final class BossSceneTracks {
	private BossSceneTracks() {
	}

	/** Piecewise smooth curve through (time, value) pairs; flat before the first and after the last. */
	public static float keys(float t, float... pairs) {
		if (t <= pairs[0]) return pairs[1];
		for (int i = 2; i < pairs.length; i += 2) {
			if (t <= pairs[i]) {
				float x = HemoEasing.clamp01((t - pairs[i - 2]) / (pairs[i] - pairs[i - 2]));
				return pairs[i - 1] + (pairs[i + 1] - pairs[i - 1]) * HemoEasing.smoothstep(x);
			}
		}
		return pairs[pairs.length - 1];
	}

	/** 0 to 1 over {@code in} ticks from {@code start}, held, then back to 0 over the {@code out} ticks before {@code end}. */
	public static float window(float t, float start, float in, float end, float out) {
		return keys(t, start, 0, start + in, 1, end - out, 1, end, 0);
	}
}
