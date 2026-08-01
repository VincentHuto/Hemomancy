package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkillTooltipDetailsTest {
	@Test
	void tooltipShowsUpgradeCostThenCurrentSkillPointBalance() {
		assertEquals("Click to level up! Cost: 250 mL + 3 SP",
				SkillTooltipDetails.upgradeCost("level up", 250, 3).getString());
		assertEquals("Skill Points: 205 SP", SkillTooltipDetails.skillPointBalance(205).getString());
	}
}
