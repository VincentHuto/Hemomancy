package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Catch;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Patch;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Placement;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Schedule;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Source;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rot sludge (design revision 2026-11-06, R2): which moves lay and how often, placement and clipping, the overlap
 * splash, the cap's oldest-first settle, every catch source with the earliest winning, runner timing, the fallback by
 * 140, the ash cells and their budget, the ending's burn, and the hazard kinds' contract.
 */
final class PutricielSludgeRulesTest {
	private static final double Y = 64.0D;
	private static final AABB FLOOR = new AABB(0.0D, Y, 0.0D, 15.0D, Y + 16.0D, 15.0D);
	private static final LongSet NO_ASH = new LongOpenHashSet();

	private static Vec3 at(double x, double z) {
		return new Vec3(x, Y, z);
	}

	// ── what lays ────────────────────────────────────────────────────

	@Test
	void onlyTheFiveRotMovesLayAndNeverInTheFire() {
		assertEquals(5, PutricielSludgeRules.LAYERS.size());
		for (PutricielMove move : PutricielMove.values()) {
			boolean layer = PutricielSludgeRules.LAYERS.contains(move);
			if (layer) assertFalse(move.fire(), move + " is a rot move");
			assertEquals(layer, PutricielSludgeRules.lays(move, PutricielStage.ROT), move + " in the rot");
			for (PutricielStage stage : new PutricielStage[]{PutricielStage.IGNITING, PutricielStage.BURNING, PutricielStage.EMBERING}) {
				assertFalse(PutricielSludgeRules.lays(move, stage), move + " lays nothing in " + stage);
			}
		}
		for (PutricielMove quiet : new PutricielMove[]{PutricielMove.CENSER_SWING, PutricielMove.CENSER_BACKSWING,
				PutricielMove.ROT_BREATH, PutricielMove.SAC_SWELL, PutricielMove.CENSER_ORBIT, PutricielMove.MOULD_COMMUNION}) {
			assertFalse(PutricielSludgeRules.LAYERS.contains(quiet), quiet + " comes too often or is a cloud");
		}
	}

	@Test
	void theVolleyPoolsTwoOfFiveAndThreeOfSevenOrNine() {
		assertEquals(2, pooled(5));
		assertEquals(3, pooled(7));
		assertEquals(3, pooled(9));
		assertTrue(PutricielSludgeRules.volleyPool(0), "the quarry's locked disc always pools");
	}

	private static int pooled(int landings) {
		int count = 0;
		for (int i = 0; i < landings; i++) if (PutricielSludgeRules.volleyPool(i)) count++;
		return count;
	}

	@Test
	void everyOtherGraspAndOnlyAMissedSnare() {
		assertTrue(PutricielSludgeRules.graspPool(2));
		assertFalse(PutricielSludgeRules.graspPool(3));
		assertTrue(PutricielSludgeRules.snarePool(false), "a Snare that strikes no one pools");
		assertFalse(PutricielSludgeRules.snarePool(true));
	}

	// ── placement ────────────────────────────────────────────────────

	@Test
	void aPoolStaysOnTheBossFloorClippedToIt() {
		assertEquals(Outcome.SKIP, PutricielSludgeRules.place(Patch.disc(at(-1.0D, 7.5D), 1.5D), FLOOR, List.of(), NO_ASH,
				List.of()).outcome(), "off the floor");
		Placement edge = PutricielSludgeRules.place(Patch.disc(at(1.2D, 7.5D), 1.5D), FLOOR, List.of(), NO_ASH, List.of());
		assertEquals(Outcome.LAY, edge.outcome());
		assertEquals(1.2D, edge.patch().radius(), 1.0E-6D, "clipped to the floor's edge");
		assertEquals(Outcome.SKIP, PutricielSludgeRules.place(Patch.disc(at(0.5D, 7.5D), 1.5D), FLOOR, List.of(), NO_ASH,
				List.of()).outcome(), "clipped under a block it is not laid");
	}

