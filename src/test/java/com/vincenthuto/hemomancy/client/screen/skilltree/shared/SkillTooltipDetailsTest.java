package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkillTooltipDetailsTest {
	@Test
	void tooltipShowsUpgradeCostThenCurrentSkillPointBalance() {
		assertEquals("Click to level up! Cost: 250 mL + 3 SP",
				EnglishText.render(SkillTooltipDetails.levelUpCost(250, 3)));
		assertEquals("Skill Points: 205 SP", EnglishText.render(SkillTooltipDetails.skillPointBalance(205)));
	}
}
