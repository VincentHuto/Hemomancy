package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkillPointInit {

	public static List<List<SkillPoint>> SKILL_TREE = new ArrayList<>();
	public static List<SkillPoint> BASE = new ArrayList<>();
	public static SkillPoint base_skill, skill_capacity, skill_efficiency, skill_last_wind, skill_dynamic_use,
			skill_feeding_frenzy, skill_hemostasis, skill_sanguine_surge, skill_crimson_mastery,
			skill_vital_link, skill_iron_will, skill_blood_flow, skill_coagulation, skill_sanguine_reach,
			skill_manip_slots,
			skill_scar_affinity, skill_scar_resonance, skill_scar_mastery,
			skill_puppet_skein, skill_living_sinew, skill_far_tether;

	// ── Milestone tracking ──
	public static void init() {
		initBaseBranch();

	}

	public static void initBaseBranch() {
		// SkillPoint(id, name, bloodCostPerLevel, maxLevels, state, parent)
		base_skill = registerSkill(BASE, new SkillPoint(0, "base", 0, 1, EnumSkillStates.UNLOCKED, null)
				.setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));

		skill_capacity = registerSkill(BASE,
				new SkillPoint(1, "skill_capacity", 100, 5, EnumSkillStates.LOCKED, base_skill)
						.setIconItem(() -> new ItemStack(ItemInit.vitality_chalice.get())));
		skill_efficiency = registerSkill(BASE,
				new SkillPoint(2, "skill_efficiency", 100, 5, EnumSkillStates.LOCKED, base_skill)
						.setIconItem(() -> new ItemStack(ItemInit.recycled_enzyme.get())));
		skill_last_wind = registerSkill(BASE,
				new SkillPoint(3, "skill_last_wind", 300, 3, EnumSkillStates.LOCKED, skill_capacity)
						.setSkillPointCost(2).setRequiredDegree(2)
						.setIconItem(() -> new ItemStack(ItemInit.sanguine_salve.get())));
		skill_dynamic_use = registerSkill(BASE,
				new SkillPoint(4, "skill_dynamic_use", 300, 3, EnumSkillStates.LOCKED, skill_efficiency)
						.setSkillPointCost(2).setRequiredDegree(2)
						.setIconItem(() -> new ItemStack(ItemInit.blood_tendency_gauge.get())));
		skill_feeding_frenzy = registerSkill(BASE,
				new SkillPoint(5, "skill_feeding_frenzy", 500, 3, EnumSkillStates.LOCKED, skill_last_wind)
						.setSkillPointCost(3).setRequiredDegree(3)
						.setIconItem(() -> new ItemStack(ItemInit.swollen_leech.get())));

		// ── New skills ──
		skill_hemostasis = registerSkill(BASE,
				new SkillPoint(6, "skill_hemostasis", 200, 3, EnumSkillStates.LOCKED, skill_efficiency)
						.setSkillPointCost(2).setRequiredDegree(2)
						.setIconItem(() -> new ItemStack(ItemInit.sanguine_conduit.get())));
		skill_sanguine_surge = registerSkill(BASE,
				new SkillPoint(7, "skill_sanguine_surge", 200, 3, EnumSkillStates.LOCKED, skill_capacity)
						.setSkillPointCost(2).setRequiredDegree(2)
						.setIconItem(() -> new ItemStack(ItemInit.blood_crystal_shard.get())));
		skill_crimson_mastery = registerSkill(BASE,
				new SkillPoint(8, "skill_crimson_mastery", 400, 3, EnumSkillStates.LOCKED, skill_dynamic_use)
						.setSkillPointCost(3).setRequiredDegree(4)
						.setIconItem(() -> new ItemStack(ItemInit.hematic_memory.get())));
		skill_vital_link = registerSkill(BASE,
				new SkillPoint(9, "skill_vital_link", 600, 3, EnumSkillStates.LOCKED, skill_feeding_frenzy)
						.setSkillPointCost(4).setRequiredDegree(5)
						.setIconItem(() -> new ItemStack(ItemInit.bleeding_bulb.get())));
		skill_iron_will = registerSkill(BASE,
				new SkillPoint(10, "skill_iron_will", 400, 3, EnumSkillStates.LOCKED, skill_last_wind)
						.setSkillPointCost(3).setRequiredDegree(3)
						.setIconItem(() -> new ItemStack(ItemInit.hematic_iron_scrap.get())));
		skill_blood_flow = registerSkill(BASE,
				new SkillPoint(11, "skill_blood_flow", 300, 5, EnumSkillStates.LOCKED, skill_hemostasis)
						.setSkillPointCost(2).setRequiredDegree(3)
						.setIconItem(() -> new ItemStack(ItemInit.dicentra_sap.get())));
		skill_coagulation = registerSkill(BASE,
				new SkillPoint(12, "skill_coagulation", 500, 3, EnumSkillStates.LOCKED, skill_hemostasis)
						.setSkillPointCost(3).setRequiredDegree(4)
						.setIconItem(() -> new ItemStack(ItemInit.foul_paste.get())));
		skill_sanguine_reach = registerSkill(BASE,
				new SkillPoint(13, "skill_sanguine_reach", 400, 3, EnumSkillStates.LOCKED, skill_crimson_mastery)
						.setSkillPointCost(3).setRequiredDegree(5)
						.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));
		skill_manip_slots = registerSkill(BASE,
				new SkillPoint(14, "skill_manip_slots", 200, 5, EnumSkillStates.LOCKED, base_skill)
						.setSkillPointCost(2).setRequiredDegree(1)
						.setIconItem(() -> new ItemStack(ItemInit.scrying_dish.get())));

		// ── Scar branch (Tier 4 — Adept gated) ──
		skill_scar_affinity = registerSkill(BASE,
				new SkillPoint(15, "skill_scar_affinity", 400, 3, EnumSkillStates.LOCKED, skill_crimson_mastery)
						.setSkillPointCost(3).setRequiredDegree(4)
						.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
		skill_scar_resonance = registerSkill(BASE,
				new SkillPoint(16, "skill_scar_resonance", 500, 3, EnumSkillStates.LOCKED, skill_scar_affinity)
						.setSkillPointCost(3).setRequiredDegree(4)
						.setIconItem(() -> new ItemStack(ItemInit.scar_heart.get())));
		skill_scar_mastery = registerSkill(BASE,
				new SkillPoint(17, "skill_scar_mastery", 600, 3, EnumSkillStates.LOCKED, skill_scar_resonance)
						.setSkillPointCost(4).setRequiredDegree(5)
						.setIconItem(() -> new ItemStack(ItemInit.scar_transcendence.get())));

		skill_puppet_skein = registerSkill(BASE,
				new SkillPoint(18, "skill_puppet_skein", 350, 3, EnumSkillStates.LOCKED, skill_manip_slots)
						.setSkillPointCost(2).setRequiredDegree(2)
						.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));
		skill_living_sinew = registerSkill(BASE,
				new SkillPoint(19, "skill_living_sinew", 450, 3, EnumSkillStates.LOCKED, skill_puppet_skein)
						.setSkillPointCost(3).setRequiredDegree(3)
						.setIconItem(() -> new ItemStack(ItemInit.sanguine_quintessence.get())));
		skill_far_tether = registerSkill(BASE,
				new SkillPoint(20, "skill_far_tether", 400, 3, EnumSkillStates.LOCKED, skill_puppet_skein)
						.setSkillPointCost(3).setRequiredDegree(3)
						.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));

		registerSkillBranch(BASE);
	}

	// Adds the skill to the selected Branch
	public static SkillPoint registerSkill(List<SkillPoint> branch, SkillPoint manip) {
		branch.add(manip);
		return manip;
	}

	// Adds branch to the greater skill tree
	public static List<SkillPoint> registerSkillBranch(List<SkillPoint> branch) {
		SKILL_TREE.add(branch);
		return branch;
	}

	/** Get a flat list of every registered skill */
	public static List<SkillPoint> getAllSkills() {
		List<SkillPoint> all = new ArrayList<>();
		for (List<SkillPoint> branch : SKILL_TREE) {
			all.addAll(branch);
		}
		return all;
	}

	/** Lookup a skill by its integer ID, or null if not found */
	public static SkillPoint getById(int id) {
		for (List<SkillPoint> branch : SKILL_TREE) {
			for (SkillPoint sp : branch) {
				if (sp.getId() == id) return sp;
			}
		}
		return null;
	}

	/** Lookup a skill by its registered name, or null if not found. */
	public static SkillPoint getByName(String name) {
		for (List<SkillPoint> branch : SKILL_TREE) {
			for (SkillPoint sp : branch) {
				if (sp.getName().equals(name)) return sp;
			}
		}
		return null;
	}

	// ── Milestone helpers ──

}
