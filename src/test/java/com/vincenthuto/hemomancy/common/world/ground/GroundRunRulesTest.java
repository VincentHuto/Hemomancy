package com.vincenthuto.hemomancy.common.world.ground;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 6: the ground network's pure rules. A run joins faces and edges but not corners and stops at its
 * bound; a pulse lights two cells of distance a tick; a stain ages wet, tacky, dry, gone; a vein's allowance grows with
 * degree and it moves blood only toward a drawing end; a hot run burns its four nearest furnaces; a basin's gutters
 * fall a level every eight.
 */
class GroundRunRulesTest {
	private static Set<BlockPos> line(int length) {
		Set<BlockPos> cells = new HashSet<>();
		for (int x = 0; x < length; x++) cells.add(new BlockPos(x, 0, 0));
		return cells;
	}

	@Test
	void aSharedGutterShowsTheHighestStreamAnyBasinGivesIt() {
		assertEquals(3, GroundRunRules.sharedFill(1, 3, 0), "a dry basin's walk lowered a gutter another basin keeps wet");
		assertEquals(2, GroundRunRules.sharedFill(2), "a gutter one basin alone wets shows its stream");
		assertEquals(2, GroundRunRules.sharedFill(0, 2, 1));
		assertEquals(0, GroundRunRules.sharedFill(0, 0, 0), "a gutter no basin wets is dry");
		assertEquals(0, GroundRunRules.sharedFill(-1), "a stream is never below dry");
	}

	@Test
	void cellsJoinByFaceOrEdgeButNotCorner() {
		assertTrue(GroundRunRules.joined(BlockPos.ZERO, new BlockPos(1, 0, 0)), "a face joins");
		assertTrue(GroundRunRules.joined(BlockPos.ZERO, new BlockPos(1, 1, 0)), "an edge joins");
		assertFalse(GroundRunRules.joined(BlockPos.ZERO, new BlockPos(1, 1, 1)), "a corner does not join");
		assertFalse(GroundRunRules.joined(BlockPos.ZERO, BlockPos.ZERO), "a cell does not join itself");
		assertFalse(GroundRunRules.joined(BlockPos.ZERO, new BlockPos(2, 0, 0)), "a gap does not join");
	}

	@Test
	void aRunStopsAtItsBound() {
		Set<BlockPos> cells = line(100);
		List<GroundRunRules.Step> run = GroundRunRules.walk(BlockPos.ZERO, cells::contains, GroundRunRules.MAX_RUN);
		assertEquals(GroundRunRules.MAX_RUN, run.size(), "a pulse reaches 64 cells and no more");
		assertEquals(GroundRunRules.MAX_RUN - 1, run.getLast().distance(), "breadth first along a line");
		assertEquals(new BlockPos(62, 0, 0), run.getLast().parent(), "each cell remembers where it was reached from");
		assertEquals(BlockPos.ZERO, run.getFirst().parent(), "the origin is its own parent");
	}

	@Test
	void aRunFollowsAStepAndACornerButNotADiagonalCorner() {
		Set<BlockPos> cells = new HashSet<>(List.of(BlockPos.ZERO, new BlockPos(1, 1, 0), new BlockPos(1, 1, 1)));
		assertEquals(3, GroundRunRules.walk(BlockPos.ZERO, cells::contains, 64).size(), "edges join: up a step, round a corner");
		Set<BlockPos> corner = new HashSet<>(List.of(BlockPos.ZERO, new BlockPos(1, 1, 1)));
		assertEquals(1, GroundRunRules.walk(BlockPos.ZERO, corner::contains, 64).size(), "a cube's corner does not join");
		assertTrue(GroundRunRules.walk(new BlockPos(5, 5, 5), cells::contains, 64).isEmpty(), "no run where there is no cell");
	}

	@Test
	void aPulseLightsTwoCellsOfDistanceATick() {
		assertEquals(1L, GroundRunRules.lightDelay(0));
		assertEquals(1L, GroundRunRules.lightDelay(1));
		assertEquals(2L, GroundRunRules.lightDelay(2));
		assertEquals(32L, GroundRunRules.lightDelay(63), "a full run lights over a second and a half, not in one tick");
	}

	@Test
	void aPointingRunFlaresNearestItsPlace() {
		List<GroundRunRules.Step> run = GroundRunRules.walk(BlockPos.ZERO, line(10)::contains, 64);
		assertEquals(new BlockPos(9, 0, 0), GroundRunRules.farEnd(run, new BlockPos(500, 64, 3)));
		assertEquals(BlockPos.ZERO, GroundRunRules.farEnd(run, new BlockPos(-40, 0, 0)));
	}

