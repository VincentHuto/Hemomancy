package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.skills.SkillPoint;

public class SkillPointInit {

	public static List<List<SkillPoint>> SKILL_TREE = new ArrayList<List<SkillPoint>>();
	public static List<SkillPoint> BASE = new ArrayList<SkillPoint>();
	public static SkillPoint base_skill;

	public static void init() {
		initBaseBranch();

	}

	public static void initBaseBranch() {
		base_skill = registerSkill(BASE, new SkillPoint("base"));
		registerSkillBranch(BASE);
	}

	public static List<SkillPoint> registerSkillBranch(List<SkillPoint> branch) {
		SKILL_TREE.add(branch);
		return branch;
	}

	public static SkillPoint registerSkill(List<SkillPoint> branch, SkillPoint manip) {
		branch.add(manip);
		return manip;
	}

}
