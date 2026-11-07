package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Frames aim due south (+z) from the origin; "right" is then -x. */
class HemorathFootprintTest {
	private static final SaintMoveInputs ONE = new SaintMoveInputs(1, 0, 0, 1);
	private static final AABB FLOOR = new AABB(-7.5, 0, -7.5, 7.5, 16, 7.5);

	private static FootprintFrame frame(List<Vec3> points) {
		return new FootprintFrame(Vec3.ZERO, new Vec3(0, 0, 10), new Vec3(0, 0, 10), 0.0D, points, LongSets.EMPTY_SET,
				List.of(), FLOOR, FLOOR.inflate(10, 0, 6));
	}

	private static boolean hits(HemorathMove move, SaintMoveInputs in, FootprintFrame frame, Vec3 at, double age) {
		return move.footprint(in).hits(frame, at, true, age);
	}

	@Test
	void theClawsConeAndItsBackhandOnTheOtherSide() {
		FootprintFrame f = frame(List.of());
		assertTrue(hits(HemorathMove.RAKING_CLAW, ONE, f, new Vec3(0, 0, 3), 0));
		assertFalse(hits(HemorathMove.RAKING_CLAW, ONE, f, new Vec3(0, 0, 5), 0));
		assertFalse(hits(HemorathMove.RAKING_CLAW, ONE, f, new Vec3(0, 0, -2), 0));
		// the backhand sits 45 degrees to the left (positive x when aiming south)
		assertTrue(hits(HemorathMove.BACKHAND, ONE, f, new Vec3(2, 0, 2), 0));
		assertFalse(hits(HemorathMove.BACKHAND, ONE, f, new Vec3(-2, 0, 2), 0));
		assertTrue(hits(HemorathMove.STAFF_HEEL, ONE, f, new Vec3(0, 0, -2.5), 0));
		assertFalse(hits(HemorathMove.STAFF_HEEL, ONE, f, new Vec3(0, 0, 2.5), 0));
	}

	@Test
	void theRakesLanesHaveGapsThatNarrowWithIron() {
		FootprintFrame f = frame(List.of());
		assertTrue(hits(HemorathMove.IRON_RAKE, ONE, f, new Vec3(0, 0, 6), 12));
		assertTrue(hits(HemorathMove.IRON_RAKE, ONE, f, new Vec3(2, 0, 6), 12));
		assertFalse(hits(HemorathMove.IRON_RAKE, ONE, f, new Vec3(1, 0, 6), 12), "the gap between lanes is safe");
		assertFalse(hits(HemorathMove.IRON_RAKE, ONE, f, new Vec3(4, 0, 6), 12), "three lanes with no iron");
		SaintMoveInputs iron = new SaintMoveInputs(1, 4, 0, 1);
		assertTrue(hits(HemorathMove.IRON_RAKE, iron, f, new Vec3(4, 0, 6), 12), "five lanes from four patches");
		assertFalse(hits(HemorathMove.IRON_RAKE, ONE, f, new Vec3(0, 0, 13), 2), "the spikes run out near to far");
	}

	@Test
	void theRuptureIsJumpedAndTheTideIsTheBossFloorToKneeHeight() {
		FootprintFrame f = frame(List.of());
		BossFootprint rupture = HemorathMove.MARROW_RUPTURE.footprint(new SaintMoveInputs(2, 0, 0, 1));
		assertTrue(rupture.hits(f, new Vec3(5, 0, 0), true, 12));
		assertFalse(rupture.hits(f, new Vec3(5, 1.0, 0), false, 12), "feet above 0.9 clear it");
		assertFalse(rupture.hits(f, new Vec3(11, 0, 0), true, 23), "beyond ten is safe");
		BossFootprint tide = HemorathMove.EXSANGUINATING_TIDE.footprint(new SaintMoveInputs(2, 0, 0, 1));
		assertTrue(tide.hits(f, new Vec3(6, 0, -6), true, 0));
		assertFalse(tide.hits(f, new Vec3(0, 0, 9), true, 0), "the aisles are dry");
		assertFalse(tide.hits(f, new Vec3(0, 0.8, 0), false, 0), "above 0.6 is out of it");
	}

	@Test
	void theRibBurstsWedgesAreSafe() {
		FootprintFrame f = frame(List.of());
		BossFootprint burst = HemorathMove.RIB_BURST.footprint(ONE);
		double spoke = Math.toRadians(HemorathMove.RIB_OFFSET);
		Vec3 onSpoke = new Vec3(-Math.sin(spoke) * 4, 0, Math.cos(spoke) * 4);
		assertTrue(burst.hits(f, onSpoke, true, 4));
		assertFalse(burst.hits(f, new Vec3(0, 0, 4), true, 4), "between spokes is a wedge");
		assertFalse(burst.hits(f, onSpoke.scale(2.2), true, 4), "beyond eight is safe");
	}

	@Test
	void theStaffFallsLineThenItsChainMarks() {
		List<Vec3> marks = List.of(new Vec3(1.5, 0, 7), new Vec3(-1.5, 0, 7));
		FootprintFrame f = frame(marks);
		BossFootprint fall = HemorathMove.STAFF_FALL.footprint(ONE);
		assertTrue(fall.hits(f, new Vec3(0, 0, 5), true, 0));
		assertFalse(fall.hits(f, new Vec3(1.5, 0, 7), true, 1), "the chains land four ticks after the line");
		assertTrue(fall.hits(f, new Vec3(1.5, 0, 7), true, HemorathMove.CHAIN_DELAY));
	}

	@Test
	void theHaloFliesOutStraightAndBowsHomeToHisLeft() {
		List<Vec3> path = new ArrayList<>();
		for (double[] p : HemorathHaloPath.points()) path.add(new Vec3(-p[1], 0, p[0]));
		FootprintFrame f = frame(path);
		BossFootprint halo = HemorathMove.SPIKED_HALO.footprint(new SaintMoveInputs(2, 0, 0, 1));
		double outTick = 5.0D / HemorathHaloPath.speed();
		assertTrue(halo.hits(f, new Vec3(0, 1.0, 5), false, outTick));
		assertFalse(halo.hits(f, new Vec3(0, 3.0, 5), false, outTick), "a halo passes over at most 2.2");
		double[] back = HemorathHaloPath.at(HemorathHaloPath.FLIGHT_TICKS * 0.75D);
		assertTrue(back[1] < -1.0D, "the return arc bows to his staff side");
		// half a block behind the live front's head, so the check does not sit on its floating-point edge
		double[] behind = HemorathHaloPath.at(HemorathHaloPath.FLIGHT_TICKS * 0.75D - 0.5D / HemorathHaloPath.speed());
		assertTrue(halo.hits(f, new Vec3(-behind[1], 1.0, behind[0]), false, HemorathHaloPath.FLIGHT_TICKS * 0.75D));
	}

	@Test
	void theSiphonAndTheTitheReadSight() {
		assertInstanceOf(BossFootprint.Occluded.class, HemorathMove.MARROW_SIPHON.footprint(ONE));
		assertInstanceOf(BossFootprint.Occluded.class, HemorathMove.TITHE_RECKONING.footprint(ONE));
		FootprintFrame f = frame(List.of());
		assertTrue(HemorathMove.TITHE_RECKONING.footprint(ONE).hits(f, new Vec3(0, 0, 10), true, 0));
		assertFalse(HemorathMove.TITHE_RECKONING.footprint(ONE).hits(f, new Vec3(3, 0, 10), true, 0),
				"the Tithe strikes its target alone");
		assertInstanceOf(BossFootprint.None.class, HemorathMove.WELLS_ANSWER.footprint(ONE));
	}
}
