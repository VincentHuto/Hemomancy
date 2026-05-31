package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class LivingStaffSkillBranch {
	private LivingStaffSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="living_staff">
			SkillPointInit.skill_living_conduit = SkillPointInit.registerSkill(branch,
					new SkillPoint(21, "skill_living_conduit", 250, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_manip_slots)
							.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(360, 480).setBranch("living_staff")
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vascular_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(22, "skill_vascular_draw", 350, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(290, 480).setBranch("living_staff")
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_crimson_projection = SkillPointInit.registerSkill(branch,
					new SkillPoint(23, "skill_crimson_projection", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(220, 480).setBranch("living_staff")
							.setIconItem(() -> new ItemStack(ItemInit.blood_projection.get())));
		// </skill-editor>
	}
}
