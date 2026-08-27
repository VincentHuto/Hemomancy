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

	public static int coldBloodedTier(int maturity) {
		return Math.max(0, Math.min(3, maturity - 1));
	}

	public static int retainedEnvironmentalFreezeTicks(int maturity, int frozenTicks, int tickCount) {
		return switch (coldBloodedTier(maturity)) {
			case 1 -> Math.max(0, frozenTicks - (tickCount % 2 == 0 ? 1 : 0));
			case 2, 3 -> 0;
			default -> frozenTicks;
		};
	}

	public static boolean canTraversePowderSnow(int maturity) {
		return coldBloodedTier(maturity) >= 2;
	}

	public static float coldDamageMultiplier(int maturity, boolean ordinaryEnvironmental, boolean freezingDamage) {
		if (!freezingDamage) return 1.0F;
		return switch (coldBloodedTier(maturity)) {
			case 1 -> ordinaryEnvironmental ? 0.75F : 1.0F;
			case 2 -> ordinaryEnvironmental ? 0.50F : 1.0F;
			case 3 -> ordinaryEnvironmental ? 0.0F : 0.25F;
			default -> 1.0F;
		};
	}
}
