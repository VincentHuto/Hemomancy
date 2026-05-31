package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.skills.CoreSkillBranch;
import com.vincenthuto.hemomancy.common.init.skills.LivingStaffSkillBranch;
import com.vincenthuto.hemomancy.common.init.skills.ScarSkillBranch;
import com.vincenthuto.hemomancy.common.init.skills.SummonSkillBranch;

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
			skill_puppet_skein, skill_living_sinew, skill_far_tether,
			skill_living_conduit, skill_vascular_draw, skill_crimson_projection;

	public static void init() {
		initBaseBranch();
	}

	public static void initBaseBranch() {
		CoreSkillBranch.register(BASE);
		ScarSkillBranch.register(BASE);
		SummonSkillBranch.register(BASE);
		LivingStaffSkillBranch.register(BASE);
		registerSkillBranch(BASE);
	}

	public static SkillPoint registerSkill(List<SkillPoint> branch, SkillPoint manip) {
		branch.add(manip);
		return manip;
	}

	public static List<SkillPoint> registerSkillBranch(List<SkillPoint> branch) {
		SKILL_TREE.add(branch);
		return branch;
	}

	public static List<SkillPoint> getAllSkills() {
		List<SkillPoint> all = new ArrayList<>();
		for (List<SkillPoint> branch : SKILL_TREE) {
			all.addAll(branch);
		}
		return all;
	}

	public static SkillPoint getById(int id) {
		for (List<SkillPoint> branch : SKILL_TREE) {
			for (SkillPoint sp : branch) {
				if (sp.getId() == id) {
					return sp;
				}
			}
		}
		return null;
	}

	public static SkillPoint getByName(String name) {
		for (List<SkillPoint> branch : SKILL_TREE) {
			for (SkillPoint sp : branch) {
				if (sp.getName().equals(name)) {
					return sp;
				}
			}
		}
		return null;
	}
}
