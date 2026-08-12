package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.List;

/** Pure locked-action timing and palette contract for Vesper's Flammeus breath. */
public final class VesperFlammeusBreathRules {
	public static final int WINDUP_TICKS = 12;
	public static final int LAST_DAMAGE_TICK = 48;
	public static final int DURATION_TICKS = 60;
	private static final List<Integer> PALETTE = List.of(0xDC000C, 0x080003);

	private VesperFlammeusBreathRules() { }

	public static boolean isDamagePulse(int actionTick) {
		return actionTick >= WINDUP_TICKS && actionTick <= LAST_DAMAGE_TICK
				&& (actionTick - WINDUP_TICKS) % 4 == 0;
	}

	public static List<Integer> palette() {
		return PALETTE;
	}

	public static double bloodCostPerTick() {
		return 0.0D;
	}
}
