package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantRangedMovesTest {
	@Test
	void mortarVolleyGrowsWithThePhaseAndAtLowHealth() {
		assertEquals(3, MycophantRangedMoves.mortarEggs(1, 600.0F, 720.0F));
		assertEquals(4, MycophantRangedMoves.mortarEggs(2, 300.0F, 720.0F));
		assertEquals(5, MycophantRangedMoves.mortarEggs(2, 250.0F, 720.0F));
	}

	@Test
	void mortarEggsLeaveAcrossTheActiveStage() {
		int active = MycophantControllerRules.timeline(MycophantAttack.SPORE_MORTAR, 2).active();
		for (int count = 3; count <= 5; count++) {
			assertEquals(0, MycophantRangedMoves.mortarLaunch(0, count, active));
			int last = MycophantRangedMoves.mortarLaunch(count - 1, count, active);
			assertTrue(last > 0 && last < active, "the last egg leaves inside the active stage");
			for (int i = 1; i < count; i++)
				assertTrue(MycophantRangedMoves.mortarLaunch(i, count, active) > MycophantRangedMoves.mortarLaunch(i - 1, count, active));
		}
	}
}
