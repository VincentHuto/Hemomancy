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
							.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(360, 480).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vascular_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(22, "skill_vascular_draw", 350, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(310, 496).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_dragging_siphon = SkillPointInit.registerSkill(branch,
					new SkillPoint(41, "skill_dragging_siphon", 300, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(366, 608).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_mobile_conduit = SkillPointInit.registerSkill(branch,
					new SkillPoint(42, "skill_mobile_conduit", 375, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_dragging_siphon)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(430, 688).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_blood_tolerance = SkillPointInit.registerSkill(branch,
					new SkillPoint(43, "skill_blood_tolerance", 425, 5, EnumSkillStates.LOCKED, SkillPointInit.skill_dragging_siphon)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(304, 608).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
			SkillPointInit.skill_crimson_projection = SkillPointInit.registerSkill(branch,
					new SkillPoint(23, "skill_crimson_projection", 400, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_living_conduit)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(276, 432).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_projection.get())));
			SkillPointInit.skill_quickened_draw = SkillPointInit.registerSkill(branch,
					new SkillPoint(45, "skill_quickened_draw", 350, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(2).setRequiredDegree(3).setTreePosition(274, 560).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_hungry_pulse = SkillPointInit.registerSkill(branch,
					new SkillPoint(46, "skill_hungry_pulse", 475, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_quickened_draw)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(248, 632).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_weapons_master = SkillPointInit.registerSkill(branch,
					new SkillPoint(39, "skill_weapons_master", 450, 4, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_projection)
							.setSkillPointCost(2).setRequiredDegree(4).setTreePosition(208, 384).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.skill_vascular_draw)
							.setIconItem(() -> new ItemStack(ItemInit.living_blade.get())));
			SkillPointInit.skill_unbound_siphon = SkillPointInit.registerSkill(branch,
					new SkillPoint(44, "skill_unbound_siphon", 600, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_mobile_conduit)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(354, 584).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_arterial_cadence = SkillPointInit.registerSkill(branch,
					new SkillPoint(47, "skill_arterial_cadence", 650, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_hungry_pulse)
							.setSkillPointCost(4).setRequiredDegree(6).setTreePosition(310, 520).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_hematic_focus = SkillPointInit.registerSkill(branch,
					new SkillPoint(24, "skill_hematic_focus", 500, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_crimson_projection)
							.setSkillPointCost(3).setRequiredDegree(5).setTreePosition(360, 464).setBranch("living_staff").setBranchColor(0xFFD9AD28).addParents(SkillPointInit.skill_vascular_draw, SkillPointInit.deep_base_skill)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_vespers_refusal = SkillPointInit.registerSkill(branch,
					new SkillPoint(25, "skill_vespers_refusal", 700, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_hematic_focus)
							.setSkillPointCost(4).setRequiredDegree(7).setTreePosition(276, 384).setBranch("living_staff").setBranchColor(0xFFD9AD28)
							.setIconItem(() -> new ItemStack(ItemInit.memory_of_vesper.get())));
			SkillPointInit.skill_persistent_arsenal = SkillPointInit.registerSkill(branch,
					new SkillPoint(50, "skill_persistent_arsenal", 350, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_weapons_master)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(286, 382).setBranch("living_staff").setBranchColor(0xFFD9AD28).setToggleable(true)
							.setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
			SkillPointInit.skill_distributed_siphon = SkillPointInit.registerSkill(branch,
					new SkillPoint(51, "skill_distributed_siphon", 400, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(210, 436).setBranch("living_staff").setBranchColor(0xFFD9AD28).setToggleable(true)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_selective_hunger = SkillPointInit.registerSkill(branch,
					new SkillPoint(52, "skill_selective_hunger", 325, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_draw)
							.setSkillPointCost(3).setRequiredDegree(3).setTreePosition(254, 506).setBranch("living_staff").setBranchColor(0xFFD9AD28).setToggleable(true)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_vascular_mercy = SkillPointInit.registerSkill(branch,
					new SkillPoint(53, "skill_vascular_mercy", 500, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_distributed_siphon)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(214, 506).setBranch("living_staff").setBranchColor(0xFFD9AD28).setToggleable(true)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
			SkillPointInit.skill_guarded_feeding = SkillPointInit.registerSkill(branch,
					new SkillPoint(54, "skill_guarded_feeding", 525, 1, EnumSkillStates.LOCKED, SkillPointInit.skill_vascular_mercy)
							.setSkillPointCost(3).setRequiredDegree(4).setTreePosition(232, 602).setBranch("living_staff").setBranchColor(0xFFD9AD28).setToggleable(true)
							.setIconItem(() -> new ItemStack(ItemInit.blood_absorption.get())));
		// </skill-editor>
	}
}