	@Test
	void thePyreCirclesStayClean() {
		Vec3 pyre = at(10.5D, 7.5D);
		Placement near = PutricielSludgeRules.place(Patch.disc(at(7.5D, 7.5D), 2.0D), FLOOR, List.of(pyre), NO_ASH, List.of());
		assertEquals(Outcome.LAY, near.outcome());
		assertTrue(near.patch().radius() + PutricielArenaRules.PYRE_SAFE <= 3.0D + 1.0E-6D, "the rim stops at the circle");
		assertEquals(Outcome.SKIP, PutricielSludgeRules.place(Patch.disc(at(12.0D, 7.5D), 1.5D), FLOOR, List.of(pyre), NO_ASH,
				List.of()).outcome(), "inside a pyre's circle it never lies");
	}

	@Test
	void neverOnThisFightsAsh() {
		LongSet ash = new LongOpenHashSet();
		ash.add(BossFootprint.cell(9, 7));
		assertEquals(Outcome.SKIP, PutricielSludgeRules.place(Patch.disc(at(9.5D, 7.5D), 1.5D), FLOOR, List.of(), ash,
				List.of()).outcome(), "a disc whose centre cell is ash is not laid");
		Placement clipped = PutricielSludgeRules.place(Patch.disc(at(7.5D, 7.5D), 2.0D), FLOOR, List.of(), ash, List.of());
		assertEquals(Outcome.LAY, clipped.outcome());
		assertEquals(1.5D, clipped.patch().radius(), 1.0E-6D, "clipped to the nearest ash centre less 0.5");
		Placement runnel = PutricielSludgeRules.place(Patch.runnel(at(2.5D, 7.5D), at(10.5D, 7.5D), 0.55D), FLOOR, List.of(),
				ash, List.of());
		assertEquals(Outcome.LAY, runnel.outcome());
		assertTrue(runnel.patch().to().x < 9.0D - 0.55D, "a runnel stops short of its first ash cell");
		assertTrue(runnel.patch().length() >= PutricielSludgeRules.MIN_LENGTH);
	}

	@Test
	void aPoolOverlapsAnOlderOneByAtMostFortyPercentElseItSplashes() {
		List<Patch> older = List.of(Patch.disc(at(5.0D, 5.0D), 1.5D));
		assertEquals(Outcome.LAY, PutricielSludgeRules.place(Patch.disc(at(7.5D, 5.0D), 1.5D), FLOOR, List.of(), NO_ASH,
				older).outcome(), "0.5 of overlap is under 40% of 1.5");
		Placement splash = PutricielSludgeRules.place(Patch.disc(at(7.0D, 5.0D), 1.5D), FLOOR, List.of(), NO_ASH, older);
		assertEquals(Outcome.SPLASH, splash.outcome());
		assertEquals(0, splash.onto(), "onto the older pool's surface");
	}

	// ── the cap ──────────────────────────────────────────────────────

	@Test
	void theCapGrowsWithTheGroupAndStops() {
		assertEquals(6, PutricielSludgeRules.poolCap(1));
		assertEquals(7, PutricielSludgeRules.poolCap(2));
		assertEquals(9, PutricielSludgeRules.poolCap(4));
		assertEquals(9, PutricielSludgeRules.poolCap(6));
		assertEquals(42.0D, PutricielSludgeRules.areaCap(1));
		assertEquals(48.0D, PutricielSludgeRules.areaCap(2));
		assertEquals(60.0D, PutricielSludgeRules.areaCap(4));
		assertEquals(60.0D, PutricielSludgeRules.areaCap(9));
		double floor = 15.0D * 15.0D;
		assertTrue(PutricielSludgeRules.areaCap(1) / floor < 0.2D, "the floor never fills");
	}

