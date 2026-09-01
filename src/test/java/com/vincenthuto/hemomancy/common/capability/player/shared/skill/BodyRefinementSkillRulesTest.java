package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyRefinementSkillRulesTest {
	@Test
	void nervesOfSteelStabilizesImpulseAndChargedCasting() {
		assertEquals(0.4D, BodyRefinementSkillRules.knockbackMultiplier(3), 0.000001D);
		assertEquals(20, BodyRefinementSkillRules.retainedChargeTicks(40, 2));
	}

	@Test
	void ironHandedTradesCadenceForImpulse() {
		assertEquals(1.5D, BodyRefinementSkillRules.meleeKnockbackBonus(3), 0.000001D);
		assertEquals(-0.225D, BodyRefinementSkillRules.attackSpeedModifier(3), 0.000001D);
	}

	@Test
	void brightEyedImprovesResistanceAndRevelation() {
		assertEquals(40, BodyRefinementSkillRules.visionDebuffTicks(100, 3));
		assertEquals(288, BodyRefinementSkillRules.revealTicks(180, 3));
		assertEquals(1.3D, BodyRefinementSkillRules.perceptionRangeMultiplier(3), 0.000001D);
	}

	@Test
	void lightFootedIsContextual() {
		assertFalse(BodyRefinementSkillRules.strongLight(11));
		assertTrue(BodyRefinementSkillRules.strongLight(12));
		assertEquals(0.15D, BodyRefinementSkillRules.lightMovementModifier(3), 0.000001D);
		assertEquals(0.45D, BodyRefinementSkillRules.lightStepHeightBonus(3), 0.000001D);
	}
}
