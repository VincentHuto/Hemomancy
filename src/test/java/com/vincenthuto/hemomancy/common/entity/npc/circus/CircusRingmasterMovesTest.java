package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.LentAct;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** NQ-15-4 A: the Ringmaster's move table, its floors, footprints and picking. */
class CircusRingmasterMovesTest {
	@Test
	void everyMoveWindsUpAtLeastTheKitFloor() {
		for (Move move : Move.values()) {
			if (move == Move.NONE) continue;
			assertTrue(move.timeline().windup() >= move.floor(), move + " winds up under its floor");
		}
		assertEquals(12, Move.STAFF_STRIKE.floor());
		assertEquals(12, Move.KNIFE_THROW.floor());
		for (Move area : Set.of(Move.SPOTLIGHT, Move.SLEIGHT, Move.CUE, Move.KNIFE_BOARD, Move.STRINGS_CUT))
			assertEquals(20, area.floor(), area + " is an area move");
	}

	@Test
	void theFiveMentalMovesAreOnTheTable() {
		Set<Move> mental = EnumSet.of(Move.SPOTLIGHT, Move.SLEIGHT, Move.CUE, Move.KNIFE_BOARD, Move.STRINGS_CUT);
		for (Move move : mental) assertNotNull(move.timeline(), move + " has a timeline");
	}

	@Test
	void theBoardHitsInsideTheOutlineAndADashBreaksIt() {
		Vec3 player = new Vec3(10.0D, 64.0D, 10.0D);
		FootprintFrame frame = FootprintFrame.simple(new Vec3(0.0D, 72.0D, 0.0D), player, player, 64.0D);
		BossFootprint board = CircusRingmasterMoves.footprint(Move.KNIFE_BOARD, null);
		assertTrue(board.hits(frame, player.add(0.5D, 0.0D, 0.0D), true, 0.0D));
		assertFalse(board.hits(frame, player.add(2.2D, 0.0D, 0.0D), true, 0.0D), "a dash out of the outline breaks it");
	}

	@Test
	void theStaffWedgeIsInFrontOfHim() {
		Vec3 him = new Vec3(0.0D, 64.0D, 0.0D);
		Vec3 ahead = new Vec3(0.0D, 64.0D, 2.0D);
		FootprintFrame frame = FootprintFrame.simple(him, ahead, ahead, 64.0D);
		BossFootprint staff = CircusRingmasterMoves.footprint(Move.STAFF_STRIKE, null);
		assertTrue(staff.hits(frame, ahead, true, 0.0D));
		assertFalse(staff.hits(frame, new Vec3(0.0D, 64.0D, -2.0D), true, 0.0D), "nothing behind him");
		assertFalse(staff.hits(frame, new Vec3(0.0D, 64.0D, 3.0D), true, 0.0D), "nothing past the staff's reach");
	}

	@Test
	void eachPerformerLendsItsOwnAct() {
		for (LentAct act : LentAct.values()) {
			assertNotNull(CircusRingmasterMoves.footprint(Move.STRINGS_CUT, act));
			assertEquals(act, LentAct.byName(act.name()));
		}
		assertNull(CircusRingmasterMoves.footprint(Move.STRINGS_CUT, null), "no lent act, no footprint");
		assertNull(CircusRingmasterMoves.footprint(Move.CUE, null), "the Cue strikes through the performers");
	}

	@Test
	void lookingAwayIsMoreThanSixtyDegreesOff() {
		Vec3 toHim = new Vec3(0.0D, 0.0D, 1.0D);
		assertFalse(CircusRingmasterMoves.looksAway(new Vec3(0.0D, 0.0D, 1.0D), toHim));
		assertFalse(CircusRingmasterMoves.looksAway(new Vec3(0.7D, 0.0D, 1.0D), toHim));
		assertTrue(CircusRingmasterMoves.looksAway(new Vec3(1.0D, 0.0D, 0.0D), toHim));
		assertTrue(CircusRingmasterMoves.looksAway(new Vec3(0.0D, 0.0D, -1.0D), toHim));
	}

	@Test
	void perchedHeThrowsAndEveryThirdBeatIsAMentalMove() {
		assertEquals(Move.KNIFE_THROW, CircusRingmasterMoves.rafters(0, 4));
		assertEquals(Move.KNIFE_THROW, CircusRingmasterMoves.rafters(1, 4));
		assertEquals(Move.SPOTLIGHT, CircusRingmasterMoves.rafters(2, 4));
		assertEquals(Move.KNIFE_BOARD, CircusRingmasterMoves.rafters(5, 4));
		assertEquals(Move.CUE, CircusRingmasterMoves.rafters(8, 4));
		assertEquals(Move.SPOTLIGHT, CircusRingmasterMoves.rafters(8, 0), "no Cue with nobody standing");
	}

	@Test
	void onTheCarouselHeCuesWhileTheTroupeStands() {
		assertEquals(Move.CUE, CircusRingmasterMoves.carousel(0, 3));
		assertTrue(CircusRingmasterMoves.carousel(0, 0) != Move.CUE);
	}

	@Test
	void inTheDescentTheBandChoosesAndAPillarGetsKnives() {
		for (long seed = 0; seed < 6; seed++) {
			assertEquals(Move.KNIFE_THROW, CircusRingmasterMoves.descent(BossBands.Band.HUG, false, 2, seed),
					"a player he cannot reach gets knives");
			Move far = CircusRingmasterMoves.descent(BossBands.Band.FAR, true, 2, seed);
			assertTrue(far == Move.KNIFE_THROW || far == Move.SPOTLIGHT, "far off: " + far);
			assertTrue(CircusRingmasterMoves.descent(BossBands.Band.NEAR, true, 0, seed) != Move.STRINGS_CUT,
					"no Strings Cut with nothing lent");
		}
		assertEquals(Move.STRINGS_CUT, CircusRingmasterMoves.descent(BossBands.Band.NEAR, true, 1, 0));
		assertEquals(Move.STAFF_STRIKE, CircusRingmasterMoves.descent(BossBands.Band.HUG, true, 0, 0));
	}
}
