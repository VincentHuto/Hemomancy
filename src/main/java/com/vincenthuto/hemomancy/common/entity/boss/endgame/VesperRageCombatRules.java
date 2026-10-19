package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Pure selection rules for the Evening Star's final, dual-sickle rage. */
public final class VesperRageCombatRules {
	private VesperRageCombatRules() { }

	public static boolean isRageThreshold(float health, float maxHealth) {
		return maxHealth > 0.0F && VesperEveningStarAct.of(health, maxHealth) == VesperEveningStarAct.SICKLES;
	}

	public static int recoveryTicks() {
		return 3;
	}
}
