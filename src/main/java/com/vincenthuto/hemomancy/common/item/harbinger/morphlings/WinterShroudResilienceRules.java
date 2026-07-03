package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class WinterShroudResilienceRules {
	private WinterShroudResilienceRules() {
	}

	public static boolean shouldApplyHide(int maturity, boolean still, float health, float maxHealth) {
		if (maturity < 1) {
			return false;
		}
		float healthRatio = maxHealth <= 0.0F ? 1.0F : health / maxHealth;
		return still || healthRatio <= 0.45F;
	}

	public static int resistanceAmplifier(int maturity, boolean still, float health, float maxHealth) {
		if (!shouldApplyHide(maturity, still, health, maxHealth)) {
			return 0;
		}
		float healthRatio = maxHealth <= 0.0F ? 1.0F : health / maxHealth;
		int lowHealthBonus = healthRatio <= 0.30F ? 1 : 0;
		return Math.min(2, Math.max(0, maturity / 2 + lowHealthBonus));
	}

	public static boolean canTunMolt(int maturity, float health, float maxHealth) {
		if (maturity < 4 || maxHealth <= 0.0F) {
			return false;
		}
		return health / maxHealth <= 0.40F;
	}
}
