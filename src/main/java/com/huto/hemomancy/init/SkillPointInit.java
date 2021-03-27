package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.skills.EnumSkillStates;
import com.huto.hemomancy.capa.skills.SkillPoint;

public class SkillPointInit {

	public static List<SkillPoint> SKILLS = new ArrayList<SkillPoint>();
	public static SkillPoint blood_shot, blood_rush, aneurysm;

	public static void init() {
		blood_shot = register(new SkillPoint("base", 0, EnumSkillStates.BASE));
	}

	public static SkillPoint register(SkillPoint manip) {
		SKILLS.add(manip);
		return manip;
	}

}
