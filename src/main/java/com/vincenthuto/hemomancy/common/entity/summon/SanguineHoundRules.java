package com.vincenthuto.hemomancy.common.entity.summon;

public final class SanguineHoundRules {
	public static final float RUPTURE_HEALTH_FRACTION = 0.4F;
	public static final int CUR_LIFETIME_TICKS = 220;

	private SanguineHoundRules() {
	}

	public static boolean shouldRupture(boolean cur, boolean ruptured, float health, float maxHealth) {
		return !cur && !ruptured && maxHealth > 0.0F && health > 0.0F
				&& health <= maxHealth * RUPTURE_HEALTH_FRACTION;
	}

	public static int curCount(int randomValue) {
		return 3 + Math.floorMod(randomValue, 3);
	}

	public static int dissolutionBloodRefund(int investedBlood) {
		return 0;
	}
}
