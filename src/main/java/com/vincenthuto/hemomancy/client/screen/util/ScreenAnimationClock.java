package com.vincenthuto.hemomancy.client.screen.util;

import java.util.Map;
import java.util.WeakHashMap;

public final class ScreenAnimationClock {

	private static final float FRAME_STEP = 0.016f;
	private static final Object GLOBAL_OWNER = ScreenAnimationClock.class;
	private static final Map<Object, Float> TIMES = new WeakHashMap<>();

	private ScreenAnimationClock() {
	}

	public static float tick(Object owner) {
		float next = TIMES.getOrDefault(owner, 0f) + FRAME_STEP;
		TIMES.put(owner, next);
		return next;
	}

	public static float tick() {
		return tick(GLOBAL_OWNER);
	}
}
