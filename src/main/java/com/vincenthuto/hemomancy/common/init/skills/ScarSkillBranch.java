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
					new SkillPoint(15, "skill_scar_affinity", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_dynamic_use)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(551, 686).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
			SkillPointInit.skill_scar_resonance = SkillPointInit.registerSkill(branch,
					new SkillPoint(16, "skill_scar_resonance", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_affinity)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(585, 750).setBranch("scars").setBranchColor(0xFF9A9A9F)
							.setIconItem(() -> new ItemStack(ItemInit.scar_heart.get())));
			SkillPointInit.skill_scar_mastery = SkillPointInit.registerSkill(branch,
					new SkillPoint(17, "skill_scar_mastery", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_resonance)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(567, 618).setBranch("scars").setBranchColor(0xFF9A9A9F).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.scar_transcendence.get())));
			SkillPointInit.skill_deep_inscription = SkillPointInit.registerSkill(branch,
					new SkillPoint(28, "skill_deep_inscription", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_scar_resonance)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(489, 650).setBranch("scars").setBranchColor(0xFF9A9A9F).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.scar_blank.get())));
			SkillPointInit.skill_fungal_symbiosis = SkillPointInit.registerSkill(branch,
					new SkillPoint(29, "skill_fungal_symbiosis", 750, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_deep_inscription)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(400, 684).setBranch("scars").setBranchColor(0xFF9A9A9F).addParents(SkillPointInit.skill_hyphal_cultivation)
							.setIconItem(() -> new ItemStack(ItemInit.fungal_spine.get())));
			SkillPointInit.skill_deep_scar_resonance = SkillPointInit.registerSkill(branch,
					new SkillPoint(63, "skill_deep_scar_resonance", 650, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_deep_inscription)
							.setSkillPointCost(4).setRequiredDegree(5).setTreePosition(662, 730).setBranch("scars").setBranchColor(0xFF9A9A9F).setToggleable(true).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
		// </skill-editor>
	}
}
