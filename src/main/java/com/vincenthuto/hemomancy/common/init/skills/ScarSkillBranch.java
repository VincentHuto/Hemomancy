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
					new SkillPoint(15, "skill_scar_affinity", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_coagulation)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(439, 750).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
			SkillPointInit.skill_scar_resonance = SkillPointInit.registerSkill(branch,
					new SkillPoint(16, "skill_scar_resonance", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_affinity)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(521, 750).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_heart.get())));
			SkillPointInit.skill_scar_mastery = SkillPointInit.registerSkill(branch,
					new SkillPoint(17, "skill_scar_mastery", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_resonance)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(439, 650).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_transcendence.get())));
			SkillPointInit.skill_deep_inscription = SkillPointInit.registerSkill(branch,
					new SkillPoint(28, "skill_deep_inscription", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_resonance)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(521, 650).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
			SkillPointInit.skill_fungal_symbiosis = SkillPointInit.registerSkill(branch,
					new SkillPoint(29, "skill_fungal_symbiosis", 750, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_deep_inscription)
							.addParents(SkillPointInit.deep_base_skill)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(480, 700).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.fungal_spine.get())));
		// </skill-editor>
	}
}
