package com.vincenthuto.hemomancy.common.summon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighStrungRulesTest {
	@Test
	void overclockHasBenefitsAndMatchingCosts() {
		assertEquals(1.3D, PuppeteerSummonRules.highStrungSpeedMultiplier(3), 0.000001D);
		assertEquals(14, PuppeteerSummonRules.highStrungAttackInterval(3));
		assertEquals(52.0D, PuppeteerSummonRules.highStrungCommandRange(40.0D, 3), 0.000001D);
		assertEquals(28, PuppeteerSummonRules.highStrungThreadUpkeep(16, 3));
		assertEquals(30.0D, PuppeteerSummonRules.highStrungBloodUpkeep(3), 0.000001D);
		assertEquals(4.5F, PuppeteerSummonRules.highStrungRecoilDamage(3), 0.000001F);
	}

	@Test
	void inactiveOverclockLeavesPuppetsAlone() {
		assertEquals(1.0D, PuppeteerSummonRules.highStrungSpeedMultiplier(0), 0.000001D);
		assertEquals(20, PuppeteerSummonRules.highStrungAttackInterval(0));
		assertEquals(16, PuppeteerSummonRules.highStrungThreadUpkeep(16, 0));
		assertEquals(0.0D, PuppeteerSummonRules.highStrungBloodUpkeep(0), 0.000001D);
	}
}
