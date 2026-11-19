package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Fire Breather's interventions and the Cinder Bellows' moves burn what they mark and miss what reads them. */
class FireEaterOrdealGeometryTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 3.0, 0.5);

	@Test
	void theBreathStartsAtTheNearRimAndCrossesThroughTheFighter() {
		Vec3 from = FireEaterChoreography.position(FLOOR, 0.0D);
		Vec3 mark = new Vec3(2.5, 3.0, 2.5);
		Vec3 start = FireEaterOrdealGeometry.breathStart(FLOOR, mark, from);
		assertEquals(FireEaterOrdealGeometry.RING_OPEN, Math.hypot(start.x - FLOOR.x, start.z - FLOOR.z), 1.0E-6);
		assertTrue(start.distanceTo(from) < mark.distanceTo(from), "the rim the breath touches first is the near one");
		Vec3 direction = FireEaterOrdealGeometry.flat(mark.subtract(from));
		double reach = start.distanceTo(mark) + 0.1;
		assertTrue(FireEaterOrdealGeometry.inBreath(start, direction, reach, mark));
		assertFalse(FireEaterOrdealGeometry.inBreath(start, direction, reach - 1.0, mark), "the front has not reached them");
		assertFalse(FireEaterOrdealGeometry.inBreath(start, direction, reach, mark.add(0, 1.5, 0).add(-2.0, 0, 2.0)),
				"stepped out of the lane");
		assertTrue(FireEaterOrdealGeometry.inBreath(start, direction, reach, mark.add(0, 1.2, 0)), "a jump does not clear fire");
	}

	@Test
	void theFrontRunsAndStops() {
		assertEquals(0.0, FireEaterOrdealGeometry.breathFront(0));
		assertEquals(FireEaterOrdealGeometry.BREATH_SPEED * 3, FireEaterOrdealGeometry.breathFront(3), 1.0E-9);
		assertEquals(FireEaterOrdealGeometry.BREATH_LENGTH, FireEaterOrdealGeometry.breathFront(400));
	}

	@Test
	void embersFlankTheLeadInsideTheRing() {
		Vec3 lead = FireEaterOrdealGeometry.emberLead(new Vec3(7.5, 3.0, 0.5), new Vec3(0.3, 0, 0), FLOOR);
		assertTrue(Math.hypot(lead.x - FLOOR.x, lead.z - FLOOR.z) <= FireEaterOrdealGeometry.MARK_LIMIT + 1.0E-9, "kept in the ring");
		List<Vec3> marks = FireEaterOrdealGeometry.emberMarks(new Vec3(0.5, 3.0, 2.5), new Vec3(0, 0, 1), FLOOR);
		assertEquals(3, marks.size());
		assertEquals(FireEaterOrdealGeometry.EMBER_FLANK, Math.abs(marks.get(1).x - marks.get(0).x), 1.0E-9, "flanks lie across the line");
		assertTrue(FireEaterOrdealGeometry.emberHits(marks.get(0), marks.get(0).add(1.0, 0, 0)));
		assertFalse(FireEaterOrdealGeometry.emberHits(marks.get(0), marks.get(0).add(1.6, 0, 0)));
	}

	@Test
	void theRingClosesOnTheCentreAndThrowsInward() {
		assertEquals(FireEaterOrdealGeometry.RING_OPEN, FireEaterOrdealGeometry.ringRadius(0));
		assertEquals(FireEaterOrdealGeometry.RING_CLOSED, FireEaterOrdealGeometry.ringRadius(FireEaterOrdealGeometry.RING_CLOSE_TICKS));
		assertEquals(FireEaterOrdealGeometry.RING_CLOSED, FireEaterOrdealGeometry.ringRadius(500), "held, not closed further");
		Vec3 onWall = new Vec3(6.5, 3.0, 0.5);
		assertTrue(FireEaterOrdealGeometry.ringHits(FLOOR, 6.0, onWall));
		assertTrue(FireEaterOrdealGeometry.ringHits(FLOOR, 6.0, onWall.add(0, 1.2, 0)), "a wall of fire is not jumped");
		assertFalse(FireEaterOrdealGeometry.ringHits(FLOOR, 6.0, new Vec3(3.0, 3.0, 0.5)), "inside it");
		assertTrue(FireEaterOrdealGeometry.ringThrow(FLOOR, onWall).x < 0.0, "thrown in toward the centre");
	}

	@Test
	void theRainSpreadsOverTheRing() {
		List<Vec3> marks = FireEaterOrdealGeometry.rainMarks(FLOOR, 1234L);
		assertEquals(FireEaterOrdealGeometry.RAIN_MARKS, marks.size());
		for (int i = 0; i < marks.size(); i++) {
			assertTrue(Math.hypot(marks.get(i).x - FLOOR.x, marks.get(i).z - FLOOR.z) <= FireEaterOrdealGeometry.MARK_LIMIT + 1.0E-9);
			for (int j = 0; j < i; j++)
				assertTrue(marks.get(i).distanceTo(marks.get(j)) > FireEaterOrdealGeometry.RAIN_RADIUS, "no two on one spot");
		}
		assertEquals(marks, FireEaterOrdealGeometry.rainMarks(FLOOR, 1234L), "the same seed rains the same");
	}

	@Test
	void theBlastBurnsItsConeAndTheInhaleDrawsOnlyWhatIsInFront() {
		Vec3 aim = new Vec3(0, 0, 1);
		assertTrue(FireEaterOrdealGeometry.blastHits(FLOOR, aim, FLOOR.add(0, 0, 5.0)));
		assertTrue(FireEaterOrdealGeometry.blastHits(FLOOR, aim, FLOOR.add(2.0, 0, 4.0)), "inside 30 degrees of the aim");
		assertFalse(FireEaterOrdealGeometry.blastHits(FLOOR, aim, FLOOR.add(4.0, 0, 2.0)), "outside the cone");
		assertFalse(FireEaterOrdealGeometry.blastHits(FLOOR, aim, FLOOR.add(0, 0, 6.5)), "beyond its reach");
		Vec3 pull = FireEaterOrdealGeometry.inhalePull(FLOOR, aim, FLOOR.add(0, 0, 5.0));
		assertTrue(pull.z < 0.0, "drawn toward the mouth");
		assertEquals(Vec3.ZERO, FireEaterOrdealGeometry.inhalePull(FLOOR, aim, FLOOR.add(0, 0, -5.0)), "nothing behind it");
	}

	@Test
	void theVentBurnsCloseAndTheLashIsJumped() {
		assertTrue(FireEaterOrdealGeometry.ventHits(FLOOR, FLOOR.add(3.0, 0, 0)));
		assertFalse(FireEaterOrdealGeometry.ventHits(FLOOR, FLOOR.add(4.0, 0, 0)));
		double radius = FireEaterOrdealGeometry.lashRadius(3);
		Vec3 onLash = FLOOR.add(radius, 0, 0);
		assertTrue(FireEaterOrdealGeometry.lashHits(FLOOR, radius, onLash));
		assertFalse(FireEaterOrdealGeometry.lashHits(FLOOR, radius, onLash.add(0, 1.2, 0)), "jumping clears the coil");
		assertEquals(FireEaterOrdealGeometry.LASH_TO, FireEaterOrdealGeometry.lashRadius(99));
	}
}
