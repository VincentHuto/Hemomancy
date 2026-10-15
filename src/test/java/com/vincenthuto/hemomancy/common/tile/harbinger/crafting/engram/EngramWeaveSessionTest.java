package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.Step;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveSession.Event;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;
import static org.junit.jupiter.api.Assertions.*;

class EngramWeaveSessionTest {
	private static final int[] LINE = starLines()[0];
	private static final int[] STOCK = {99, 99, 99, 99, 99, 99, 99, 99};

	private static EngramWeaveSession session() {
		EngramBoard board = EngramBoard.blank(false, 0);
		board.addFragment(0, 0, 0, false, "");
		board.addFragment(4, 0, 0, true, "blood_shot");
		EngramWeaveSession session = new EngramWeaveSession();
		session.start(board);
		return session;
	}

	@Test
	void pressingAFragmentTakesItsLaneAndHoldingThreadsIt() {
		EngramWeaveSession s = session();
		assertEquals(0, s.step(0, false, true, STOCK).fragment());
		assertEquals(0, s.active());
		assertTrue(s.step(LINE[1], false, false, STOCK).changed());
		assertTrue(s.step(LINE[2], false, false, STOCK).changed());
		assertArrayEquals(new int[]{0, LINE[1], LINE[2]}, s.board().lane(0));
	}

	@Test
	void nothingThreadsUntilALaneIsTaken() {
		EngramWeaveSession s = session();
		assertFalse(s.step(LINE[1], false, false, STOCK).changed());
		s.step(0, false, true, STOCK);
		s.release();
		assertFalse(s.step(LINE[1], false, false, STOCK).changed(), "letting go drops the lane");
	}

	@Test
	void aLaneReachingTheVesselLogsItsLanding() {
		EngramWeaveSession s = session();
		s.step(0, false, true, STOCK);
		for (int node : new int[]{LINE[1], LINE[2], VESSEL}) s.step(node, false, false, STOCK);
		EngramWeaveSession.Beat last = s.beats().getLast();
		assertEquals(Event.LAND, last.event());
		assertEquals(VESSEL, last.node());
		assertEquals(0, last.fragment());
	}

	@Test
	void aRefusalIsLoggedOncePerNode() {
		EngramWeaveSession s = session();
		s.board().setNode(LINE[1], EngramBoard.NodeKind.SCAR, -1);
		s.step(0, false, true, STOCK);
		assertEquals(Step.SCARRED, s.step(LINE[1], false, false, STOCK).step());
		s.step(LINE[1], false, false, STOCK);
		assertEquals(1, s.beats().stream().filter(b -> b.event() == Event.REFUSE).count());
	}

	@Test
	void aSneakingPressCutsOrClears() {
		EngramWeaveSession s = session();
		s.step(0, false, true, STOCK);
		s.step(LINE[3], false, false, STOCK);
		assertTrue(s.step(LINE[1], true, true, STOCK).changed());
		assertArrayEquals(new int[]{0, LINE[1]}, s.board().lane(0));
		assertEquals(Event.CUT, s.beats().getLast().event());
		s.step(0, true, true, STOCK);
		assertArrayEquals(new int[]{0}, s.board().lane(0));
		assertFalse(s.step(LINE[2], true, false, STOCK).changed(), "holding while sneaking does nothing");
	}

	@Test
	void aSnapClearsAFalseLaneAndReturnsItsSpans() {
		EngramWeaveSession s = session();
		s.step(4, false, true, STOCK);
		s.step(EngramSigilGeometry.neighbours(4)[0], false, false, STOCK);
		assertEquals(1, s.snap(1));
		assertArrayEquals(new int[]{4}, s.board().lane(1));
		assertEquals(Event.SNAP, s.beats().getLast().event());
		assertEquals(-1, s.active());
	}

	@Test
	void theLogKeepsOnlyRecentBeatsAndSurvivesSaving() {
		EngramWeaveSession s = session();
		s.step(0, false, true, STOCK);
		for (int i = 0; i < 5; i++) {
			s.step(LINE[1], false, false, STOCK);
			s.step(LINE[1], true, true, STOCK);
			s.step(0, true, true, STOCK);
		}
		assertTrue(s.beats().size() <= 6);
		EngramWeaveSession loaded = new EngramWeaveSession();
		loaded.load(s.save());
		assertEquals(s.beats(), loaded.beats());
		assertEquals(s.serial(), loaded.serial());
		assertEquals(s.board().save(), loaded.board().save());
	}
}
