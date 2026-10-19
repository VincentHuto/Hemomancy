package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;

/**
 * The belly drag: two diagonal claw pairs alternate, each biting in for the first 60% of its half-cycle and hauling
 * the body forward while planted. Shared by the server's ground speed and the client's claw animation.
 */
public final class MycophantMotionRules {
	public static final float CYCLE_TICKS = 36.0F;
	private static final float PLANTED_FRACTION = 0.6F;
	private static final float PEAK_SPEED = 0.11F;

	private MycophantMotionRules() {
	}

	/** 0..1 through one full drag cycle. */
	public static float dragPhase(float ticks) {
		float wrapped = ticks % CYCLE_TICKS;
		if (wrapped < 0.0F) wrapped += CYCLE_TICKS;
		return wrapped / CYCLE_TICKS;
	}

	/** 0..1 through one pair's own step; pair 1 runs half a cycle behind pair 0. */
	public static float pairPhase(int pair, float ticks) {
		return dragPhase(ticks + (pair == 0 ? 0.0F : CYCLE_TICKS * 0.5F));
	}

	public static boolean pairPlanted(int pair, float ticks) {
		return pairPhase(pair, ticks) < PLANTED_FRACTION;
	}

	/** Forward speed in blocks per tick: surges while a pair hauls, never negative. */
	public static float haulSpeed(float ticks, int phase) {
		float best = 0.0F;
		for (int pair = 0; pair < 2; pair++) {
			float local = pairPhase(pair, ticks);
			if (local < PLANTED_FRACTION) best = Math.max(best, Mth.sin((float) Math.PI * local / PLANTED_FRACTION));
		}
		return best * PEAK_SPEED * (phase >= 2 ? 1.25F : 1.0F);
	}
}
