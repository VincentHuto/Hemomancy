package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperFlammeusBreathRulesTest {
	@Test
	void concentrationHasReadableWindupLockedDurationAndSharedCadence() {
		assertEquals(12, VesperFlammeusBreathRules.WINDUP_TICKS);
		assertEquals(60, VesperFlammeusBreathRules.DURATION_TICKS);
		assertFalse(VesperFlammeusBreathRules.isDamagePulse(11));
		assertTrue(VesperFlammeusBreathRules.isDamagePulse(12));
		assertTrue(VesperFlammeusBreathRules.isDamagePulse(16));
		assertFalse(VesperFlammeusBreathRules.isDamagePulse(17));
		assertTrue(VesperFlammeusBreathRules.isDamagePulse(48));
		assertFalse(VesperFlammeusBreathRules.isDamagePulse(52));
	}

	@Test
	void vesperPaletteContainsOnlyCrimsonAndBlack() {
		assertEquals(2, VesperFlammeusBreathRules.palette().size());
		assertTrue(VesperFlammeusBreathRules.palette().contains(0xDC000C));
		assertTrue(VesperFlammeusBreathRules.palette().contains(0x080003));
		assertFalse(VesperFlammeusBreathRules.palette().contains(0xFF6508));
	}

	@Test
	void vesperNeverPaysPlayerBlood() {
		assertEquals(0.0D, VesperFlammeusBreathRules.bloodCostPerTick());
	}
}
