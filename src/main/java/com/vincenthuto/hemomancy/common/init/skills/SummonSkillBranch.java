package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SummonSkillBranch {
	private SummonSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="summons">
			SkillPointInit.skill_puppet_skein = SkillPointInit.registerSkill(branch,
					new SkillPoint(18, "skill_puppet_skein", 350, 3, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(670, 480).setBranch("summons").setBranchColor(0xFF2370DB)
							.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));
			SkillPointInit.skill_living_sinew = SkillPointInit.registerSkill(branch,
					new SkillPoint(19, "skill_living_sinew", 450, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_puppet_skein)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(740, 439).setBranch("summons").setBranchColor(0xFF2370DB)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_quintessence.get())));
			SkillPointInit.skill_far_tether = SkillPointInit.registerSkill(branch,
					new SkillPoint(20, "skill_far_tether", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_puppet_skein)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(740, 521).setBranch("summons").setBranchColor(0xFF2370DB)
							.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));
		// </skill-editor>
	}
}