	@Test
	void pastTheCapTheOldestSettleFirst() {
		List<Patch> six = List.of(Patch.disc(at(2, 2), 1.0D), Patch.disc(at(5, 2), 1.0D), Patch.disc(at(8, 2), 1.0D),
				Patch.disc(at(11, 2), 1.0D), Patch.disc(at(2, 12), 1.0D), Patch.disc(at(5, 12), 1.0D));
		assertEquals(1, PutricielSludgeRules.settleCount(six, Patch.disc(at(8, 12), 1.0D), 1), "the seventh settles one");
		assertEquals(0, PutricielSludgeRules.settleCount(six, Patch.disc(at(8, 12), 1.0D), 2), "a second challenger adds a pool");
		List<Patch> big = List.of(Patch.disc(at(3, 3), 2.0D), Patch.disc(at(9, 3), 2.0D), Patch.disc(at(3, 9), 2.0D));
		assertEquals(1, PutricielSludgeRules.settleCount(big, Patch.disc(at(9, 9), 2.0D), 1), "the area cap settles too");
		assertEquals(0, PutricielSludgeRules.settleCount(List.of(), Patch.disc(at(9, 9), 2.0D), 1));
	}

	// ── the catch ────────────────────────────────────────────────────

	@Test
	void theIgnitionRingCatchesPoolsWithinSixAsItPasses() {
		Vec3 body = at(0.0D, 0.0D);
		Patch near = Patch.disc(at(4.0D, 0.0D), 1.0D);
		Patch far = Patch.disc(at(0.0D, 12.0D), 1.0D);
		Schedule schedule = PutricielSludgeRules.catchSchedule(List.of(near, far), body, List.of(), 4);
		Catch ring = schedule.entry(0);
		assertEquals(Source.RING, ring.source());
		assertEquals(5, ring.tick(), "commit + 10 x 3 / 6");
		assertEquals(3.0D, ring.point().x, 1.0E-6D, "at its nearest edge");
		assertFalse(ring.runner());
		assertEquals(Source.FALLBACK, schedule.entry(1).source());
		assertEquals(PutricielSludgeRules.FALLBACK, schedule.entry(1).tick(), "anything unlit catches by 140");
	}

	@Test
	void aRelitPyreSendsARunnerToItsNearestPool() {
		Vec3 body = at(100.0D, 100.0D);
		Patch pool = Patch.disc(at(6.0D, 0.0D), 1.0D);
		Schedule schedule = PutricielSludgeRules.catchSchedule(List.of(pool), body, List.of(at(0, 0), at(12, 0)), 4);
		Catch runner = schedule.entry(0);
		assertEquals(Source.PYRE, runner.source());
		assertEquals(4, runner.depart(), "4 ticks after the first pyre relights");
		assertEquals(11, runner.tick(), "crawling 3.5 blocks at 0.5 a tick");
		assertEquals(1.5D, runner.from().x, 1.0E-6D, "from its circle's edge");
		assertEquals(5.0D, runner.point().x, 1.0E-6D);
		assertEquals(List.of(0), schedule.departing(4));
		assertTrue(schedule.due(10).isEmpty());
		assertEquals(List.of(0), schedule.due(11));
	}

	@Test
	void aPyreReachesOnlyEightBlocks() {
		Patch pool = Patch.disc(at(13.0D, 0.0D), 1.0D);
		Schedule schedule = PutricielSludgeRules.catchSchedule(List.of(pool), at(100, 100), List.of(at(0, 0)), 4);
		assertEquals(Source.FALLBACK, schedule.entry(0).source(), "10.5 from the circle is past a runner's reach");
	}

