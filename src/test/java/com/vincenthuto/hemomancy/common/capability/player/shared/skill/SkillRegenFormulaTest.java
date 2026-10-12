package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillRegenFormulaTest {
	@Test
	void sanguineSurgeIsAQuarterMillilitrePerTickPerLevel() {
		assertEquals(0.0D, SkillPointHelper.sanguineSurgeRegenPerTick(0), 1.0E-9);
		assertEquals(0.25D, SkillPointHelper.sanguineSurgeRegenPerTick(1), 1.0E-9);
		assertEquals(0.75D, SkillPointHelper.sanguineSurgeRegenPerTick(3), 1.0E-9);
		// 5 / 10 / 15 mL per second
		assertEquals(15.0D, SkillPointHelper.sanguineSurgeRegenPerTick(3) * 20, 1.0E-9);
	}

	@Test
	void lastWindIsHalfAMillilitrePerTickPerLevel() {
		assertEquals(0.0D, SkillPointHelper.lastWindRegenPerTick(0), 1.0E-9);
		assertEquals(0.5D, SkillPointHelper.lastWindRegenPerTick(1), 1.0E-9);
		assertEquals(1.5D, SkillPointHelper.lastWindRegenPerTick(3), 1.0E-9);
		// Max 30 mL per second at level 3
		assertEquals(30.0D, SkillPointHelper.lastWindRegenPerTick(3) * 20, 1.0E-9);
		assertEquals(0.10D, SkillPointHelper.getLastWindThreshold(), 1.0E-9);
	}
}
