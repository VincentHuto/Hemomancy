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
							.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(376, 416).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vascular_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(22, "skill_vascular_draw", 350, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(310, 496).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_crimson_projection = SkillPointInit.registerSkill(branch,
					new SkillPoint(23, "skill_crimson_projection", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(260, 400).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_projection.get())));
			SkillPointInit.skill_hematic_focus = SkillPointInit.registerSkill(branch,
					new SkillPoint(24, "skill_hematic_focus", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_projection)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(376, 432).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.skill_vascular_draw, SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vespers_refusal = SkillPointInit.registerSkill(branch,
					new SkillPoint(25, "skill_vespers_refusal", 700, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_hematic_focus)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(260, 400).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.memory_of_vesper.get())));
		// </skill-editor>
	}
}