	@Test
	void aBarePulseIsShortAndACatalystSetsTheTime() {
		assertEquals(GroundRunRules.BARE_LIT_TICKS, GroundRunRules.litTicks(0));
		assertEquals(7200, GroundRunRules.litTicks(7200));
	}

	@Test
	void aStainAgesWetTackyDryThenGoes() {
		assertEquals(GroundRunRules.TACKY, GroundRunRules.nextStainAge(GroundRunRules.WET, 0.1F));
		assertEquals(GroundRunRules.WET, GroundRunRules.nextStainAge(GroundRunRules.WET, 0.9F));
		assertEquals(GroundRunRules.DRY, GroundRunRules.nextStainAge(GroundRunRules.TACKY, 0.1F));
		assertEquals(-1, GroundRunRules.nextStainAge(GroundRunRules.DRY, 0.1F), "a dry stain wears away");
		assertEquals(GroundRunRules.DRY, GroundRunRules.nextStainAge(GroundRunRules.DRY, 0.9F));
		assertEquals(GroundRunRules.STAIN_REFUND_ML, GroundRunRules.stainRefund(GroundRunRules.WET));
		assertEquals(0.0D, GroundRunRules.stainRefund(GroundRunRules.TACKY), "a tacky stain gives nothing back");
		assertTrue(GroundRunRules.STAIN_REFUND_ML < GroundRunRules.STAIN_ML, "taking a stain up never pays more than it cost");
	}

	@Test
	void veinLengthGrowsWithDegree() {
		assertEquals(0, GroundRunRules.veinAllowance(2), "no veins before the Third Degree");
		int last = 0;
		for (int degree = 3; degree <= 8; degree++) {
			int allowance = GroundRunRules.veinAllowance(degree);
			assertTrue(allowance >= last, "allowance never shrinks with degree");
			last = allowance;
		}
		assertEquals(GroundRunRules.MAX_RUN, GroundRunRules.veinAllowance(8));
	}

	/** CL-14, rewritten from the old levelling rule: a vein moves blood only toward an end that draws. */
	@Test
	void aVeinMovesBloodOnlyTowardADrawingEnd() {
		// Nothing draws: a full tank beside an empty one moves nothing (the old rule levelled them).
		assertEquals(0.0D, GroundRunRules.veinTransfer(600, 0, 600, 0, 0, 600, 600, 0, 20.0D), 1.0E-9);
		// B draws: up to the flow.
		assertEquals(20.0D, GroundRunRules.veinTransfer(600, 0, 600, 0, 0, 600, 600, 600, 20.0D), 1.0E-9);
		// Never past what B draws, the room in B or what A holds fresh.
		assertEquals(7.0D, GroundRunRules.veinTransfer(600, 0, 600, 0, 0, 600, 600, 7, 20.0D), 1.0E-9);
		assertEquals(5.0D, GroundRunRules.veinTransfer(600, 0, 600, 0, 595, 5, 600, 600, 20.0D), 1.0E-9);
		assertEquals(3.0D, GroundRunRules.veinTransfer(3, 597, 600, 0, 0, 600, 600, 600, 20.0D), 1.0E-9);
		// A draws: blood runs from B to A (negative), even from the emptier share.
		assertEquals(-20.0D, GroundRunRules.veinTransfer(900, 100, 1000, 100, 100, 500, 600, 0, 20.0D), 1.0E-9);
		// Both draw: the fuller levels into the emptier and stops where they meet, as before.
		assertEquals(15.0D, GroundRunRules.veinTransfer(30, 570, 600, 570, 0, 600, 600, 600, 20.0D), 1.0E-9);
		assertEquals(0.0D, GroundRunRules.veinTransfer(300, 300, 600, 300, 1000, 1000, 2000, 1000, 20.0D), 1.0E-9);
	}

