package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.skills.*;

import java.util.ArrayList;
import java.util.List;

public class SkillPointInit {

	private static final int[][] DEGREE_LABEL_POSITIONS = {
			{90, 108},
			{90, 142},
			{90, 176},
			{90, 210},
			{90, 244},
			{90, 278},
			{90, 312},
			{90, 346},
			{90, 380}
	};

	public static List<List<SkillPoint>> SKILL_TREE = new ArrayList<>();
	public static List<SkillPoint> BASE = new ArrayList<>();
	public static SkillPoint base_skill, deep_base_skill, skill_capacity, skill_efficiency, skill_last_wind, skill_dynamic_use,
			skill_feeding_frenzy, skill_hemostasis, skill_sanguine_surge, skill_crimson_mastery,
			skill_vital_link, skill_iron_will, skill_blood_flow, skill_coagulation, skill_sanguine_reach,
			skill_manip_slots, skill_sanguine_crystallization, skill_synaptic_memory,
			skill_scar_affinity, skill_scar_resonance, skill_scar_mastery,
			skill_puppet_skein, skill_living_sinew, skill_far_tether,
			skill_living_conduit, skill_vascular_draw, skill_crimson_projection,
			skill_weapons_master, skill_hematic_focus, skill_vespers_refusal,
			skill_dragging_siphon, skill_mobile_conduit, skill_blood_tolerance,
			skill_unbound_siphon, skill_quickened_draw, skill_hungry_pulse,
			skill_arterial_cadence,
			skill_thread_economy, skill_skein_transposition, skill_bound_command,
			skill_deep_inscription, skill_fungal_symbiosis,
			skill_fane_suture, skill_bloodline_concord, skill_servitor_tender,
			skill_ancestral_sovereignty,
			skill_sporitic_attunement, skill_hyphal_cultivation, skill_qliphoth_gestation,
			skill_primal_morphogenesis,
			skill_persistent_arsenal, skill_distributed_siphon, skill_selective_hunger,
			skill_sated_siphon,
			skill_sanguine_reserve, skill_automatic_coagulation, skill_guarded_feeding,
			skill_shared_siphon, skill_autonomous_retaliation, skill_merciful_command,
			skill_deep_scar_resonance, skill_crimson_wake, skill_vascular_mercy,
			skill_bloodhound_sense, skill_reflexive_coagulation, skill_dormant_symbiote,
			skill_symbiotic_metabolism, skill_nerves_of_steel, skill_iron_handed,
			skill_bright_eyed, skill_light_footed, skill_high_strung, skill_sanguine_spinning;

	public static void init() {
		initBaseBranch();
	}

	public static void initBaseBranch() {
		CoreSkillBranch.register(BASE);
		LivingStaffSkillBranch.register(BASE);
		SummonSkillBranch.register(BASE);
		MycelialSkillBranch.register(BASE);
		ScarSkillBranch.register(BASE);
		CovenantSkillBranch.register(BASE);
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

	public static void setDegreeLabelPosition(int degree, int x, int y) {
		if (degree < 0 || degree >= DEGREE_LABEL_POSITIONS.length) return;
		DEGREE_LABEL_POSITIONS[degree][0] = x;
		DEGREE_LABEL_POSITIONS[degree][1] = y;
	}

	public static int[] getDegreeLabelPosition(int degree) {
		if (degree < 0 || degree >= DEGREE_LABEL_POSITIONS.length) return new int[] {90, 108};
		return DEGREE_LABEL_POSITIONS[degree];
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