	@Test
	void aBurningPoolPassesTheFireOnAndTheEarliestWins() {
		Vec3 body = at(0.0D, 0.0D);
		Patch first = Patch.disc(at(5.0D, 0.0D), 1.0D);
		Patch second = Patch.disc(at(9.0D, 0.0D), 1.5D);
		Schedule schedule = PutricielSludgeRules.catchSchedule(List.of(first, second), body, List.of(), 4);
		assertEquals(7, schedule.entry(0).tick(), "the ring: 10 x 4 / 6, rounded up");
		Catch chain = schedule.entry(1);
		assertEquals(Source.POOL, chain.source());
		assertEquals(13, chain.depart(), "6 ticks after the first caught");
		assertEquals(16, chain.tick(), "1.5 blocks at 0.5 a tick");
		// a fire move lights the first early: its runner leaves early too, and only ever brings a catch forward
		assertTrue(schedule.light(0, 2, first.nearestEdge(body), body, Source.FIRE));
		assertEquals(11, schedule.entry(1).tick());
		assertFalse(schedule.light(0, 1, first.centre(), body, Source.FIRE), "a pool burns once");
		assertTrue(schedule.light(1, 5, second.centre(), body, Source.BODY));
		assertEquals(5, schedule.entry(1).tick());
		assertTrue(schedule.allCaught());
	}

	@Test
	void runnersCarryFireToPoolsOnlyNeverToColonies() {
		Patch colony = Patch.colonyAt(at(4.0D, 0.0D), PutricielHazard.COLONY_RADIUS);
		Schedule fromPyre = PutricielSludgeRules.catchSchedule(List.of(colony), at(100, 100), List.of(at(0, 0)), 4);
		assertEquals(Source.FALLBACK, fromPyre.entry(0).source());
		Patch pool = Patch.disc(at(0.0D, 5.0D), 1.0D);
		Patch nextColony = Patch.colonyAt(at(0.0D, 8.5D), PutricielHazard.COLONY_RADIUS);
		Schedule chain = PutricielSludgeRules.catchSchedule(List.of(pool, nextColony), at(0, 0), List.of(), 4);
		assertEquals(Source.FALLBACK, chain.entry(1).source(), "a colony catches from the ring, the body, a fire move or the fallback");
	}

	@Test
	void everyPatchCatchesByTheFallback() {
		List<Patch> patches = List.of(Patch.disc(at(2, 2), 1.0D), Patch.disc(at(14, 14), 1.0D),
				Patch.runnel(at(3, 12), at(7, 12), 0.55D), Patch.colonyAt(at(12, 3), 1.5D));
		Schedule schedule = PutricielSludgeRules.catchSchedule(patches, at(7.5, 7.5), List.of(at(1, 14)), 4);
		for (int i = 0; i < schedule.size(); i++) {
			assertTrue(schedule.entry(i).tick() <= PutricielSludgeRules.FALLBACK, "patch " + i);
			assertTrue(schedule.entry(i).tick() >= 0);
		}
		assertEquals(schedule.size(), schedule.due(PutricielSludgeRules.FALLBACK).size());
	}

