package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.network.chat.Component;

final class SkillTooltipDetails {
	private SkillTooltipDetails() {}

	static Component unlockCost(int bloodCost, int skillPointCost) {
		return upgradeCost("screen.hemomancy.skill_tooltip_details.click_to_unlock_cost", bloodCost, skillPointCost);
	}

	static Component levelUpCost(int bloodCost, int skillPointCost) {
		return upgradeCost("screen.hemomancy.skill_tooltip_details.click_to_level_up_cost", bloodCost, skillPointCost);
	}

	private static Component upgradeCost(String key, int bloodCost, int skillPointCost) {
		return Component.translatable(key,
				bloodCost,
				skillPointCost).withStyle(style -> style.withColor(0xBB8833));
	}

	static Component skillPointBalance(int skillPoints) {
		return Component.translatable("screen.hemomancy.skill_tooltip_details.skill_points_sp",
				skillPoints).withStyle(style -> style.withColor(0xD6B85A));
	}
}
