package com.vincenthuto.hemomancy.common.entity.boss.court;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class KindledChampionRulesTest {
	@Test
	void regrowthByCrownStageIsTwoThenThreeThenThree() {
		assertEquals(2.0F, KindledChampionRules.regrowthPerSecond(0));
		assertEquals(3.0F, KindledChampionRules.regrowthPerSecond(1));
		assertEquals(3.0F, KindledChampionRules.regrowthPerSecond(2));
		assertEquals(3.0F, KindledChampionRules.regrowthPerSecond(5));
	}
}
