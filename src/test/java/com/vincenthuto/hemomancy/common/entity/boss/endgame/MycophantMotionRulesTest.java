package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantMotionRulesTest {
	@Test
	void dragPhaseCyclesEveryThirtySixTicks() {
		assertEquals(0.0F, MycophantMotionRules.dragPhase(0.0F), 1.0E-5F);
		assertEquals(0.0F, MycophantMotionRules.dragPhase(36.0F), 1.0E-5F);
		assertEquals(0.5F, MycophantMotionRules.dragPhase(18.0F), 1.0E-5F);
	}

	@Test
	void haulSpeedIsNeverNegativeAndPhaseTwoIsFaster() {
		float peakOne = 0.0F;
		float peakTwo = 0.0F;
		for (int i = 0; i < 360; i++) {
			float t = i / 10.0F;
			float one = MycophantMotionRules.haulSpeed(t, 1);
			float two = MycophantMotionRules.haulSpeed(t, 2);
			assertTrue(one >= 0.0F && two >= 0.0F);
			peakOne = Math.max(peakOne, one);
			peakTwo = Math.max(peakTwo, two);
		}
		assertTrue(peakOne > 0.0F);
		assertEquals(peakOne * 1.25F, peakTwo, 1.0E-4F);
	}

	@Test
	void diagonalPairsAlternate() {
		// mid-swing for pair 0 is mid-stance for pair 1, and vice versa
		assertNotEquals(MycophantMotionRules.pairPlanted(0, 9.0F), MycophantMotionRules.pairPlanted(1, 9.0F));
		assertNotEquals(MycophantMotionRules.pairPlanted(0, 27.0F), MycophantMotionRules.pairPlanted(1, 27.0F));
	}
}
