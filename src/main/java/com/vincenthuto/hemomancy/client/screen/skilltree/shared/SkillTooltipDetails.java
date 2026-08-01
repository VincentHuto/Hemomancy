package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.network.chat.Component;

final class SkillTooltipDetails {
	private SkillTooltipDetails() {}

	static Component upgradeCost(String action, int bloodCost, int skillPointCost) {
		return Component.literal("Click to " + action + "! Cost: " + bloodCost + " mL + "
				+ skillPointCost + " SP").withStyle(style -> style.withColor(0xBB8833));
	}

	static Component skillPointBalance(int skillPoints) {
		return Component.literal("Skill Points: " + skillPoints + " SP")
				.withStyle(style -> style.withColor(0xD6B85A));
	}
}
