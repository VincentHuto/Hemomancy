package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Retired Understudy's interventions and the Mnemonist Puppet's moves hit what they mark (2026-11-17). */
class UnderstudyOrdealGeometryTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 3.0, 0.5);

	@Test
	void spotlightHuntsAtItsOwnPaceAndBurnsItsPool() {
		Vec3 light = new Vec3(0.5, 3.0, 0.5);
		Vec3 step = UnderstudyOrdealGeometry.spotlightStep(light, new Vec3(6.5, 3.0, 0.5), FLOOR);
		assertEquals(UnderstudyOrdealGeometry.SPOT_SPEED, step.x - light.x, 1.0E-9, "it follows no faster than its pace");
		Vec3 caught = UnderstudyOrdealGeometry.spotlightStep(light, new Vec3(0.6, 3.0, 0.5), FLOOR);
		assertEquals(0.6, caught.x, 1.0E-9, "within a step it settles on the fighter");
		assertTrue(UnderstudyOrdealGeometry.spotlightHits(light, new Vec3(1.5, 3.0, 1.0)));
		assertFalse(UnderstudyOrdealGeometry.spotlightHits(light, new Vec3(3.0, 3.0, 0.5)), "outside the pool");
	}

	@Test
	void spotlightStaysInsideTheRing() {
		Vec3 far = UnderstudyOrdealGeometry.spotlightStep(new Vec3(8.4, 3.0, 0.5), new Vec3(20.0, 3.0, 0.5), FLOOR);
		assertTrue(UnderstudyOrdealGeometry.horizontal(far, FLOOR) <= UnderstudyOrdealGeometry.RING - 1.0 + 1.0E-9);
	}

	@Test
	void curtainIsAWallWithAParting() {
		double along = UnderstudyOrdealGeometry.curtainOffset(UnderstudyOrdealGeometry.CURTAIN_TICKS / 2);
		assertEquals(0.0, along, 1.0E-9, "half-way through its sweep it crosses the centre");
		double parting = 2.0;
		Vec3 onLine = new Vec3(-3.5, 3.0, 0.5 + along);
		assertTrue(UnderstudyOrdealGeometry.curtainHits(FLOOR, along, parting, onLine));
		assertTrue(UnderstudyOrdealGeometry.curtainHits(FLOOR, along, parting, onLine.add(0, 1.4, 0)), "no jump clears it");
		assertFalse(UnderstudyOrdealGeometry.curtainHits(FLOOR, along, parting, new Vec3(2.7, 3.0, 0.5)),
				"standing in the parting");
		assertFalse(UnderstudyOrdealGeometry.curtainHits(FLOOR, along, parting, new Vec3(-3.5, 3.0, 2.5)), "beside it");
	}

	@Test
	void partingAndHalvesMeetAtTheRim() {
		assertEquals(-UnderstudyOrdealGeometry.PARTING_SPAN, UnderstudyOrdealGeometry.parting(0.0), 1.0E-9);
		double[][] halves = UnderstudyOrdealGeometry.curtainHalves(1.0);
		assertEquals(-UnderstudyOrdealGeometry.CURTAIN_REACH, halves[0][0], 1.0E-9);
		assertEquals(1.0 - UnderstudyOrdealGeometry.PARTING_HALF, halves[0][1], 1.0E-9);
		assertEquals(1.0 + UnderstudyOrdealGeometry.PARTING_HALF, halves[1][0], 1.0E-9);
	}

	@Test
	void trapdoorsOpenUnderTheFighterAndRoundThem() {
		List<Vec3> doors = UnderstudyOrdealGeometry.trapdoors(new Vec3(2.5, 3.0, 0.5), FLOOR, 0.0);
		assertEquals(4, doors.size());
		assertTrue(UnderstudyOrdealGeometry.trapdoorHits(doors.get(0), new Vec3(2.5, 3.0, 0.5)));
		assertTrue(UnderstudyOrdealGeometry.trapdoorHits(doors.get(0), new Vec3(2.5, 4.2, 0.5)), "a jump lands back in it");
		for (Vec3 door : doors)
			assertTrue(UnderstudyOrdealGeometry.horizontal(door, FLOOR) <= UnderstudyOrdealGeometry.RING
					- UnderstudyOrdealGeometry.TRAP_RADIUS + 1.0E-9, "every door is inside the ring");
		assertFalse(UnderstudyOrdealGeometry.trapdoorHits(doors.get(0), new Vec3(4.6, 3.0, 2.8)), "between the doors");
	}

	@Test
	void recitalWalksForward() {
		Vec3 feet = new Vec3(0.5, 3.0, 0.5), aim = new Vec3(0, 0, 1);
		Vec3 last = UnderstudyOrdealGeometry.recitalBlow(feet, aim, 2);
		assertEquals(0.5 + UnderstudyOrdealGeometry.RECITAL_FIRST + 2 * UnderstudyOrdealGeometry.RECITAL_STEP, last.z, 1.0E-9);
		assertTrue(UnderstudyOrdealGeometry.recitalHits(last, last.add(1.0, 0, 0)));
		assertFalse(UnderstudyOrdealGeometry.recitalHits(last, last.add(2.0, 0, 0)));
	}

	@Test
	void threadsFanAndCatchAlongTheirLength() {
		Vec3 aim = new Vec3(1, 0, 0);
		List<Vec3> threads = UnderstudyOrdealGeometry.threads(aim);
		assertEquals(3, threads.size());
		assertTrue(UnderstudyOrdealGeometry.threadHits(FLOOR, threads.get(1), new Vec3(5.5, 3.0, 0.9)));
		assertFalse(UnderstudyOrdealGeometry.threadHits(FLOOR, threads.get(1), new Vec3(8.5, 3.0, 0.5)), "past its length");
		assertFalse(UnderstudyOrdealGeometry.threadHits(FLOOR, threads.get(1), new Vec3(-2.5, 3.0, 0.5)), "behind the puppet");
		Vec3 side = FLOOR.add(threads.get(0).scale(5.0));
		assertTrue(UnderstudyOrdealGeometry.threadHits(FLOOR, threads.get(0), side));
		assertFalse(UnderstudyOrdealGeometry.threadHits(FLOOR, threads.get(1), side), "between two threads");
	}

	@Test
	void echoIsJumped() {
		double wave = UnderstudyOrdealGeometry.echoRadius(UnderstudyOrdealGeometry.ECHO_TRAVEL);
		assertEquals(UnderstudyOrdealGeometry.ECHO_RADIUS, wave, 1.0E-9);
		Vec3 onWave = new Vec3(6.5, 3.0, 0.5);
		assertTrue(UnderstudyOrdealGeometry.echoHits(FLOOR, wave, onWave));
		assertFalse(UnderstudyOrdealGeometry.echoHits(FLOOR, wave, onWave.add(0, 1.2, 0)), "jumping clears it");
	}

	@Test
	void lungeStopsAtTheCurb() {
		Vec3 near = UnderstudyOrdealGeometry.lungeStep(new Vec3(7.5, 3.0, 0.5), new Vec3(1, 0, 0), FLOOR, 7.5);
		assertEquals(null, near, "the next step would cross the curb");
		Vec3 inside = UnderstudyOrdealGeometry.lungeStep(FLOOR, new Vec3(1, 0, 0), FLOOR, 8.0);
		assertEquals(0.5 + UnderstudyOrdealGeometry.LUNGE_SPEED, inside.x, 1.0E-9);
	}
}
