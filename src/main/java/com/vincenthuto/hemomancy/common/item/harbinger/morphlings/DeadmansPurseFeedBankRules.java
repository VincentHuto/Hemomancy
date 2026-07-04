package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class DeadmansPurseFeedBankRules {
	public static final double BLOOD_PER_DAMAGE = 6.0D;
	public static final double BLOOD_PER_KILL_HEALTH = 4.0D;

	private DeadmansPurseFeedBankRules() {
	}

	public static double attackDeposit(int maturity, float damageAmount) {
		if (maturity < 2 || damageAmount <= 0.0F) {
			return 0.0D;
		}
		double maturityMultiplier = 1.0D + Math.max(0, maturity - 2) * 0.20D;
		return damageAmount * BLOOD_PER_DAMAGE * maturityMultiplier;
	}

	public static double killDeposit(int maturity, float victimMaxHealth) {
		if (maturity < 3 || victimMaxHealth <= 0.0F) {
			return 0.0D;
		}
		double maturityMultiplier = 1.0D + Math.max(0, maturity - 3) * 0.25D;
		return Math.min(160.0D, victimMaxHealth * BLOOD_PER_KILL_HEALTH * maturityMultiplier);
	}
}
