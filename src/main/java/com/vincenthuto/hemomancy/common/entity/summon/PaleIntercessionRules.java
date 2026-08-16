package com.vincenthuto.hemomancy.common.entity.summon;

public final class PaleIntercessionRules {
	public static final int DURATION_TICKS = 1_200;
	public static final int COOLDOWN_TICKS = 600;
	public static final int THREAT_MEMORY_TICKS = 100;
	public static final int DISSOLUTION_TICKS = 20;
	public static final int DISTORTION_TICKS = 8;
	public static final double THREAT_RANGE = 16.0;
	public static final double FOLLOW_MIN_DISTANCE = 2.0;
	public static final double FOLLOW_MAX_DISTANCE = 4.0;
	public static final double TELEPORT_DISTANCE = 16.0;
	public static final float STRIKE_DAMAGE = 6.0f;
	public static final int SLOWNESS_TICKS = 40;

	private PaleIntercessionRules() {
	}

	public static int remainingAfterDamage(int remainingTicks, float damage) {
		if (damage <= 0.0f) {
			return Math.max(0, remainingTicks);
		}
		int cost = Math.max(20, (int) Math.ceil(damage * 20.0f));
		return Math.max(0, remainingTicks - cost);
	}
}
