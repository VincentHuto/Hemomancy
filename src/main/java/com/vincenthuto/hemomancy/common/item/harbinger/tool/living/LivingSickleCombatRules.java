package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingSickleCombatRules {
	public static final float SWEEP_DAMAGE_MULTIPLIER = 0.40F;
	public static final float EXECUTION_HEALTH_RATIO = 0.30F;
	public static final float EXECUTION_BONUS_DAMAGE = 3.0F;

	private LivingSickleCombatRules() {
	}

	public static float executionBonus(float health, float maxHealth) {
		if (maxHealth <= 0.0F || health / maxHealth > EXECUTION_HEALTH_RATIO) return 0.0F;
		return EXECUTION_BONUS_DAMAGE;
	}

	public static float sweepDamage(float normalAttackDamage) {
		return Math.max(0.0F, normalAttackDamage) * SWEEP_DAMAGE_MULTIPLIER;
	}
}
