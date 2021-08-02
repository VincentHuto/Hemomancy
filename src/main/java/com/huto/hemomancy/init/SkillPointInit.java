package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.skills.EnumSkillStates;
import com.huto.hemomancy.capa.skills.SkillPoint;

public class SkillPointInit {

	public static List<List<SkillPoint>> SKILL_TREE = new ArrayList<List<SkillPoint>>();
	public static List<SkillPoint> BASE = new ArrayList<SkillPoint>();
	public static SkillPoint base_skill, skill_capacity, skill_efficiency, skill_last_wind, skill_dynamic_use,
			skill_feeding_frenzy;

	public static void init() {
		initBaseBranch();

	}

	public static void initBaseBranch() {
		base_skill = registerSkill(BASE, new SkillPoint(0, "base", 1, 0, EnumSkillStates.UNLOCKED, null));

		skill_capacity = registerSkill(BASE,
				new SkillPoint(1, "skill_capacity", 3, 100, EnumSkillStates.LOCKED, base_skill));
		skill_efficiency = registerSkill(BASE,
				new SkillPoint(2, "skill_efficiency", 3, 100, EnumSkillStates.LOCKED, base_skill));
		skill_last_wind = registerSkill(BASE,
				new SkillPoint(3, "skill_last_wind", 2, 300, EnumSkillStates.LOCKED, skill_capacity));
		skill_dynamic_use = registerSkill(BASE,
				new SkillPoint(4, "skill_dynamic_use", 2, 300, EnumSkillStates.LOCKED, skill_efficiency));
		skill_feeding_frenzy = registerSkill(BASE,
				new SkillPoint(5, "skill_feeding_frenzy", 3, 100, EnumSkillStates.LOCKED, skill_feeding_frenzy));
		registerSkillBranch(BASE);
	}

	// Adds branch to the greater skill tree
	public static List<SkillPoint> registerSkillBranch(List<SkillPoint> branch) {
		SKILL_TREE.add(branch);
		return branch;
	}

	// Adds the skill to the selected Branch
	public static SkillPoint registerSkill(List<SkillPoint> branch, SkillPoint manip) {
		branch.add(manip);
		return manip;
	}

}
