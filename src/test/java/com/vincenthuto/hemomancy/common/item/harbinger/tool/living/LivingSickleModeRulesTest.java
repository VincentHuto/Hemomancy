package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LivingSickleModeRulesTest {
	@Test
	void sickleDefaultsToShortReapAndCyclesBetweenTwoModes() {
		assertEquals(LivingSickleMode.SHORT_REAP, LivingSickleMode.defaultMode());
		assertEquals(LivingSickleMode.BLOOD_HOOK, LivingSickleMode.SHORT_REAP.next());
		assertEquals(LivingSickleMode.SHORT_REAP, LivingSickleMode.BLOOD_HOOK.next());
	}

	@Test
	void modesUseDistinctAttackSpeeds() {
		assertEquals(-1.8F, LivingSickleCombatRules.attackSpeed(LivingSickleMode.SHORT_REAP), 0.0001F);
		assertEquals(-2.8F, LivingSickleCombatRules.attackSpeed(LivingSickleMode.BLOOD_HOOK), 0.0001F);
	}

	@Test
	void spinUsesAReadableAreaAndFourSecondCooldown() {
		assertEquals(4.0D, LivingSickleCombatRules.SPIN_RADIUS, 0.0001D);
		assertEquals(4.9F, LivingSickleCombatRules.spinDamage(7.0F), 0.0001F);
		assertEquals(80, LivingSickleCombatRules.SPIN_COOLDOWN_TICKS);
	}

	@Test
	void hookHasLongRangeDamageAndResistanceAwarePull() {
		assertEquals(18.0D, LivingSickleCombatRules.HOOK_RANGE, 0.0001D);
		assertEquals(7.0F, LivingSickleCombatRules.hookDamage(7.0F), 0.0001F);
		assertEquals(1.2D, LivingSickleCombatRules.pullStrength(0.0D), 0.0001D);
		assertEquals(0.6D, LivingSickleCombatRules.pullStrength(0.5D), 0.0001D);
		assertEquals(0.0D, LivingSickleCombatRules.pullStrength(1.0D), 0.0001D);
		assertEquals(20, LivingSickleCombatRules.HOOK_COOLDOWN_TICKS);
	}
}
