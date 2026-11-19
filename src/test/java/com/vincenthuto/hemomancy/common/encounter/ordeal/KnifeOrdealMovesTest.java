package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Dagger Thrower's blades hit what they mark and miss what reads them (2026-11-17). */
class KnifeOrdealMovesTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 3.0, 0.5);

	@Test
	void theyPaceTheGalleryOutOfReach() {
		for (int t = 0; t < 1600; t += 50) {
			Vec3 at = KnifeOrdealMoves.professorAt(FLOOR, t);
			assertEquals(KnifeOrdealMoves.GALLERY_RADIUS, KnifeOrdealMoves.horizontal(FLOOR, at), 1.0E-6);
			assertTrue(at.z < FLOOR.z, "always on the north side");
			assertTrue(KnifeOrdealMoves.horizontal(FLOOR, at) > OrdealStage.RING_RADIUS + 4.0D);
		}
	}

	@Test
	void theFanIsDodgedBetweenItsLanes() {
		Vec3 origin = new Vec3(0.5, 3.0, -14.5), aim = FLOOR;
		assertTrue(KnifeOrdealMoves.fanHits(origin, aim, FLOOR, FLOOR.y), "the middle lane runs at the mark");
		// Between the middle lane and the next, 15 blocks out: 11 degrees apart is about 2.9 blocks.
		Vec3 between = new Vec3(0.5 + 15.0 * Math.tan(Math.toRadians(5.5)), 3.0, 0.5);
		assertFalse(KnifeOrdealMoves.fanHits(origin, aim, between, FLOOR.y));
		assertFalse(KnifeOrdealMoves.fanHits(origin, aim, FLOOR.add(0, 3.0, 0), FLOOR.y), "far above the blades");
		assertTrue(KnifeOrdealMoves.fanHits(origin, aim, FLOOR.add(0, 1.2, 0), FLOOR.y), "a jump does not clear a knife");
	}

	@Test
	void aLaneCrossesTheRingFromCurbToCurb() {
		Vec3 origin = new Vec3(0.5, 3.0, -14.5);
		double[] span = KnifeOrdealMoves.span(origin, new Vec3(0, 0, 1), FLOOR, 9.5);
		assertNotNull(span);
		assertEquals(5.5, span[0], 1.0E-6);
		assertEquals(24.5, span[1], 1.0E-6);
		assertNull(KnifeOrdealMoves.span(origin, new Vec3(1, 0, 0), FLOOR, 9.5), "a lane wide of the ring");
	}

	@Test
	void theClosingRingIsLeftOrJumped() {
		Vec3 onRing = FLOOR.add(KnifeOrdealMoves.RING_RADIUS, 0, 0);
		assertTrue(KnifeOrdealMoves.ringHits(FLOOR, KnifeOrdealMoves.ringRadius(0), onRing));
		assertFalse(KnifeOrdealMoves.ringHits(FLOOR, KnifeOrdealMoves.ringRadius(0), onRing.add(0, 1.2, 0)), "jumped");
		assertFalse(KnifeOrdealMoves.ringHits(FLOOR, KnifeOrdealMoves.ringRadius(0), FLOOR.add(6.5, 0, 0)), "outside it");
		assertEquals(0.0, KnifeOrdealMoves.ringRadius(KnifeOrdealMoves.RING_CLOSE), 1.0E-6);
		assertTrue(KnifeOrdealMoves.ringHits(FLOOR, KnifeOrdealMoves.ringRadius(KnifeOrdealMoves.RING_CLOSE), FLOOR),
				"it meets in the middle");
	}

	@Test
	void theBoardOutlinesTheFighterAndTheBullseyeLandsLast() {
		for (int k = 0; k < KnifeOrdealMoves.BOARD_KNIVES; k++) {
			Vec3 spot = KnifeOrdealMoves.boardSpot(FLOOR, k);
			assertEquals(KnifeOrdealMoves.BOARD_OUTLINE, KnifeOrdealMoves.horizontal(FLOOR, spot), 1.0E-6);
			assertFalse(KnifeOrdealMoves.spotHits(spot, KnifeOrdealMoves.BOARD_SPOT, FLOOR), "standing still misses the outline");
			assertTrue(KnifeOrdealMoves.boardLanding(k) < KnifeOrdealMoves.BULLSEYE_AT);
		}
		assertTrue(KnifeOrdealMoves.spotHits(FLOOR, KnifeOrdealMoves.BULLSEYE, FLOOR));
		assertFalse(KnifeOrdealMoves.spotHits(FLOOR, KnifeOrdealMoves.BULLSEYE, FLOOR.add(1.5, 0, 0)), "stepped off it");
	}

	@Test
	void theVolleyRunsDownItsLane() {
		Vec3 from = new Vec3(0.5, 3.0, -5.5), dir = new Vec3(0, 0, 1);
		double length = MarrowJugglerOrdealMoves.VOLLEY_LENGTH, half = MarrowJugglerOrdealMoves.VOLLEY_HALF_WIDTH;
		assertTrue(KnifeOrdealMoves.laneHits(from, dir, length, half, FLOOR, FLOOR.y, 2.4));
		assertFalse(KnifeOrdealMoves.laneHits(from, dir, length, half, FLOOR.add(1.5, 0, 0), FLOOR.y, 2.4), "stepped aside");
		assertFalse(KnifeOrdealMoves.laneHits(from, dir, length, half, from.add(0, 0, -1), FLOOR.y, 2.4), "behind the thrower");
	}
}
