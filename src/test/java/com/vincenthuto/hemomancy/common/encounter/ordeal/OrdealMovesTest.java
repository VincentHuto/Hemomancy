package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Strongman's interventions hit what they mark and miss what reads them (2026-11-17). */
class OrdealMovesTest {
	private static final Vec3 STAGE = new Vec3(0.5, 3.0, 0.5);

	@Test
	void droppedWeightHitsItsCircle() {
		Vec3 mark = new Vec3(3.5, 3.0, 0.5);
		assertTrue(OrdealMoves.droppedWeightHits(mark, new Vec3(4.5, 3.0, 1.0)));
		assertFalse(OrdealMoves.droppedWeightHits(mark, new Vec3(6.6, 3.0, 0.5)), "a block outside the circle");
	}

	@Test
	void groundHeaveIsJumped() {
		double wave = 5.0;
		Vec3 onWave = new Vec3(5.5, 3.0, 0.5);
		assertTrue(OrdealMoves.groundHeaveHits(STAGE, wave, onWave));
		assertFalse(OrdealMoves.groundHeaveHits(STAGE, wave, onWave.add(0, 1.2, 0)), "jumping clears it");
		assertFalse(OrdealMoves.groundHeaveHits(STAGE, wave, new Vec3(2.5, 3.0, 0.5)), "inside the wave");
	}

	@Test
	void barSweepIsJumped() {
		double along = 2.0;
		Vec3 onBar = new Vec3(2.5, 3.0, 4.0);
		assertTrue(OrdealMoves.barSweepHits(STAGE, along, onBar));
		assertFalse(OrdealMoves.barSweepHits(STAGE, along, onBar.add(0, 1.2, 0)), "a jumping player clears the bar");
		assertFalse(OrdealMoves.barSweepHits(STAGE, along, new Vec3(4.0, 3.0, 4.0)), "beside the bar");
	}

	@Test
	void holdTheLinePullsTowardThePuppet() {
		Vec3 pull = OrdealMoves.holdTheLinePull(new Vec3(0, 3, 6), new Vec3(0, 3, -4));
		assertEquals(0.0, pull.x, 1.0E-9);
		assertTrue(pull.z < 0);
		assertEquals(OrdealMoves.PULL, pull.length(), 1.0E-9);
	}

	@Test
	void sweepAndHeaveRunTheirCourse() {
		assertEquals(OrdealMoves.HEAVE_START, OrdealMoves.heaveRadius(0), 1.0E-9);
		assertTrue(OrdealMoves.heaveRadius(30) < OrdealMoves.heaveRadius(0));
		assertTrue(OrdealMoves.sweepOffset(0) < 0 && OrdealMoves.sweepOffset(OrdealMoves.SWEEP_TICKS) > 0);
	}
}
