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
							.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(360, 512).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vascular_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(22, "skill_vascular_draw", 350, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(374, 608).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_dragging_siphon = SkillPointInit.registerSkill(branch,
					new SkillPoint(41, "skill_dragging_siphon", 300, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(462, 624).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_mobile_conduit = SkillPointInit.registerSkill(branch,
					new SkillPoint(42, "skill_mobile_conduit", 375, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_dragging_siphon)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(510, 688).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_blood_tolerance = SkillPointInit.registerSkill(branch,
					new SkillPoint(43, "skill_blood_tolerance", 425, 5, EnumSkillStates.LOCKED, SkillPointInit.skill_dragging_siphon)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(480, 544).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
			SkillPointInit.skill_crimson_projection = SkillPointInit.registerSkill(branch,
					new SkillPoint(23, "skill_crimson_projection", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(260, 496).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_projection.get())));
			SkillPointInit.skill_quickened_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(45, "skill_quickened_draw", 350, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(306, 672).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_hungry_pulse = SkillPointInit.registerSkill(branch,
					new SkillPoint(46, "skill_hungry_pulse", 475, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_quickened_draw)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(296, 744).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_weapons_master = SkillPointInit.registerSkill(branch,
					new SkillPoint(39, "skill_weapons_master", 450, 4, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_projection)
							.setSkillPointCost(2).setRequiredDegree(4).setTreePosition(224, 608).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.skill_vascular_draw)
							.setIconItem(() -> new ItemStack(ItemInit.living_blade.get())));
			SkillPointInit.skill_unbound_siphon = SkillPointInit.registerSkill(branch,
					new SkillPoint(44, "skill_unbound_siphon", 600, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_mobile_conduit)
							.setSkillPointCost(4).setRequiredDegree(5).setTreePosition(482, 456).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_arterial_cadence = SkillPointInit.registerSkill(branch,
					new SkillPoint(47, "skill_arterial_cadence", 650, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_hungry_pulse)
							.setSkillPointCost(4).setRequiredDegree(5).setTreePosition(278, 552).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_hematic_focus = SkillPointInit.registerSkill(branch,
					new SkillPoint(24, "skill_hematic_focus", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_projection)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(360, 464).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.skill_vascular_draw, SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vespers_refusal = SkillPointInit.registerSkill(branch,
					new SkillPoint(25, "skill_vespers_refusal", 700, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_hematic_focus)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(260, 448).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.memory_of_vesper.get())));
		// </skill-editor>
	}
}
