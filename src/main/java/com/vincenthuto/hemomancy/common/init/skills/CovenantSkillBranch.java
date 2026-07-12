package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CovenantSkillBranch {
	private CovenantSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="covenant">
			SkillPointInit.skill_fane_suture = SkillPointInit.registerSkill(branch,
					new SkillPoint(30, "skill_fane_suture", 550, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_blood_flow)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(581, 411).setBranch("covenant").setBranchColor(0xFFA54569).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_suture_needle.get())));
			SkillPointInit.skill_bloodline_concord = SkillPointInit.registerSkill(branch,
					new SkillPoint(31, "skill_bloodline_concord", 650, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_fane_suture)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(568, 328).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_quintessence.get())));
			SkillPointInit.skill_servitor_tender = SkillPointInit.registerSkill(branch,
					new SkillPoint(32, "skill_servitor_tender", 650, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_fane_suture)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(642, 434).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_suture_needle.get())));
			SkillPointInit.skill_ancestral_sovereignty = SkillPointInit.registerSkill(branch,
					new SkillPoint(33, "skill_ancestral_sovereignty", 850, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_bloodline_concord)
							.setSkillPointCost(5).setRequiredDegree(7).setTreePosition(652, 340).setBranch("covenant").setBranchColor(0xFFA54569).addParents(SkillPointInit.skill_servitor_tender)
							.setIconItem(() -> new ItemStack(ItemInit.covenant_mantle.get())));
		// </skill-editor>
	}
}
