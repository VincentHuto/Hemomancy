package com.vincenthuto.hemomancy.common.init.skills;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CoreSkillBranch {
	private CoreSkillBranch() {
	}

	public static void register(List<SkillPoint> branch) {
		// <skill-editor branch="core">
			SkillPointInit.setDegreeLabelPosition(0, 410, 540);
			SkillPointInit.setDegreeLabelPosition(1, 362, 558);
			SkillPointInit.setDegreeLabelPosition(2, 298, 576);
			SkillPointInit.setDegreeLabelPosition(3, 218, 594);
			SkillPointInit.setDegreeLabelPosition(4, 154, 612);
			SkillPointInit.setDegreeLabelPosition(5, 522, 566);
			SkillPointInit.setDegreeLabelPosition(6, 570, 584);
			SkillPointInit.setDegreeLabelPosition(7, 618, 602);
			SkillPointInit.setDegreeLabelPosition(8, 666, 620);
			SkillPointInit.base_skill = SkillPointInit.registerSkill(branch,
					new SkillPoint(0, "base", 0, 1, EnumSkillStates.UNLOCKED, null)
							.setTreePosition(480, 480).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
			SkillPointInit.deep_base_skill = SkillPointInit.registerSkill(branch,
					new SkillPoint(38, "deep_base", 0, 1, EnumSkillStates.UNLOCKED, null)
							.setSkillPointCost(1).setRequiredDegree(5).setTreePosition(480, 480).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_memory.get())));
			SkillPointInit.skill_capacity = SkillPointInit.registerSkill(branch,
					new SkillPoint(1, "skill_capacity", 100, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
							.setTreePosition(455, 408).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.vitality_chalice.get())));
			SkillPointInit.skill_efficiency = SkillPointInit.registerSkill(branch,
					new SkillPoint(2, "skill_efficiency", 100, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
							.setTreePosition(521, 408).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.recycled_enzyme.get())));
			SkillPointInit.skill_last_wind = SkillPointInit.registerSkill(branch,
					new SkillPoint(3, "skill_last_wind", 300, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_capacity)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(389, 326).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_salve.get())));
			SkillPointInit.skill_dynamic_use = SkillPointInit.registerSkill(branch,
					new SkillPoint(4, "skill_dynamic_use", 300, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_efficiency)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(505, 310).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.blood_tendency_gauge.get())));
			SkillPointInit.skill_feeding_frenzy = SkillPointInit.registerSkill(branch,
					new SkillPoint(5, "skill_feeding_frenzy", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_last_wind)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(391, 210).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.swollen_leech.get())));
			SkillPointInit.skill_hemostasis = SkillPointInit.registerSkill(branch,
					new SkillPoint(6, "skill_hemostasis", 200, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_efficiency)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(587, 342).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_conduit.get())));
			SkillPointInit.skill_sanguine_surge = SkillPointInit.registerSkill(branch,
					new SkillPoint(7, "skill_sanguine_surge", 200, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_capacity)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(455, 310).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.blood_crystal_shard.get())));
			SkillPointInit.skill_crimson_mastery = SkillPointInit.registerSkill(branch,
					new SkillPoint(8, "skill_crimson_mastery", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_dynamic_use)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(487, 376).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_memory.get())));
			SkillPointInit.skill_vital_link = SkillPointInit.registerSkill(branch,
					new SkillPoint(9, "skill_vital_link", 600, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_feeding_frenzy)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(384, 342).setBranch("core").setBranchColor(0xFFD00000).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.bleeding_bulb.get())));
			SkillPointInit.skill_iron_will = SkillPointInit.registerSkill(branch,
					new SkillPoint(10, "skill_iron_will", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_last_wind)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(311, 324).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.hematic_iron_scrap.get())));
			SkillPointInit.skill_blood_flow = SkillPointInit.registerSkill(branch,
					new SkillPoint(11, "skill_blood_flow", 300, 5, EnumSkillStates.LOCKED, SkillPointInit.skill_hemostasis)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(649, 340).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.dicentra_sap.get())));
			SkillPointInit.skill_coagulation = SkillPointInit.registerSkill(branch,
					new SkillPoint(12, "skill_coagulation", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_hemostasis)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(569, 274).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.foul_paste.get())));
			SkillPointInit.skill_sanguine_reach = SkillPointInit.registerSkill(branch,
					new SkillPoint(13, "skill_sanguine_reach", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_mastery)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(473, 312).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.puppeteering_thread.get())));
			SkillPointInit.skill_manip_slots = SkillPointInit.registerSkill(branch,
					new SkillPoint(14, "skill_manip_slots", 200, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
							.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(368, 504).setBranch("core").setBranchColor(0xFFD00000)
							.setIconItem(() -> new ItemStack(ItemInit.scrying_dish.get())));
		// </skill-editor>
	}
}
