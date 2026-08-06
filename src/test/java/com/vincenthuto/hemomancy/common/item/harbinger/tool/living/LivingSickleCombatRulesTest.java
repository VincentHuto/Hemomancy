package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LivingSickleCombatRulesTest {
	@Test
	void reapingBonusOnlyAppliesAtThirtyPercentHealth() throws Exception {
		Class<?> rules = Class.forName(
				"com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleCombatRules");
		Method bonus = rules.getMethod("executionBonus", float.class, float.class);
		assertEquals(0.0F, (float) bonus.invoke(null, 6.1F, 20.0F), 0.0001F);
		assertEquals(3.0F, (float) bonus.invoke(null, 6.0F, 20.0F), 0.0001F);
	}

	@Test
	void sweepDealsFortyPercentOfNormalAttackDamage() throws Exception {
		Class<?> rules = Class.forName(
				"com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleCombatRules");
		Method sweep = rules.getMethod("sweepDamage", float.class);
		assertEquals(2.8F, (float) sweep.invoke(null, 7.0F), 0.0001F);
	}
}
