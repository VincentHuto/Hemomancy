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

	// ──────────────── Hemostasis ────────────────
	// Reduces blood lost when taking damage. Each level reduces loss by 10%.

	/** Multiplier for blood lost on damage. e.g. 0.7 = 30% less blood lost */
	public static double getHemostasisMultiplier() {
		SkillPoint sp = SkillPointInit.skill_hemostasis;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return Math.max(0.4, 1.0 - sp.getCurrentLevel() * 0.10);
	}

	// ──────────────── Sanguine Surge ────────────────
	// Passive blood regeneration per tick. Each level adds +1 regen/tick.

	/** Extra blood regen per tick from Sanguine Surge. */
	public static double getSanguineSurgeRegen() {
		SkillPoint sp = SkillPointInit.skill_sanguine_surge;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel() * 1.0;
	}

	// ──────────────── Crimson Mastery ────────────────
	// Increases manipulation damage/effectiveness. Each level adds +15%.

	/** Bonus multiplier for manipulation damage. e.g. 1.45 = +45% */
	public static double getCrimsonMasteryMultiplier() {
		SkillPoint sp = SkillPointInit.skill_crimson_mastery;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.15;
	}

	// ──────────────── Vital Link ────────────────
	// Chance to heal when dealing damage with manipulations.
	// Each level adds +10% chance.

	/** Probability (0..1) of healing on hit. */
	public static double getVitalLinkChance() {
		SkillPoint sp = SkillPointInit.skill_vital_link;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel() * 0.10;
	}

	// ──────────────── Iron Will ────────────────
	// Damage reduction while blood is below 15%. Each level adds +10%.

	/** Damage reduction multiplier when low blood. e.g. 0.7 = 30% less damage */
	public static double getIronWillMultiplier() {
		SkillPoint sp = SkillPointInit.skill_iron_will;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return Math.max(0.4, 1.0 - sp.getCurrentLevel() * 0.10);
	}

	/** Threshold ratio below which Iron Will triggers. */
	public static double getIronWillThreshold() {
		return 0.15;
	}

	// ──────────────── Blood Flow ────────────────
	// Reduces manipulation cooldown. Each level reduces cooldown by 5%.

	/** Cooldown multiplier. e.g. 0.75 = 25% faster cooldowns */
	public static double getBloodFlowMultiplier() {
		SkillPoint sp = SkillPointInit.skill_blood_flow;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return Math.max(0.5, 1.0 - sp.getCurrentLevel() * 0.05);
	}

	// ──────────────── Coagulation ────────────────
	// Chance to block incoming bleed/blood-drain effects. Each level +15%.

	/** Probability (0..1) of blocking a bleed effect. */
	public static double getCoagulationChance() {
		SkillPoint sp = SkillPointInit.skill_coagulation;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel() * 0.15;
	}

	// ──────────────── Sanguine Reach ────────────────
	// Increases effective range of ranged blood manipulations.
	// Each level adds +15% range bonus.

	/** Range multiplier for ranged manipulations. e.g. 1.45 = +45% range */
	public static double getSanguineReachMultiplier() {
		SkillPoint sp = SkillPointInit.skill_sanguine_reach;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.15;
	}

	// ──────────────── Scar Affinity ────────────────
	// Increases scar effect potency (synergy bonus amounts). Each level adds +10%.

	/** Multiplier applied to scar synergy attribute bonuses. e.g. 1.2 = +20% potency */
	public static double getScarAffinityMultiplier() {
		SkillPoint sp = SkillPointInit.skill_scar_affinity;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.10;
	}

	// ──────────────── Scar Resonance ────────────────
	// Adds extra equippable scar slots. Each level adds +1 slot (base 4, max 7).

	/** Number of bonus scar slots granted by Scar Resonance. */
	public static int getScarResonanceSlots() {
		SkillPoint sp = SkillPointInit.skill_scar_resonance;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel();
	}

	// ──────────────── Scar Mastery ────────────────
	// Increases duration of finite effects applied via scar abilities. Each level adds +20%.

	/** Duration multiplier for scar-triggered status effects. e.g. 1.4 = +40% longer */
	public static double getScarMasteryDurationMultiplier() {
		SkillPoint sp = SkillPointInit.skill_scar_mastery;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 1.0;
		return 1.0 + sp.getCurrentLevel() * 0.20;
	}

	public static int getPuppetSkeinLevel() {
		SkillPoint sp = SkillPointInit.skill_puppet_skein;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel();
	}

	public static int getLivingSinewLevel() {
		SkillPoint sp = SkillPointInit.skill_living_sinew;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel();
	}

	public static int getFarTetherLevel() {
		SkillPoint sp = SkillPointInit.skill_far_tether;
		if (sp == null || sp.getState() != EnumSkillStates.UNLOCKED) return 0;
		return sp.getCurrentLevel();
	}
}
