package com.vincenthuto.hemomancy.common.entity.boss.endgame;

public final class EndgameBossCombatRules {
	public static final int MIN_ATTACK_CADENCE = 20;

	private EndgameBossCombatRules() {
	}

	public static boolean isArmored(float health, float maxHealth) {
		return maxHealth > 0.0F && health <= maxHealth * 0.5F;
	}

	public static boolean isVulnerable(float health, float maxHealth) {
		return maxHealth > 0.0F && health <= maxHealth * 0.25F;
	}

	public static int scaledCadence(int baseCadence, float health, float maxHealth) {
		float scale = 1.0F;
		if (isVulnerable(health, maxHealth)) {
			scale = 0.35F;
		} else if (isArmored(health, maxHealth)) {
			scale = 0.7F;
		}
		return Math.max(MIN_ATTACK_CADENCE, Math.round(baseCadence * scale));
	}

	public static boolean shouldTrigger(int tickCount, int randomOffset, int baseCadence, float health, float maxHealth) {
		int cadence = scaledCadence(baseCadence, health, maxHealth);
		return Math.floorMod(tickCount + randomOffset, cadence) == 0;
	}

	public static float regenerationPerTick(float health, float maxHealth) {
		if (isVulnerable(health, maxHealth)) {
			return 0.1F;
		}
		if (isArmored(health, maxHealth)) {
			return 0.05F;
		}
		return 0.0F;
	}
}
