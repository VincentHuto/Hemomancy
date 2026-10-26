package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantWingMovesTest {
	@Test
	void theVariantPicksTheSweepSideAndVariantZeroTosses() {
		assertEquals(1, MycophantWingMoves.gazeDirection(1, false));
		assertEquals(-1, MycophantWingMoves.gazeDirection(2, true));
		assertEquals(1, MycophantWingMoves.gazeDirection(0, true));
		assertEquals(-1, MycophantWingMoves.gazeDirection(0, false));
		int heads = 0;
		for (long start = 0; start < 400; start++) {
			assertEquals(MycophantWingMoves.coin(start, 7), MycophantWingMoves.coin(start, 7), "the same move tosses the same way");
			if (MycophantWingMoves.coin(start * 131 + 17, 42)) heads++;
		}
		assertTrue(heads > 120 && heads < 280, "both sides come up: " + heads);
	}

	@Test
	void theSweepStartIsRecoveredFromLastTicksYaw() {
		for (int direction : new int[] { 1, -1 }) {
			for (float start = -180.0F; start < 180.0F; start += 37.0F) {
				for (int k = 1; k < MycophantStrikeRules.GAZE_SWEEP_TICKS; k++) {
					float previous = MycophantStrikeRules.gazeYaw(start, direction, k - 1);
					float recovered = MycophantWingMoves.gazeStart(previous, direction, k - 1);
					assertEquals(start, recovered, 1.0E-3F);
				}
			}
		}
	}

	@Test
	void theBeamBurnsEveryFiveTicksThroughTheSweepOnly() {
		int burns = 0;
		for (int active = -5; active < 60; active++) if (MycophantWingMoves.gazeStrikes(active)) burns++;
		assertEquals(MycophantStrikeRules.GAZE_SWEEP_TICKS / MycophantWingMoves.GAZE_EVERY, burns);
		assertTrue(MycophantWingMoves.gazeStrikes(0));
		assertFalse(MycophantWingMoves.gazeStrikes(3));
		assertFalse(MycophantWingMoves.gazeStrikes(MycophantStrikeRules.GAZE_SWEEP_TICKS));
		assertEquals(MycophantStrikeRules.GAZE_SWEEP_TICKS, MycophantControllerRules.timeline(MycophantAttack.GAZE, 2).active(),
				"the sweep fills the Gaze's active stage");
	}

	@Test
	void theNetsCatchIsReadJustAfterItLands() {
		assertFalse(MycophantWingMoves.netCatchTick(MycophantWingMoves.NET_LAND_DELAY - 1));
		assertTrue(MycophantWingMoves.netCatchTick(MycophantWingMoves.NET_LAND_DELAY));
		assertFalse(MycophantWingMoves.netCatchTick(MycophantWingMoves.NET_LAND_DELAY + MycophantWingMoves.NET_CATCH_WINDOW + 1));
		var timeline = MycophantControllerRules.timeline(MycophantAttack.LATTICE_NET, 2);
		assertTrue(MycophantWingMoves.NET_LAND_DELAY + MycophantWingMoves.NET_CATCH_WINDOW < timeline.active() + timeline.recovery(),
				"the move is still running when the net lands");
	}

	@Test
	void aSecondLanceLineBelowAThirdOfHealth() {
		assertFalse(MycophantWingMoves.secondLine(300.0F, 720.0F));
		assertTrue(MycophantWingMoves.secondLine(250.0F, 720.0F));
	}
}
