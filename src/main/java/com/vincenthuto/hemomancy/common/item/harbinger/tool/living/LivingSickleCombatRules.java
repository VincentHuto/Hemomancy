package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingSickleCombatRules {
	public static final float SWEEP_DAMAGE_MULTIPLIER = 0.40F;
	public static final float SPIN_DAMAGE_MULTIPLIER = 0.70F;
	public static final float EXECUTION_HEALTH_RATIO = 0.30F;
	public static final float EXECUTION_BONUS_DAMAGE = 3.0F;
	public static final double SPIN_RADIUS = 4.0D;
	public static final double HOOK_RANGE = 18.0D;
	public static final double HOOK_PULL_STRENGTH = 1.2D;
	public static final int SPIN_COOLDOWN_TICKS = 80;
	public static final int HOOK_COOLDOWN_TICKS = 20;

	private LivingSickleCombatRules() {
	}

	public static float executionBonus(float health, float maxHealth) {
		if (maxHealth <= 0.0F || health / maxHealth > EXECUTION_HEALTH_RATIO) return 0.0F;
		return EXECUTION_BONUS_DAMAGE;
	}

	public static float sweepDamage(float normalAttackDamage) {
		return Math.max(0.0F, normalAttackDamage) * SWEEP_DAMAGE_MULTIPLIER;
	}

	public static float spinDamage(float normalAttackDamage) {
		return Math.max(0.0F, normalAttackDamage) * SPIN_DAMAGE_MULTIPLIER;
	}

	public static float hookDamage(float normalAttackDamage) {
		return Math.max(0.0F, normalAttackDamage);
	}

	public static float attackSpeed(LivingSickleMode mode) {
		return mode == LivingSickleMode.BLOOD_HOOK ? -2.8F : -1.8F;
	}

	public static double pullStrength(double knockbackResistance) {
		return HOOK_PULL_STRENGTH * (1.0D - Math.max(0.0D, Math.min(1.0D, knockbackResistance)));
	}
}
