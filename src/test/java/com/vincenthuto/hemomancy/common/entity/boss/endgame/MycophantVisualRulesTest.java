package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantVisualRulesTest {
	@Test
	void tetherSagsWithDistanceButNeverDrapesPastABlockAndAHalf() {
		assertEquals(0.0F, MycophantVisualRules.tetherSag(0.0F), 1.0E-6F);
		assertTrue(MycophantVisualRules.tetherSag(6.0F) > MycophantVisualRules.tetherSag(3.0F));
		assertTrue(MycophantVisualRules.tetherSag(10.0F) <= 1.5F);
		assertTrue(MycophantVisualRules.tetherSag(40.0F) <= 1.5F);
	}

	@Test
	void pulseIsAHeartbeatBetweenZeroAndOne() {
		float max = 0.0F;
		for (int t = 0; t < 200; t++) {
			float pulse = MycophantVisualRules.tetherPulse(t);
			assertTrue(pulse >= 0.0F && pulse <= 1.0F);
			max = Math.max(max, pulse);
		}
		assertTrue(max > 0.8F);
	}

	@Test
	void claimWrapFollowsTheMeterClamped() {
		assertEquals(0.0F, MycophantVisualRules.claimWrapProgress(-0.5F), 1.0E-6F);
		assertEquals(0.5F, MycophantVisualRules.claimWrapProgress(0.5F), 1.0E-6F);
		assertEquals(1.0F, MycophantVisualRules.claimWrapProgress(2.0F), 1.0E-6F);
	}

	@Test
	void theSiphonMouthIsOnHerRight() {
		double[] mouth = MycophantVisualRules.siphonMouth();
		org.junit.jupiter.api.Assertions.assertArrayEquals(new double[] { 2.2D, 3.4D, 2.0D }, mouth, 1.0E-9);
	}
}