	@Test
	void liveSourcesTouchTheRim() {
		Patch pool = Patch.disc(at(4.5D, 0.0D), 1.0D);
		FootprintFrame frame = FootprintFrame.simple(at(0, 0), at(0, 1), at(0, 1), Y);
		Vec3 hit = PutricielSludgeRules.touched(pool, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 4.0D,
				BossFootprint.Height.STANDING), frame, 0);
		assertNotNull(hit, "a fire footprint over the rim lights it");
		assertTrue(BossFootprint.Shapes.planar(hit, at(0, 0)) <= 4.0D);
		assertNull(PutricielSludgeRules.touched(pool, BossFootprint.None.INSTANCE, frame, 0));
		assertNull(PutricielSludgeRules.touched(pool, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 2.0D,
				BossFootprint.Height.STANDING), frame, 0));
		assertTrue(PutricielSludgeRules.bodyTouches(pool, at(3.0D, 0.0D), 0.5D), "the burning body walks into it");
		assertFalse(PutricielSludgeRules.bodyTouches(pool, at(1.0D, 0.0D), 0.5D));
	}

	@Test
	void theEndingBurnsEveryPoolFromItsEdgeNearestTheBody() {
		Patch pool = Patch.disc(at(6.0D, 0.0D), 1.5D);
		Vec3 point = PutricielSludgeRules.endingCatch(pool, at(0, 0));
		assertEquals(4.5D, point.x, 1.0E-6D);
		assertEquals(10, PutricielSludgeRules.ENDING_BURN, "every hazard burns out over 10 ticks at the ending");
	}

	// ── the ash and the colonies ─────────────────────────────────────

	@Test
	void aBurnedPoolLeavesAshUnderItWithinItsBudget() {
		LongList disc = PutricielSludgeRules.ashCells(Patch.disc(at(7.5D, 7.5D), 1.5D), PutricielArenaRules.SLUDGE_ASH_PER_POOL);
		assertTrue(!disc.isEmpty() && disc.size() <= 8);
		assertEquals(BossFootprint.cell(7, 7), disc.getLong(0), "from the centre out");
		for (long cell : disc) {
			double x = net.minecraft.core.BlockPos.getX(cell) + 0.5D, z = net.minecraft.core.BlockPos.getZ(cell) + 0.5D;
			assertTrue(Math.hypot(x - 7.5D, z - 7.5D) <= 1.5D - PutricielSludgeRules.ASH_INSET + 1.0E-6D, "inside the pool");
		}
		LongList runnel = PutricielSludgeRules.ashCells(Patch.runnel(at(2.5, 2.5), at(6.5, 2.5), 0.55D), 8);
		assertTrue(!runnel.isEmpty() && runnel.size() <= 8);
		assertEquals(3 * PutricielArenaRules.SLUDGE_ASH_PER_POOL, PutricielArenaRules.SLUDGE_ASH_PER_WINDOW);
		assertEquals(64, PutricielArenaRules.SLUDGE_ASH_RECORDED);
		assertEquals("putriciel_sludge_ash", PutricielArenaRules.SLUDGE_ASH_BUDGET);
	}

	@Test
	void aPoolAbsorbsTheColonyItLaysOver() {
		Patch pool = Patch.disc(at(0, 0), 1.5D);
		assertTrue(PutricielSludgeRules.absorbs(pool, at(1.9D, 0.0D), 1.5D));
		assertFalse(PutricielSludgeRules.absorbs(pool, at(2.5D, 0.0D), 1.5D));
	}

	@Test
	void theBloomDrainsClearOfSafeGroundAndChallengers() {
		LongSet safe = new LongOpenHashSet();
		for (int x = 2; x <= 4; x++) for (int z = 2; z <= 4; z++) safe.add(BossFootprint.cell(x, z));
		List<Vec3> players = List.of(at(10.5D, 10.5D));
		List<Vec3> drains = PutricielSludgeRules.bloomDrains(FLOOR, at(7.5, 7.5), 12.0D, safe, players, Y, 42L);
		assertEquals(2, drains.size());
		for (Vec3 drain : drains) {
			assertTrue(PutricielSludgeRules.nearestAsh(drain, safe) >= PutricielSludgeRules.BLOOM_SAFE_GAP, "clear of safe ground");
			assertTrue(BossFootprint.Shapes.planar(drain, players.get(0)) >= PutricielSludgeRules.BLOOM_CHALLENGER_GAP);
		}
		assertTrue(BossFootprint.Shapes.planar(drains.get(0), drains.get(1)) >= 2.0D * PutricielSludgeRules.BLOOM_RADIUS);
		assertEquals(drains, PutricielSludgeRules.bloomDrains(FLOOR, at(7.5, 7.5), 12.0D, safe, players, Y, 42L), "picked by the seed");
	}

	// ── the kit's contract ───────────────────────────────────────────

	@Test
	void aPoolsFrameOriginIsItsCentre() {
		Patch disc = Patch.disc(at(5, 5), 1.5D);
		FootprintFrame frame = PutricielSludgeRules.frame(disc, Y);
		assertEquals(disc.centre(), frame.origin());
		assertEquals(disc, PutricielSludgeRules.patchOf(PutricielSludgeRules.shape(disc), frame, false));
		Patch runnel = Patch.runnel(at(2, 2), at(6, 2), 0.55D);
		FootprintFrame lane = PutricielSludgeRules.frame(runnel, Y);
		assertEquals(4.0D, lane.origin().x, 1.0E-6D, "a runnel's origin is its midpoint");
		assertEquals(runnel, PutricielSludgeRules.patchOf(PutricielSludgeRules.shape(runnel), lane, false));
		assertTrue(PutricielSludgeRules.shape(runnel).hits(lane, at(5.5D, 2.3D), 0));
		assertFalse(PutricielSludgeRules.shape(runnel).hits(lane, at(5.5D, 2.8D), 0));
		// the burning pool keeps the shape while its aim moves to the catch point
		assertTrue(PutricielSludgeRules.shape(runnel).hits(lane.withAim(at(2, 2)).withTarget(at(0, 0)), at(3.0D, 2.0D), 0));
	}

	@Test
	void theBeatsAndTheHazardKinds() {
		assertEquals(6, PutricielSludgeRules.CATCH);
		assertEquals(30, PutricielSludgeRules.BOIL_END);
		assertEquals(44, PutricielSludgeRules.ASH_AT);
		assertEquals(50, PutricielSludgeRules.BURN);
		assertEquals(60, PutricielSludgeRules.COOL);
		assertEquals(30, PutricielSludgeRules.SETTLE);
		assertEquals(8, PutricielSludgeRules.FORM);
		assertEquals(0.5D, PutricielSludgeRules.RUNNER_SPEED);
		assertEquals(30, PutricielSludgeRules.COLONY_BURN);
		assertEquals(5, PutricielHazard.ROT_SLUDGE.id());
		assertEquals(6, PutricielHazard.SLUDGE_FIRE.id());
		assertEquals(7, PutricielHazard.FIRE_RUNNER.id());
		Set<Integer> ids = new HashSet<>();
		for (PutricielHazard kind : PutricielHazard.values()) assertTrue(ids.add(kind.id()), kind + " repeats an id");
		assertEquals(2400, PutricielHazard.ROT_SLUDGE.life());
		assertEquals(5, PutricielHazard.ROT_SLUDGE.pulse());
		assertEquals(0.0F, PutricielHazard.ROT_SLUDGE.damage(), "the denial is the slow and the growth");
		assertEquals(0.5F, PutricielHazard.ROT_SLUDGE.colonisation());
		assertFalse(PutricielHazard.ROT_SLUDGE.fire(), "no red until it burns");
		assertEquals(110, PutricielHazard.SLUDGE_FIRE.life());
		assertEquals(10, PutricielHazard.SLUDGE_FIRE.pulse());
		assertEquals(1.0F, PutricielHazard.SLUDGE_FIRE.damage());
		assertEquals(-2.0F, PutricielHazard.SLUDGE_FIRE.colonisation());
		assertFalse(PutricielHazard.SLUDGE_FIRE.harmless(49));
		assertTrue(PutricielHazard.SLUDGE_FIRE.harmless(50), "cooling touches nothing");
		assertTrue(PutricielHazard.FIRE_RUNNER.harmless(0), "a runner is harmless");
		assertFalse(PutricielHazard.ROT_COLONY.harmless(1000));
		assertTrue(PutricielHazard.ROT_SLUDGE.catches() && PutricielHazard.ROT_COLONY.catches());
		assertFalse(PutricielHazard.ROT_CLOUD.catches(), "breath clouds flash off at the commit instead");
		assertEquals(PutricielSludgeRules.runnerLife(4.0D), 8 + 30, "a runner lives its travel plus 30");
		assertEquals(-0.35D, PutricielSludgeRules.SLOW, "x0.65 while wading");
		assertEquals(6, PutricielSludgeRules.SLOW_RELEASE);
	}
}
