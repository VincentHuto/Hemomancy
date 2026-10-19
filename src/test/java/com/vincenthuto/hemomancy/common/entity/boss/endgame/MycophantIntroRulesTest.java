package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules.Beat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantIntroRulesTest {
	@Test
	void fullWakingBeats() {
		assertEquals(75, MycophantIntroRules.tickOf(Beat.FIBERS_SNAP, false));
		assertEquals(85, MycophantIntroRules.tickOf(Beat.NOTICE, false));
		assertEquals(95, MycophantIntroRules.tickOf(Beat.TURN, false));
		assertEquals(120, MycophantIntroRules.tickOf(Beat.DROP, false));
		assertEquals(130, MycophantIntroRules.tickOf(Beat.CONTROL, false));
		assertEquals(150, MycophantIntroRules.tickOf(Beat.ROAR, false));
		assertEquals(190, MycophantIntroRules.tickOf(Beat.RUSH, false));
		assertEquals(Beat.BOUND, MycophantIntroRules.beatAt(0, false));
		assertEquals(Beat.NOTICE, MycophantIntroRules.beatAt(90, false));
		assertEquals(Beat.DONE, MycophantIntroRules.beatAt(500, false));
		assertTrue(MycophantIntroRules.bound(129, false));
		assertFalse(MycophantIntroRules.bound(130, false));
		assertTrue(MycophantIntroRules.invulnerable(149, false));
		assertFalse(MycophantIntroRules.invulnerable(150, false));
	}

	@Test
	void rematchSkipsTheWaking() {
		assertEquals(20, MycophantIntroRules.tickOf(Beat.ROAR, true));
		assertEquals(60, MycophantIntroRules.tickOf(Beat.RUSH, true));
		assertFalse(MycophantIntroRules.bound(0, true));
		assertEquals(60, MycophantIntroRules.length(true));
		assertEquals(190, MycophantIntroRules.length(false));
	}

	@Test
	void musicWaitsForTheRoar() {
		assertFalse(MycophantIntroRules.musicPlays(149, false));
		assertTrue(MycophantIntroRules.musicPlays(150, false));
		assertFalse(MycophantIntroRules.musicPlays(19, true));
		assertTrue(MycophantIntroRules.musicPlays(20, true));
	}

	@Test
	void sheFacesThePlayerBeforeTheFunglingFalls() {
		for (boolean rematch : new boolean[] { false, true }) {
			assertTrue(MycophantIntroRules.turnRateDegrees(rematch) * MycophantIntroRules.turnTicks(rematch) >= 180.0F);
			assertTrue(MycophantIntroRules.tickOf(Beat.TURN, rematch) + MycophantIntroRules.turnTicks(rematch)
					<= MycophantIntroRules.tickOf(Beat.ROAR, rematch));
		}
		assertTrue(MycophantIntroRules.tickOf(Beat.TURN, false) + MycophantIntroRules.turnTicks(false)
				<= MycophantIntroRules.tickOf(Beat.DROP, false));
		assertTrue(MycophantIntroRules.tickOf(Beat.DROP, false) + MycophantIntroRules.dropFallTicks()
				< MycophantIntroRules.tickOf(Beat.CONTROL, false));
	}
}
