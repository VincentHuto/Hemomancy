package com.vincenthuto.hemomancy.common.entity.utility;

import net.minecraft.util.Mth;

/**
 * How the daemon turns toward where it faces: eased a share of the remaining angle each tick, the short way round,
 * so it never snaps. The head turns quicker than the body, so it leads and the body follows.
 */
public final class DaemonTurn {
	/** Share of the remaining turn the head makes each tick. */
	public static final float HEAD_RATE = 0.22F;
	/** Share of the remaining turn the body makes each tick. */
	public static final float BODY_RATE = 0.09F;

	private DaemonTurn() {
	}

	/** One tick of turning from {@code current} toward {@code target} (degrees), the short way round. */
	public static float toward(float current, float target, float rate) {
		return current + Mth.wrapDegrees(target - current) * rate;
	}
}
