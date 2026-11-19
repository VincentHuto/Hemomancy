package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Stilt Walker's interventions and the Scarlet Mummer's moves hit what they draw and miss what reads them. */
class StiltWalkerMovesTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 3.0, 0.5);

	@Test
	void kindsStayInTheStiltWalkersBlock() {
		for (int kind : new int[] { StiltWalkerMoves.KIND_PLANT, StiltWalkerMoves.KIND_STRIDE, StiltWalkerMoves.KIND_SPIN,
				StiltWalkerMoves.KIND_SHADOW, StiltWalkerMoves.KIND_DAZZLE, StiltWalkerMoves.KIND_FEINT,
				StiltWalkerMoves.KIND_WHIRL, StiltWalkerMoves.KIND_ENTRANCE })
			assertTrue(kind >= OrdealHazardKinds.STILT_WALKER && kind < OrdealHazardKinds.STILT_WALKER + 10, "kind " + kind);
	}

	@Test
	void plantedStiltHitsItsMark() {
		Vec3 mark = new Vec3(3.5, 3.0, 0.5);
		assertTrue(StiltWalkerMoves.plantHits(mark, new Vec3(4.5, 3.0, 1.0)));
		assertFalse(StiltWalkerMoves.plantHits(mark, new Vec3(5.5, 3.0, 0.5)), "two blocks off the mark");
	}

	@Test
	void strideWalksTheChordThroughTheFighterAndStaysInTheRing() {
		Vec3 rim = StiltWalkerMoves.rimToward(FLOOR, FLOOR.add(15, 0, 0));
		Vec3 fighter = FLOOR.add(2, 0, 3);
		Vec3[] chord = StiltWalkerMoves.strideChord(FLOOR, rim, fighter);
		Vec3 end = chord[0].add(chord[1]);
		assertEquals(StiltWalkerMoves.RIM, Math.hypot(end.x - FLOOR.x, end.z - FLOOR.z), 1.0E-6, "the chord ends on the far rim");
		// The fighter stands on the chord.
		Vec3 dir = chord[1].normalize();
		double across = Math.abs((fighter.x - rim.x) * dir.z - (fighter.z - rim.z) * dir.x);
		assertEquals(0.0, across, 1.0E-6);
		for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++) {
			Vec3 step = StiltWalkerMoves.strideStep(chord[0], chord[1], k);
			assertTrue(Math.hypot(step.x - FLOOR.x, step.z - FLOOR.z) < StiltWalkerMoves.RIM, "footfall " + k + " inside");
			assertTrue(StiltWalkerMoves.strideHits(step, step));
		}
		assertTrue(StiltWalkerMoves.strideLands(1) > StiltWalkerMoves.strideLands(0), "the footfalls land one after another");
	}

	@Test
	void strideOnTheRimCrossesTheCentre() {
		Vec3 rim = StiltWalkerMoves.rimToward(FLOOR, FLOOR.add(0, 0, 15));
		Vec3[] chord = StiltWalkerMoves.strideChord(FLOOR, rim, rim);
		assertEquals(2.0 * StiltWalkerMoves.RIM, chord[1].length(), 1.0E-6);
	}

	@Test
	void spinSweepsThroughTheFighterHalfwayAndIsJumped() {
		Vec3 fighter = FLOOR.add(4, 0, 4);
		double start = StiltWalkerMoves.spinStart(FLOOR, fighter);
		assertFalse(StiltWalkerMoves.spinHits(FLOOR, StiltWalkerMoves.spinAngle(start, 0), fighter), "it starts a quarter away");
		double half = StiltWalkerMoves.spinAngle(start, StiltWalkerMoves.SPIN_TICKS / 2.0);
		assertTrue(StiltWalkerMoves.spinHits(FLOOR, half, fighter));
		assertTrue(StiltWalkerMoves.spinHits(FLOOR, half, FLOOR.add(-4, 0, -4)), "both stilts sweep");
		assertFalse(StiltWalkerMoves.spinHits(FLOOR, half, fighter.add(0, 1.2, 0)), "jumping clears it");
		assertEquals(start + Math.PI, StiltWalkerMoves.spinAngle(start, StiltWalkerMoves.SPIN_TICKS), 1.0E-6, "half a circle");
	}

	@Test
	void shadowFallsDownItsLaneAndASidestepClearsIt() {
		Vec3 foot = FLOOR.add(-9.5, 0, 0);
		Vec3 aim = StiltWalkerMoves.shadowAim(foot, FLOOR.add(2, 0, 0), FLOOR);
		assertTrue(StiltWalkerMoves.shadowHits(foot, aim, FLOOR.add(2, 0, 1)));
		assertFalse(StiltWalkerMoves.shadowHits(foot, aim, FLOOR.add(2, 0, 2)), "two blocks aside");
		assertFalse(StiltWalkerMoves.shadowHits(foot, aim, FLOOR.add(6, 0, 0)), "past its length");
		assertEquals(0.0F, StiltWalkerMoves.shadowFall(0), 1.0E-6F);
		assertEquals((float) (Math.PI / 2.0), StiltWalkerMoves.shadowFall(StiltWalkerMoves.SHADOW_WINDUP), 1.0E-5F);
		assertTrue(StiltWalkerMoves.shadowFall(10) < StiltWalkerMoves.shadowFall(20) / 4.0F, "slow to start, fast to land");
	}

	@Test
	void dazzleCatchesOnlyAFighterLookingAtIt() {
		Vec3 mummer = FLOOR.add(0, 2.4, 0);
		Vec3 eyes = FLOOR.add(4, 1.6, 0);
		assertTrue(StiltWalkerMoves.dazzleHits(mummer, eyes, new Vec3(-1, 0.1, 0)));
		assertFalse(StiltWalkerMoves.dazzleHits(mummer, eyes, new Vec3(1, 0, 0)), "looking away");
		assertFalse(StiltWalkerMoves.dazzleHits(mummer, FLOOR.add(8, 1.6, 0), new Vec3(-1, 0, 0)), "out of reach");
	}

	@Test
	void mummerMovesHitTheirShapes() {
		Vec3 start = FLOOR.add(1, 0, 0);
		Vec3 aim = new Vec3(0, 0, 1);
		assertTrue(StiltWalkerMoves.feintHits(start, aim, start.add(0.5, 0, 3)));
		assertFalse(StiltWalkerMoves.feintHits(start, aim, start.add(1.5, 0, 3)), "beside the lunge");
		assertTrue(StiltWalkerMoves.whirlHits(FLOOR, FLOOR.add(2.5, 0, 0)));
		assertFalse(StiltWalkerMoves.whirlHits(FLOOR, FLOOR.add(3.5, 0, 0)));
		Vec3 mark = FLOOR.add(5, 0, 0);
		assertTrue(StiltWalkerMoves.entranceHits(mark, mark.add(2, 0, 0)));
		assertFalse(StiltWalkerMoves.entranceHits(mark, mark.add(3, 0, 0)));
		assertEquals(mark.x, StiltWalkerMoves.entranceArc(FLOOR, mark, 1.0).x, 1.0E-6, "the leap lands on its mark");
		assertTrue(StiltWalkerMoves.entranceArc(FLOOR, mark, 0.5).y > FLOOR.y + 3.0, "and arcs high");
	}

	@Test
	void marksAreKeptInsideTheRing() {
		Vec3 kept = StiltWalkerMoves.inside(FLOOR, FLOOR.add(20, 5, 0), 0.8);
		assertEquals(StiltWalkerMoves.RIM - 0.8, kept.x - FLOOR.x, 1.0E-6);
		assertEquals(FLOOR.y, kept.y, 1.0E-6);
	}

	@Test
	void theWalkStaysOnThePromenadeAndOutOfReach() {
		for (double s = 0; s < 40; s += 0.37) {
			Vec3 at = OrdealStiltWalk.position(FLOOR, s);
			assertEquals(OrdealStiltWalk.RADIUS, Math.hypot(at.x - FLOOR.x, at.z - FLOOR.z), 1.0E-6);
		}
		assertTrue(OrdealStiltWalk.RADIUS > OrdealStage.RING_RADIUS + 4.0, "past the curb and the apron");
	}
}
