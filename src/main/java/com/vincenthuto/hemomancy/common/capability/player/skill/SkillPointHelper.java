package com.vincenthuto.hemomancy.common.capability.player.skill;

import com.vincenthuto.hemomancy.common.init.SkillPointInit;

/**
 * Central utility that converts skill levels into gameplay bonuses.
 * <p>
 * All methods are pure functions of the current {@link SkillPointInit} state,
 * so they work on both the client (for display) and the server (for gameplay).
 */
public final class SkillPointHelper {

	private SkillPointHelper() {}

	// ──────────────── Capacity ────────────────
	// Each level grants +500 max blood volume.

	/** Flat bonus to max blood volume from skill_capacity. */
	public static double getCapacityBonus() {
		SkillPoint sp = SkillPointInit.skill_capacity;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel() * 500.0;
	}

	// ──────────────── Efficiency ────────────────
	// Each level reduces manipulation blood cost by 8% (multiplicative).
	// At max level (5) → ~34% discount.

	/** Multiplier for manipulation blood cost. e.g. 0.72 = 28% discount */
	public static double getEfficiencyMultiplier() {
		SkillPoint sp = SkillPointInit.skill_efficiency;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return Math.pow(0.92, sp.getCurrentLevel());
	}

	// ──────────────── Last Wind ────────────────
	// When blood drops below 10% of max, passively regenerate extra blood
	// per tick. Each level adds +2 regen per tick in the danger zone.

	/** Extra blood regen per tick while below the 10% threshold. */
	public static double getLastWindRegenPerTick() {
		SkillPoint sp = SkillPointInit.skill_last_wind;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel() * 2.0;
	}

	/** Threshold ratio below which Last Wind triggers. */
	public static double getLastWindThreshold() {
		return 0.10;
	}

	// ──────────────── Dynamic Use ────────────────
	// Manipulations whose tendency matches the player's highest tendency
	// deal/do more. Each level grants +10% bonus effectiveness.

	/** Bonus multiplier for tendency-matched manipulations. e.g. 1.2 = +20% */
	public static double getDynamicUseMultiplier() {
		SkillPoint sp = SkillPointInit.skill_dynamic_use;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.10;
	}

	// ──────────────── Feeding Frenzy ────────────────
	// Increases blood gained from kills. Each level adds +25% bonus.

	/** Multiplier for blood gained on kill. e.g. 1.5 = +50% */
	public static double getFeedingFrenzyMultiplier() {
		SkillPoint sp = SkillPointInit.skill_feeding_frenzy;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.25;
	}
}
