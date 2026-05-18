package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Converts skill progress into gameplay bonuses.
 * Server gameplay must pass the player so bonuses are read from that player's
 * persistent attachment. No-arg overloads are client display helpers backed by
 * the latest skill sync packet.
 */
public final class SkillPointHelper {
	private SkillPointHelper() {}

	public static SkillProgress progress(@Nullable Player player) {
		return player == null
				? SkillProgressClientCache.current()
				: HemoCapabilityAccess.getSkillProgress(player).orElseGet(SkillProgressClientCache::current);
	}

	public static int getSkillLevel(@Nullable Player player, SkillPoint skill) {
		return progress(player).getLevel(skill);
	}

	public static boolean isUnlocked(@Nullable Player player, SkillPoint skill) {
		return progress(player).isUnlocked(skill);
	}

	public static double getCapacityBonus(Player player) {
		return getCapacityBonus(progress(player));
	}

	public static double getCapacityBonus() {
		return getCapacityBonus(progress(null));
	}

	private static double getCapacityBonus(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_capacity;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp) * 500.0;
	}

	public static double getEfficiencyMultiplier(Player player) {
		return getEfficiencyMultiplier(progress(player));
	}

	public static double getEfficiencyMultiplier() {
		return getEfficiencyMultiplier(progress(null));
	}

	private static double getEfficiencyMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_efficiency;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return Math.pow(0.92, progress.getLevel(sp));
	}

	public static double getLastWindRegenPerTick(Player player) {
		return getLastWindRegenPerTick(progress(player));
	}

	public static double getLastWindRegenPerTick() {
		return getLastWindRegenPerTick(progress(null));
	}

	private static double getLastWindRegenPerTick(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_last_wind;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp) * 2.0;
	}

	public static double getLastWindThreshold() {
		return 0.10;
	}

	public static double getDynamicUseMultiplier(Player player) {
		return getDynamicUseMultiplier(progress(player));
	}

	public static double getDynamicUseMultiplier() {
		return getDynamicUseMultiplier(progress(null));
	}

	private static double getDynamicUseMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_dynamic_use;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.10;
	}

	public static double getFeedingFrenzyMultiplier(Player player) {
		return getFeedingFrenzyMultiplier(progress(player));
	}

	public static double getFeedingFrenzyMultiplier() {
		return getFeedingFrenzyMultiplier(progress(null));
	}

	private static double getFeedingFrenzyMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_feeding_frenzy;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.25;
	}

	public static double getHemostasisMultiplier(Player player) {
		return getHemostasisMultiplier(progress(player));
	}

	public static double getHemostasisMultiplier() {
		return getHemostasisMultiplier(progress(null));
	}

	private static double getHemostasisMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_hemostasis;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return Math.max(0.4, 1.0 - progress.getLevel(sp) * 0.10);
	}

	public static double getSanguineSurgeRegen(Player player) {
		return getSanguineSurgeRegen(progress(player));
	}

	public static double getSanguineSurgeRegen() {
		return getSanguineSurgeRegen(progress(null));
	}

	private static double getSanguineSurgeRegen(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_sanguine_surge;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp) * 1.0;
	}

	public static double getCrimsonMasteryMultiplier(Player player) {
		return getCrimsonMasteryMultiplier(progress(player));
	}

	public static double getCrimsonMasteryMultiplier() {
		return getCrimsonMasteryMultiplier(progress(null));
	}

	private static double getCrimsonMasteryMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_crimson_mastery;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.15;
	}

	public static double getVitalLinkChance(Player player) {
		return getVitalLinkChance(progress(player));
	}

	public static double getVitalLinkChance() {
		return getVitalLinkChance(progress(null));
	}

	private static double getVitalLinkChance(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_vital_link;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp) * 0.10;
	}

	public static double getIronWillMultiplier(Player player) {
		return getIronWillMultiplier(progress(player));
	}

	public static double getIronWillMultiplier() {
		return getIronWillMultiplier(progress(null));
	}

	private static double getIronWillMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_iron_will;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return Math.max(0.4, 1.0 - progress.getLevel(sp) * 0.10);
	}

	public static double getIronWillThreshold() {
		return 0.15;
	}

	public static double getBloodFlowMultiplier(Player player) {
		return getBloodFlowMultiplier(progress(player));
	}

	public static double getBloodFlowMultiplier() {
		return getBloodFlowMultiplier(progress(null));
	}

	private static double getBloodFlowMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_blood_flow;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return Math.max(0.5, 1.0 - progress.getLevel(sp) * 0.05);
	}

	public static double getCoagulationChance(Player player) {
		return getCoagulationChance(progress(player));
	}

	public static double getCoagulationChance() {
		return getCoagulationChance(progress(null));
	}

	private static double getCoagulationChance(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_coagulation;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp) * 0.15;
	}

	public static double getSanguineReachMultiplier(Player player) {
		return getSanguineReachMultiplier(progress(player));
	}

	public static double getSanguineReachMultiplier() {
		return getSanguineReachMultiplier(progress(null));
	}

	private static double getSanguineReachMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_sanguine_reach;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.15;
	}

	public static double getScarAffinityMultiplier(Player player) {
		return getScarAffinityMultiplier(progress(player));
	}

	public static double getScarAffinityMultiplier() {
		return getScarAffinityMultiplier(progress(null));
	}

	private static double getScarAffinityMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_scar_affinity;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.10;
	}

	public static int getScarResonanceSlots(Player player) {
		return getScarResonanceSlots(progress(player));
	}

	public static int getScarResonanceSlots() {
		return getScarResonanceSlots(progress(null));
	}

	private static int getScarResonanceSlots(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_scar_resonance;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp);
	}

	public static double getScarMasteryDurationMultiplier(Player player) {
		return getScarMasteryDurationMultiplier(progress(player));
	}

	public static double getScarMasteryDurationMultiplier() {
		return getScarMasteryDurationMultiplier(progress(null));
	}

	private static double getScarMasteryDurationMultiplier(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_scar_mastery;
		if (sp == null || !progress.isUnlocked(sp)) return 1.0;
		return 1.0 + progress.getLevel(sp) * 0.20;
	}

	public static int getPuppetSkeinLevel(Player player) {
		return getPuppetSkeinLevel(progress(player));
	}

	public static int getPuppetSkeinLevel() {
		return getPuppetSkeinLevel(progress(null));
	}

	private static int getPuppetSkeinLevel(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_puppet_skein;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp);
	}

	public static int getLivingSinewLevel(Player player) {
		return getLivingSinewLevel(progress(player));
	}

	public static int getLivingSinewLevel() {
		return getLivingSinewLevel(progress(null));
	}

	private static int getLivingSinewLevel(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_living_sinew;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp);
	}

	public static int getFarTetherLevel(Player player) {
		return getFarTetherLevel(progress(player));
	}

	public static int getFarTetherLevel() {
		return getFarTetherLevel(progress(null));
	}

	private static int getFarTetherLevel(SkillProgress progress) {
		SkillPoint sp = SkillPointInit.skill_far_tether;
		if (sp == null || !progress.isUnlocked(sp)) return 0;
		return progress.getLevel(sp);
	}
}
