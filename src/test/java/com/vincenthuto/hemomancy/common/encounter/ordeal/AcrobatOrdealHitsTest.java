package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Acrobat's rigging and the Vulture's moves hit what they mark and miss what reads them (2026-11-17). */
class AcrobatOrdealHitsTest {
	private static final double FLOOR = 3.0D;
	private static final Vec3 CENTRE = new Vec3(0.5, FLOOR, 0.5);
	private static final Vec3 EAST = new Vec3(1, 0, 0);

	@Test
	void theSwingIsAPendulumAcrossItsLane() {
		assertEquals(-AcrobatOrdealHits.SWING_HALF_LENGTH, AcrobatOrdealHits.swingAlong(0), 1.0E-9);
		assertEquals(0.0D, AcrobatOrdealHits.swingAlong(AcrobatOrdealHits.SWING_TICKS / 2.0D), 1.0E-9);
		assertEquals(AcrobatOrdealHits.SWING_HALF_LENGTH, AcrobatOrdealHits.swingAlong(AcrobatOrdealHits.SWING_TICKS), 1.0E-9);
		double early = AcrobatOrdealHits.swingAlong(1) - AcrobatOrdealHits.swingAlong(0);
		double middle = AcrobatOrdealHits.swingAlong(7.5) - AcrobatOrdealHits.swingAlong(6.5);
		assertTrue(middle > 3.0D * early, "fastest through the middle");
		assertTrue(AcrobatOrdealHits.barHeight(0) < AcrobatOrdealHits.barHeight(6), "lowest in the middle");
	}

	@Test
	void theSwingIsSidesteppedOrJumpedLowAndPassesOverHigh() {
		Vec3 onLane = new Vec3(0.5, FLOOR, 0.5);
		assertTrue(AcrobatOrdealHits.swingHits(CENTRE, EAST, 0.0D, FLOOR, onLane));
		assertFalse(AcrobatOrdealHits.swingHits(CENTRE, EAST, 0.0D, FLOOR, onLane.add(0, 0, 1.6)), "out of the lane");
		assertFalse(AcrobatOrdealHits.swingHits(CENTRE, EAST, 0.0D, FLOOR, onLane.add(0, 1.25, 0)), "jumped at its lowest");
		Vec3 nearEnd = new Vec3(7.5, FLOOR, 0.5);
		assertFalse(AcrobatOrdealHits.swingHits(CENTRE, EAST, 7.0D, FLOOR, nearEnd), "high over the head at the end");
	}

	@Test
	void theDropLandsOnItsLedMark() {
		Vec3 mark = AcrobatOrdealHits.lead(new Vec3(2.0, FLOOR, 2.0), new Vec3(0.25, 0, 0), 6.0D, FLOOR);
		assertEquals(3.5D, mark.x, 1.0E-9);
		assertTrue(AcrobatOrdealHits.discHits(mark, AcrobatOrdealHits.DROP_RADIUS, new Vec3(4.5, FLOOR, 2.5)));
		assertFalse(AcrobatOrdealHits.discHits(mark, AcrobatOrdealHits.DROP_RADIUS, new Vec3(6.0, FLOOR, 2.0)));
		assertTrue(AcrobatOrdealHits.dropHeight(2) > AcrobatOrdealHits.DROP_HEIGHT - 3.0D, "it hangs in the dark");
		assertEquals(0.0D, AcrobatOrdealHits.dropHeight(AcrobatOrdealHits.DROP_WINDUP), 1.0E-9);
		double last = AcrobatOrdealHits.dropHeight(AcrobatOrdealHits.DROP_WINDUP - 1) - AcrobatOrdealHits.dropHeight(AcrobatOrdealHits.DROP_WINDUP);
		double first = AcrobatOrdealHits.dropHeight(AcrobatOrdealHits.DROP_WINDUP - 4) - AcrobatOrdealHits.dropHeight(AcrobatOrdealHits.DROP_WINDUP - 3);
		assertTrue(last > first, "the plummet accelerates");
	}

	@Test
	void theSnareYanksUpTowardTheAcrobat() {
		Vec3 yank = AcrobatOrdealHits.snareYank(new Vec3(0, FLOOR, 0), new Vec3(10, FLOOR + 9, 0));
		assertTrue(yank.x > 1.0D && yank.y > 1.0D && Math.abs(yank.z) < 1.0E-9);
	}

	@Test
	void theVultureStoopsAcceleratingAndRakesInAStraightLine() {
		assertEquals(AcrobatOrdealHits.STOOP_HEIGHT, AcrobatOrdealHits.stoopHeight(0), 1.0E-9);
		assertEquals(0.0D, AcrobatOrdealHits.stoopHeight(5), 1.0E-9);
		assertTrue(AcrobatOrdealHits.stoopHeight(4) - AcrobatOrdealHits.stoopHeight(5)
				> AcrobatOrdealHits.stoopHeight(0) - AcrobatOrdealHits.stoopHeight(1));
		Vec3 start = new Vec3(0, FLOOR + 1, 0);
		assertEquals(AcrobatOrdealHits.RAKE_SPEED * 4, AcrobatOrdealHits.rakeAt(start, new Vec3(0, 0, 3), 4).z, 1.0E-9);
		assertTrue(AcrobatOrdealHits.insideRing(CENTRE, new Vec3(7.0, FLOOR, 0.5), 1.2D));
		assertFalse(AcrobatOrdealHits.insideRing(CENTRE, new Vec3(9.0, FLOOR, 0.5), 1.2D), "the pass stops at the curb");
	}

	@Test
	void theDowndraftPressesDownAndOut() {
		Vec3 shove = AcrobatOrdealHits.downdraftShove(new Vec3(0, FLOOR, 0), new Vec3(0, FLOOR, 2));
		assertTrue(shove.z > 0.9D && shove.y < 0.0D);
	}
}
