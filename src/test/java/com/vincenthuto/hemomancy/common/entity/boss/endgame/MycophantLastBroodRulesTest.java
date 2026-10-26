package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.Outcome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantLastBroodRulesTest {
	@Test
	void triggersOnceBelowAQuarterInPhaseTwo() {
		assertTrue(MycophantLastBroodRules.triggers(2, 179.0F, 720.0F, false));
		assertFalse(MycophantLastBroodRules.triggers(1, 179.0F, 720.0F, false));
		assertFalse(MycophantLastBroodRules.triggers(2, 179.0F, 720.0F, true));
		assertFalse(MycophantLastBroodRules.triggers(2, 200.0F, 720.0F, false));
		assertFalse(MycophantLastBroodRules.triggers(2, 180.0F, 720.0F, false));
	}

	@Test
	void theSetPieceHalvesDamageAndHoldsTheFloor() {
		assertEquals(10.0F, MycophantLastBroodRules.incoming(20.0F, 100.0F, 720.0F), 1.0E-5F);
		assertEquals(4.0F, MycophantLastBroodRules.incoming(100.0F, 40.0F, 720.0F), 1.0E-5F);
		assertEquals(0.0F, MycophantLastBroodRules.incoming(10.0F, 36.0F, 720.0F), 1.0E-5F);
		assertEquals(0.0F, MycophantLastBroodRules.incoming(10.0F, 30.0F, 720.0F), 1.0E-5F);
		for (float health = 36.0F; health < 180.0F; health += 7.3F) {
			for (float amount = 0.0F; amount < 300.0F; amount += 13.1F) {
				assertTrue(health - MycophantLastBroodRules.incoming(amount, health, 720.0F) >= 36.0F - 1.0E-4F);
			}
		}
	}

	@Test
	void outcomes() {
		assertEquals(Outcome.CUT_FREE, MycophantLastBroodRules.outcome(40, 0, false));
		assertEquals(Outcome.CUT_FREE, MycophantLastBroodRules.outcome(160, 0, false));
		assertEquals(Outcome.COCOONED, MycophantLastBroodRules.outcome(40, 2, true));
		assertEquals(Outcome.SPENT, MycophantLastBroodRules.outcome(160, 1, false));
		assertEquals(Outcome.SPENT, MycophantLastBroodRules.outcome(200, 4, false));
		assertEquals(Outcome.PULLING, MycophantLastBroodRules.outcome(159, 4, false));
		assertEquals(Outcome.PULLING, MycophantLastBroodRules.outcome(0, 3, false));
	}

	@Test
	void anchorsRingHerAQuarterTurnApart() {
		for (long seed = 0; seed < 200; seed++) {
			float yaw = (seed * 37) % 360 - 180.0F;
			double[][] anchors = MycophantLastBroodRules.anchors(20.0, -8.0, yaw, seed);
			assertEquals(MycophantLastBroodRules.TENDRILS, anchors.length);
			double[] bearings = new double[anchors.length];
			for (int i = 0; i < anchors.length; i++) {
				double dx = anchors[i][0] - 20.0;
				double dz = anchors[i][1] + 8.0;
				double r = Math.hypot(dx, dz);
				assertTrue(r >= 6.0 - 1.0E-9 && r <= 9.0 + 1.0E-9, "seed " + seed + " radius " + r);
				bearings[i] = Math.toDegrees(Math.atan2(dz, dx));
			}
			for (int i = 0; i < bearings.length; i++) {
				for (int j = i + 1; j < bearings.length; j++) {
					double gap = Math.abs(((bearings[i] - bearings[j]) % 360.0 + 540.0) % 360.0 - 180.0);
					assertTrue(gap >= 60.0, "seed " + seed + " anchors " + i + "," + j + " only " + gap + " apart");
				}
			}
		}
	}
}