	/** NQ-02-3 A: a recent draw pulls to fill the room; a machine under its reserve pulls up to it; a store never pulls by level. */
	@Test
	void anEndDrawsWhenDrawnFromOrAMachineIsUnderItsReserve() {
		long now = 1_000L;
		long window = GroundRunRules.VEIN_DRAW_WINDOW_TICKS;
		assertEquals(400.0D, GroundRunRules.drawDemand(now, now - window, 1_600, 400, false, 600), 1.0E-9,
				"a store drawn from within five seconds pulls");
		assertEquals(0.0D, GroundRunRules.drawDemand(now, now - window - 1, 0, 2_000, false, 600), 1.0E-9,
				"an empty store nobody draws from never pulls by its level");
		assertEquals(0.0D, GroundRunRules.drawDemand(now, GroundRunRules.NEVER_DRAWN, 0, 2_000, false, 600), 1.0E-9);
		assertEquals(600.0D, GroundRunRules.drawDemand(now, GroundRunRules.NEVER_DRAWN, 0, 600, true, 600), 1.0E-9,
				"a dry machine pulls to its working reserve");
		assertEquals(100.0D, GroundRunRules.drawDemand(now, GroundRunRules.NEVER_DRAWN, 500, 1_000, true, 600), 1.0E-9,
				"a machine pulls only up to its reserve");
		assertEquals(0.0D, GroundRunRules.drawDemand(now, GroundRunRules.NEVER_DRAWN, 600, 400, true, 600), 1.0E-9,
				"a machine at its reserve nobody draws from does not pull");
		assertEquals(0.0D, GroundRunRules.drawDemand(now, now, 600, 0, true, 600), 1.0E-9, "a full tank draws nothing");
	}

	/** CQ-43: a hot run burns the four furnaces nearest its pulse, each once. */
	@Test
	void aHotRunTakesTheFourNearestFurnaces() {
		List<GroundRunRules.Step> run = GroundRunRules.walk(BlockPos.ZERO, line(6)::contains, 64);
		// Furnaces north and south of every cell: twelve in all.
		Set<BlockPos> furnaces = new HashSet<>();
		for (int x = 0; x < 6; x++) {
			furnaces.add(new BlockPos(x, 0, -1));
			furnaces.add(new BlockPos(x, 0, 1));
		}
		var sides = GroundRunRules.furnaceSides(run, furnaces::contains, GroundRunRules.HOT_FURNACE_MAX);
		int burned = sides.values().stream().mapToInt(Integer::bitCount).sum();
		assertEquals(4, burned, "a hot run burned " + burned + " furnaces");
		assertEquals(2, Integer.bitCount(sides.get(BlockPos.ZERO)), "the pulse's own cell takes its two furnaces first");
		assertEquals(2, Integer.bitCount(sides.get(new BlockPos(1, 0, 0))));
		assertFalse(sides.containsKey(new BlockPos(2, 0, 0)), "a farther cell burned a furnace past the cap");
		assertTrue(GroundRunRules.burnsSide(sides.get(BlockPos.ZERO), net.minecraft.core.Direction.NORTH));
		// A furnace between two cells is counted once.
		Set<BlockPos> shared = Set.of(new BlockPos(1, 0, 0));
		List<GroundRunRules.Step> pair = GroundRunRules.walk(BlockPos.ZERO, Set.of(BlockPos.ZERO, new BlockPos(1, 1, 0))::contains, 64);
		assertEquals(2, pair.size());
		var once = GroundRunRules.furnaceSides(pair, shared::contains, 4);
		assertEquals(1, once.values().stream().mapToInt(Integer::bitCount).sum(), "a furnace beside two cells was taken twice");
		assertTrue(once.containsKey(BlockPos.ZERO), "the nearer cell did not take the shared furnace");
	}

	@Test
	void aBasinsGuttersFallALevelEveryEight() {
		assertEquals(0, GroundRunRules.basinFill(0, 2000));
		assertEquals(1, GroundRunRules.basinFill(100, 2000));
		assertEquals(2, GroundRunRules.basinFill(1000, 2000));
		assertEquals(3, GroundRunRules.basinFill(2000, 2000));
		assertEquals(3, GroundRunRules.streamAt(3, 7));
		assertEquals(2, GroundRunRules.streamAt(3, 8));
		assertEquals(0, GroundRunRules.streamAt(3, 24), "a full basin's stream runs dry 24 gutters out");
		assertEquals(0, GroundRunRules.streamAt(0, 0), "an empty basin wets nothing");
		assertEquals(5.0D, GroundRunRules.deliveryShare(1000, 2), 1.0E-9, "two ends share a second's flow");
		assertEquals(1.5D, GroundRunRules.deliveryShare(3, 2), 1.0E-9, "never more than the fresh blood left");
		assertEquals(0.0D, GroundRunRules.deliveryShare(1000, 0), 1.0E-9);
	}

	@Test
	void everyFunctionHasAName() {
		for (GroundFunction function : GroundFunction.values()) {
			assertNotNull(GroundFunction.byName("hemomancy:" + function.getSerializedName()));
		}
		assertFalse(GroundFunction.DARK.drawn(), "a dark run hides itself");
		assertEquals(0, GroundFunction.DARK.light());
		assertTrue(GroundFunction.LIGHT.light() > GroundFunction.HOT.light(), "a light run is the brightest");
	}
}
