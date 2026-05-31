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
			SkillPointInit.skill_sanctum_suture = SkillPointInit.registerSkill(branch,
					new SkillPoint(30, "skill_sanctum_suture", 550, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_blood_flow)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(565, 395).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_suture_needle.get())));
			SkillPointInit.skill_bloodline_concord = SkillPointInit.registerSkill(branch,
					new SkillPoint(31, "skill_bloodline_concord", 650, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_sanctum_suture)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(600, 360).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.bloodline_pool_monitor.get())));
			SkillPointInit.skill_servitor_tender = SkillPointInit.registerSkill(branch,
					new SkillPoint(32, "skill_servitor_tender", 650, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_sanctum_suture)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(658, 418).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.drudge_electrode.get())));
			SkillPointInit.skill_ancestral_sovereignty = SkillPointInit.registerSkill(branch,
					new SkillPoint(33, "skill_ancestral_sovereignty", 850, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_bloodline_concord)
							.addParents(SkillPointInit.skill_servitor_tender, SkillPointInit.deep_base_skill)
							.setSkillPointCost(5).setRequiredDegree(7).setTreePosition(636, 324).setBranch("covenant").setBranchColor(0xFFA54569)
							.setIconItem(() -> new ItemStack(ItemInit.covenant_mantle.get())));
		// </skill-editor>
	}
}
