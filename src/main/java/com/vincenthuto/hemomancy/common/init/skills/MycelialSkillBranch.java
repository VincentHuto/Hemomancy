package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class MycelialSkillBranch {
	private MycelialSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="mycelial">
			SkillPointInit.skill_sporitic_attunement = SkillPointInit.registerSkill(branch,
					new SkillPoint(34, "skill_sporitic_attunement", 450, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_coagulation)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(289, 671).setBranch("mycelial").setBranchColor(0xFF6E8F3A)
							.setIconItem(() -> new ItemStack(ItemInit.sporitic_thurible.get())));
			SkillPointInit.skill_hyphal_cultivation = SkillPointInit.registerSkill(branch,
					new SkillPoint(35, "skill_hyphal_cultivation", 550, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_sporitic_attunement)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(395, 565).setBranch("mycelial").setBranchColor(0xFF6E8F3A)
							.setIconItem(() -> new ItemStack(ItemInit.mycophant_tendril.get())));
			SkillPointInit.skill_qliphoth_gestation = SkillPointInit.registerSkill(branch,
					new SkillPoint(36, "skill_qliphoth_gestation", 750, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_hyphal_cultivation)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(324, 636).setBranch("mycelial").setBranchColor(0xFF6E8F3A)
							.setIconItem(() -> new ItemStack(ItemInit.qliphoth_pome.get())));
			SkillPointInit.skill_primal_morphogenesis = SkillPointInit.registerSkill(branch,
					new SkillPoint(37, "skill_primal_morphogenesis", 1000, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_qliphoth_gestation)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(5).setRequiredDegree(8).setTreePosition(289, 671).setBranch("mycelial").setBranchColor(0xFF6E8F3A)
							.setIconItem(() -> new ItemStack(ItemInit.morphling_fungal.get())));
		// </skill-editor>
	}
}
