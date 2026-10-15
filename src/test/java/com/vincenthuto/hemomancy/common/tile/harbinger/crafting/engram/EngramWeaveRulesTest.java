package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard.NodeKind;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.Step;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;
import static org.junit.jupiter.api.Assertions.*;

class EngramWeaveRulesTest {
	private static final int ANIMUS = 0, FLAMMEUS = 1, LUX = 3;
	/** Star line 0 runs tip 0, notch, inner, inner, notch, tip 3. */
	private static final int[] LINE = starLines()[0];

	private static int[] plenty() {
		int[] stock = new int[8];
		java.util.Arrays.fill(stock, 99);
		return stock;
	}

	private static int[] counts(int tendency, int n) {
		int[] counts = new int[8];
		counts[tendency] = n;
		return counts;
	}

	@Test
	void aLaneExtendsAlongAnEdgeAndCommitsASpan() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		assertEquals(Step.OK, EngramWeaveRules.extend(board, a, LINE[1], plenty()));
		assertArrayEquals(new int[]{0, LINE[1]}, board.lane(a));
		assertEquals(1, EngramWeaveRules.committed(board, ANIMUS));
	}

	@Test
	void aLaneCannotJumpToANodeItDoesNotTouch() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		assertEquals(Step.NOT_ADJACENT, EngramWeaveRules.extend(board, a, LINE[2], plenty()));
		assertArrayEquals(new int[]{0}, board.lane(a));
	}

	@Test
	void anotherColoursNodeIsOccupied() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		board.addFragment(LINE[1], FLAMMEUS, FLAMMEUS, false, "");
		assertEquals(Step.OCCUPIED, EngramWeaveRules.extend(board, a, LINE[1], plenty()));
	}

	@Test
	void aLaneOfTheSameColourPliesIntoAnotherAndIsCarriedByIt() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		int b = board.addFragment(LINE[1], ANIMUS, ANIMUS, false, "");
		assertEquals(Step.OK, EngramWeaveRules.extend(board, a, LINE[1], plenty()));
		assertEquals(Step.LANDED, EngramWeaveRules.extend(board, a, LINE[2], plenty()), "a plied lane ends at the join");
		assertFalse(EngramWeaveRules.connected(board, a));
		EngramWeaveRules.extend(board, b, LINE[2], plenty());
		assertEquals(Step.OK, EngramWeaveRules.extend(board, b, VESSEL, plenty()));
		assertTrue(EngramWeaveRules.connected(board, b));
		assertTrue(EngramWeaveRules.connected(board, a), "the plied lane rides its carrier home");
		assertEquals(1, EngramWeaveRules.laneSpans(board, a), "a plied lane pays only its own segment");
		assertEquals(3, EngramWeaveRules.committed(board, ANIMUS));
	}

	@Test
	void aCarrierMayPassThroughWhereALaneOfItsColourWaits() {
		int notch = LINE[1];
		int[] cross = null;
		for (int[] line : starLines()) if (line != starLines()[0] && EngramWeaveRules.contains(line, notch) && line[0] != LINE[0]) cross = line;
		int at = 0;
		while (cross[at] != notch) at++;
		int before = cross[at == 1 ? 0 : at + 1], after = cross[at == 1 ? 2 : at - 1];
		EngramBoard board = EngramBoard.blank(false, 0);
		int waiting = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		int carrier = board.addFragment(before, ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.extend(board, waiting, notch, plenty());
		assertFalse(EngramWeaveRules.landed(board, waiting), "nothing carries it yet");
		assertEquals(Step.OK, EngramWeaveRules.extend(board, carrier, notch, plenty()));
		assertEquals(Step.OK, EngramWeaveRules.extend(board, carrier, after, plenty()), "the carrier threads on through");
		assertTrue(EngramWeaveRules.landed(board, waiting), "the waiting lane is now plied into it");
	}

	@Test
	void aLaneCannotDoubleBackOntoItself() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.extend(board, a, 1, plenty());
		EngramWeaveRules.extend(board, a, 2, plenty());
		assertEquals(Step.OCCUPIED, EngramWeaveRules.extend(board, a, 1, plenty()));
	}

	@Test
	void scarsBlockEveryLane() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		board.setNode(LINE[1], NodeKind.SCAR, -1);
		assertEquals(Step.SCARRED, EngramWeaveRules.extend(board, a, LINE[1], plenty()));
	}

	@Test
	void membranesPassOnlyTheirOwnColour() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		int f = board.addFragment(LINE[5], FLAMMEUS, FLAMMEUS, false, "");
		board.setNode(LINE[1], NodeKind.MEMBRANE, FLAMMEUS);
		board.setNode(LINE[4], NodeKind.MEMBRANE, FLAMMEUS);
		assertEquals(Step.MEMBRANE, EngramWeaveRules.extend(board, a, LINE[1], plenty()));
		assertEquals(Step.OK, EngramWeaveRules.extend(board, f, LINE[4], plenty()));
	}

	@Test
	void keyedFacesTakeOnlyTheirTendency() {
		int face = face(LINE[2]);
		EngramBoard wrong = EngramBoard.blank(true, Math.floorMod(face + 1, 8));
		int a = wrong.addFragment(0, ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.extend(wrong, a, LINE[1], plenty());
		EngramWeaveRules.extend(wrong, a, LINE[2], plenty());
		assertEquals(Step.WRONG_FACE, EngramWeaveRules.extend(wrong, a, VESSEL, plenty()));

		EngramBoard right = EngramBoard.blank(true, face);
		int b = right.addFragment(0, ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.extend(right, b, LINE[1], plenty());
		EngramWeaveRules.extend(right, b, LINE[2], plenty());
		assertEquals(Step.OK, EngramWeaveRules.extend(right, b, VESSEL, plenty()));
		assertEquals(ANIMUS, EngramWeaveRules.faceTendency(right, face));
		assertEquals(FLAMMEUS, EngramWeaveRules.faceTendency(right, face + 1), "the wheel runs clockwise in enum order");
	}

	@Test
	void unkeyedFacesTakeAnyColour() {
		EngramBoard board = EngramBoard.blank(false, 3);
		int a = board.addFragment(0, LUX, LUX, false, "");
		EngramWeaveRules.extend(board, a, LINE[1], plenty());
		EngramWeaveRules.extend(board, a, LINE[2], plenty());
		assertEquals(Step.OK, EngramWeaveRules.extend(board, a, VESSEL, plenty()));
	}

	@Test
	void extendRefusedPastStock() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		int[] stock = new int[8];
		stock[ANIMUS] = 2;
		assertEquals(Step.OK, EngramWeaveRules.extend(board, a, LINE[1], stock));
		assertEquals(Step.OK, EngramWeaveRules.extend(board, a, LINE[2], stock));
		assertEquals(Step.NO_STOCK, EngramWeaveRules.extend(board, a, VESSEL, stock));
	}

	@Test
	void completionNeedsEveryTrueFragmentHomeAndIgnoresIntrusions() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		board.addFragment(4, ANIMUS, ANIMUS, true, "blood_shot");
		assertFalse(EngramWeaveRules.complete(board, counts(ANIMUS, 1)));
		for (int node : new int[]{LINE[1], LINE[2], VESSEL}) EngramWeaveRules.extend(board, a, node, plenty());
		assertTrue(EngramWeaveRules.complete(board, counts(ANIMUS, 1)));
		assertFalse(EngramWeaveRules.complete(board, counts(ANIMUS, 2)), "the recipe's count must be met exactly");
	}

	@Test
	void anIntrusionReachingTheVesselIsReported() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int i = board.addFragment(0, ANIMUS, ANIMUS, true, "blood_shot");
		for (int node : new int[]{LINE[1], LINE[2], VESSEL}) EngramWeaveRules.extend(board, i, node, plenty());
		assertEquals(java.util.List.of(i), EngramWeaveRules.connectedIntrusions(board));
	}

	@Test
	void aGreyLaneTakesItsPrismsColourAndRefusesAWrongPrism() {
		EngramBoard board = EngramBoard.blank(true, 0);
		int g = board.addFragment(0, -1, LUX, false, "");
		board.setNode(LINE[1], NodeKind.PRISM, FLAMMEUS);
		assertEquals(Step.WRONG_PRISM, EngramWeaveRules.extend(board, g, LINE[1], plenty()));
		board.setNode(LINE[1], NodeKind.PRISM, LUX);
		assertEquals(Step.OK, EngramWeaveRules.extend(board, g, LINE[1], plenty()));
		assertEquals(LUX, EngramWeaveRules.colour(board, g));
		assertEquals(1, EngramWeaveRules.committed(board, LUX), "a grey lane pays in its true colour");
	}

	@Test
	void anUndyedGreyLaneCannotEnterAKeyedFaceOrPly() {
		EngramBoard board = EngramBoard.blank(true, 0);
		int g = board.addFragment(0, -1, ANIMUS, false, "");
		board.addFragment(LINE[2], ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.extend(board, g, LINE[1], plenty());
		assertEquals(Step.OCCUPIED, EngramWeaveRules.extend(board, g, LINE[2], plenty()));
	}

	@Test
	void cuttingAndClearingRefundSpans() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		for (int node : new int[]{LINE[1], LINE[2], VESSEL}) EngramWeaveRules.extend(board, a, node, plenty());
		EngramWeaveRules.cut(board, a, LINE[1]);
		assertArrayEquals(new int[]{0, LINE[1]}, board.lane(a));
		assertEquals(1, EngramWeaveRules.committed(board, ANIMUS));
		EngramWeaveRules.clear(board, a);
		assertEquals(0, EngramWeaveRules.committed(board, ANIMUS));
	}

	@Test
	void sweepFillsOnlyAlongOneLine() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		assertEquals(Step.OK, EngramWeaveRules.sweep(board, a, LINE[3], plenty()));
		assertArrayEquals(new int[]{0, LINE[1], LINE[2], LINE[3]}, board.lane(a));
		assertEquals(Step.NOT_ADJACENT, EngramWeaveRules.sweep(board, a, 6, plenty()));
		assertArrayEquals(new int[]{0, LINE[1], LINE[2], LINE[3]}, board.lane(a));
	}

	@Test
	void sweepingBackOntoThePreviousNodeRetracts() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		EngramWeaveRules.sweep(board, a, LINE[2], plenty());
		assertEquals(Step.RETRACTED, EngramWeaveRules.sweep(board, a, LINE[1], plenty()));
		assertArrayEquals(new int[]{0, LINE[1]}, board.lane(a));
	}

	@Test
	void aSweepStopsAtTheFirstRefusal() {
		EngramBoard board = EngramBoard.blank(false, 0);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		board.setNode(LINE[2], NodeKind.SCAR, -1);
		assertEquals(Step.SCARRED, EngramWeaveRules.sweep(board, a, LINE[3], plenty()));
		assertArrayEquals(new int[]{0, LINE[1]}, board.lane(a));
	}

	@Test
	void stakesFollowTheRank() {
		var ranks = com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.values();
		assertEquals(EngramWeaveRules.Stake.NONE, EngramWeaveRules.stakeFor(ranks[0]));
		assertEquals(EngramWeaveRules.Stake.BURN, EngramWeaveRules.stakeFor(ranks[1]));
		assertEquals(EngramWeaveRules.Stake.PRESSURE, EngramWeaveRules.stakeFor(ranks[2]));
		assertEquals(EngramWeaveRules.Stake.PRESSURE, EngramWeaveRules.stakeFor(ranks[3]));
		assertEquals(EngramWeaveRules.Stake.BITE, EngramWeaveRules.stakeFor(ranks[4]));
	}

	@Test
	void sigilSizeAndReachGrowWithTheLoom() {
		assertEquals(5.0, EngramWeaveRules.tipRadius(0), 1e-9);
		assertEquals(5.75, EngramWeaveRules.tipRadius(1), 1e-9);
		assertEquals(6.5, EngramWeaveRules.tipRadius(2), 1e-9);
		assertEquals(5.5, EngramWeaveRules.traceReach(0), 1e-9);
		assertEquals(8.0, EngramWeaveRules.traceReach(1), 1e-9);
		assertEquals(16.0, EngramWeaveRules.listenReach(1), 1e-9);
		assertEquals(EngramWeaveRules.traceReach(0), EngramWeaveRules.listenReach(0), 1e-9);
		assertTrue(EngramWeaveRules.tipRadius(0) * 0.4142 > 1.7, "the inner octagon clears the loom frame");
	}

	@Test
	void pressureScarsOnlyFreeFleshOffTheReferenceAndTheReferenceStillThreads() {
		int[] counts = {2, 0, 1, 0, 0, 0, 0, 0};
		EngramBoard board = EngramBoardGenerator.generate(5, counts, com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.SUMMA);
		int a = 0;
		EngramWeaveRules.extend(board, a, board.solution(a)[1], plenty());
		java.util.Random random = new java.util.Random(1);
		int changed;
		int rounds = 0;
		while ((changed = EngramWeaveRules.applyPressure(board, random, ANIMUS, "decoy")) >= 0) {
			assertFalse(board.onSolution(changed), "pressure touched the reference at " + changed);
			assertTrue(rounds++ < 40);
		}
		assertEquals(2, board.lane(a).length, "pressure never cuts a lane the weaver drew");
		for (int i = 0; i < board.fragmentCount(); i++) {
			if (board.fragment(i).intrusion()) continue;
			int[] path = board.solution(i);
			for (int k = board.lane(i).length; k < path.length; k++)
				assertEquals(Step.OK, EngramWeaveRules.extend(board, i, path[k], plenty()));
		}
		assertTrue(EngramWeaveRules.complete(board, counts));
	}

	@Test
	void pressureRecallsAnIntrusionOnceNoFreeFleshRemains() {
		EngramBoard board = EngramBoard.blank(false, 0);
		board.addFragment(0, ANIMUS, ANIMUS, false, "");
		for (int n = 1; n < VESSEL; n++) board.setNode(n, NodeKind.SCAR, -1);
		int node = EngramWeaveRules.applyPressure(board, new java.util.Random(2), ANIMUS, "blood_shot");
		assertTrue(EngramSigilGeometry.isTip(node) && node != 0, "a scarred tip reopens with a new false fragment");
		assertTrue(board.fragment(board.fragmentAt(node)).intrusion());
		assertEquals("blood_shot", board.fragment(board.fragmentAt(node)).decoy());
		for (int i = 0; i < 6; i++) EngramWeaveRules.applyPressure(board, new java.util.Random(i), ANIMUS, "blood_shot");
		assertEquals(-1, EngramWeaveRules.applyPressure(board, new java.util.Random(2), ANIMUS, "blood_shot"),
				"every tip holds a fragment: nothing is left to change");
	}

	@Test
	void aBoardSurvivesSaving() {
		EngramBoard board = EngramBoard.blank(true, 5);
		int a = board.addFragment(0, ANIMUS, ANIMUS, false, "");
		board.addFragment(4, FLAMMEUS, FLAMMEUS, true, "fire_pulse");
		board.setNode(LINE[3], NodeKind.MEMBRANE, FLAMMEUS);
		board.setNode(9, NodeKind.SCAR, -1);
		EngramWeaveRules.extend(board, a, LINE[1], plenty());
		board.setPressure(2);
		board.setReference(new int[]{3, 3, 0, 0, 0, 0, 0, 0});
		board.premarkIntrusions();
		EngramBoard loaded = EngramBoard.load(board.save());
		assertEquals(board.save(), loaded.save());
		assertTrue(loaded.fragment(1).premarked());
		assertEquals("fire_pulse", loaded.fragment(1).decoy());
		assertEquals(2, loaded.pressure());
	}
}
