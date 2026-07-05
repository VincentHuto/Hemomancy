package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class ManipulationSlotLedger {
	private ManipulationSlotLedger() {
	}

	public static ManipulationSlotSnapshot collect(Player player) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		int degreeBonus = Math.max(0, degree / 2);
		int skillBonus = skillBonus(player);
		List<ManipulationSlotContribution> contributions = new ArrayList<>();
		contributions.add(ManipulationSlotContribution.amount("base", "Base Slots",
				Category.BASE, ManipSlotHelper.BASE_SLOTS));
		if (degreeBonus > 0) {
			contributions.add(ManipulationSlotContribution.amount("degree", "Initiatory Degree",
					Category.DEGREE, degreeBonus));
		}
		if (skillBonus > 0) {
			contributions.add(ManipulationSlotContribution.amount("skill_manip_slots", "Manipulation Slots",
					Category.SKILL, skillBonus));
		}
		return ManipulationSlotRules.snapshot(contributions, ManipSlotHelper.MAX_SLOTS);
	}

	public static int getMaxSlots(Player player) {
		return collect(player).appliedSlots();
	}

	public static int getExcessSkillLevels(Player player) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		int degreeBonus = Math.max(0, degree / 2);
		return ManipulationSlotRules.excessSkillLevels(ManipSlotHelper.BASE_SLOTS, degreeBonus,
				skillBonus(player), ManipSlotHelper.MAX_SLOTS);
	}

	private static int skillBonus(Player player) {
		if (SkillPointInit.skill_manip_slots == null) {
			return 0;
		}
		return Math.max(0, SkillPointHelper.getSkillLevel(player, SkillPointInit.skill_manip_slots));
	}
}
