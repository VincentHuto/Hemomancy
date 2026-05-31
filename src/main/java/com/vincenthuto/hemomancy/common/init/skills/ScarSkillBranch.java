package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ScarSkillBranch {
	private ScarSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="scars">
			SkillPointInit.skill_scar_affinity = SkillPointInit.registerSkill(branch,
					new SkillPoint(15, "skill_scar_affinity", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(480, 808).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
			SkillPointInit.skill_scar_resonance = SkillPointInit.registerSkill(branch,
					new SkillPoint(16, "skill_scar_resonance", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_affinity)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(569, 799).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_heart.get())));
			SkillPointInit.skill_scar_mastery = SkillPointInit.registerSkill(branch,
					new SkillPoint(17, "skill_scar_mastery", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_resonance)
							.setSkillPointCost(4).setRequiredDegree(5).setTreePosition(480, 880).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_transcendence.get())));
		// </skill-editor>
	}
}
