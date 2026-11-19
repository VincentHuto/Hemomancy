package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Beast Tamer's interventions and the Sanguine Hound's moves hit what they mark and miss what reads them (2026-11-17). */
class BeastTamerMovesTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 3.0, 0.5);

	@Test
	void kindsStayInTheBeastTamersBlock() {
		for (int kind : new int[]{BeastTamerMoves.KIND_CRACK, BeastTamerMoves.KIND_HOOP, BeastTamerMoves.KIND_GOAD,
				BeastTamerMoves.KIND_BEAST, BeastTamerMoves.KIND_LUNGE, BeastTamerMoves.KIND_POUNCE, BeastTamerMoves.KIND_SHAKE,
				BeastTamerMoves.KIND_HOWL})
			assertTrue(kind >= OrdealHazardKinds.BEAST_TAMER && kind < OrdealHazardKinds.BEAST_TAMER + 10, "kind " + kind);
	}

	@Test
	void whipcrackStartsAtTheRimAndIsSidestepped() {
		Vec3 tamer = new Vec3(14.0, 2.0, 0.5);
		Vec3 mark = new Vec3(0.5, 3.0, 0.5);
		Vec3 from = BeastTamerMoves.crackFrom(FLOOR, tamer, mark, OrdealStage.RING_RADIUS);
		assertEquals(OrdealStage.RING_RADIUS, BeastTamerMoves.horizontal(from, FLOOR), 1.0E-6, "the lane begins on the rim");
		Vec3 dir = BeastTamerMoves.flat(mark.subtract(tamer));
		double length = BeastTamerMoves.crackLength(from, mark);
		assertTrue(BeastTamerMoves.laneHits(from, dir, length, BeastTamerMoves.CRACK_HALF_WIDTH, mark));
		assertTrue(BeastTamerMoves.laneHits(from, dir, length, BeastTamerMoves.CRACK_HALF_WIDTH, mark.add(0, 1.2, 0)),
				"a jump does not clear the lash");
		assertFalse(BeastTamerMoves.laneHits(from, dir, length, BeastTamerMoves.CRACK_HALF_WIDTH, mark.add(0, 0, 1.5)),
				"a step aside does");
		assertTrue(BeastTamerMoves.laneHits(from, dir, length, BeastTamerMoves.CRACK_HALF_WIDTH,
				mark.add(-BeastTamerMoves.CRACK_OVERSHOOT + .5, 0, 0)), "it runs on past the mark");
	}

	@Test
	void hoopIsLeaptAndItsInsideBurns() {
		Vec3 mark = new Vec3(2.5, 3.0, 2.5);
		Vec3 onBand = mark.add(BeastTamerMoves.HOOP_RADIUS, 0, 0);
		assertTrue(BeastTamerMoves.hoopBandHits(mark, onBand));
		assertFalse(BeastTamerMoves.hoopBandHits(mark, onBand.add(0, 1.2, 0)), "leaping through the hoop clears it");
		assertTrue(BeastTamerMoves.hoopInsideHits(mark, mark.add(.5, 0, .5)));
		assertFalse(BeastTamerMoves.hoopInsideHits(mark, mark.add(BeastTamerMoves.HOOP_RADIUS + 1, 0, 0)), "outside the hoop");
	}

	@Test
	void beastsComeFromThreeSidesAndAgreeOnFloats() {
		Vec3 mark = new Vec3(1.25, 3.0, -2.75);
		for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
			Vec3 from = BeastTamerMoves.beastFrom(mark, i);
			assertEquals(BeastTamerMoves.BEAST_FROM, BeastTamerMoves.horizontal(from, mark), 1.0E-6);
			Vec3 end = BeastTamerMoves.beastAt(mark, i, BeastTamerMoves.BEAST_RUN);
			assertEquals(BeastTamerMoves.BEAST_PAST, BeastTamerMoves.horizontal(end, mark), 1.0E-6, "they run on past the mark");
			assertTrue(BeastTamerMoves.beastHits(mark, i, BeastTamerMoves.BEAST_RUN * BeastTamerMoves.BEAST_FROM
					/ (BeastTamerMoves.BEAST_FROM + BeastTamerMoves.BEAST_PAST), mark), "beast " + i + " reaches the mark");
		}
		Vec3 a = BeastTamerMoves.beastFrom(mark, 0), b = BeastTamerMoves.beastFrom(mark, 1);
		assertTrue(BeastTamerMoves.horizontal(a, b) > BeastTamerMoves.BEAST_FROM, "the beasts are spread round the mark");
		// The client receives the mark as floats: the lanes must come out the same.
		Vec3 received = new Vec3(mark.toVector3f());
		assertEquals(BeastTamerMoves.beastTurn(mark), BeastTamerMoves.beastTurn(received), 1.0E-9);
		assertTrue(BeastTamerMoves.beastLands(1) > BeastTamerMoves.beastLands(0));
	}

	@Test
	void houndsLungeStopsAtTheCurbAndItsPounceArcs() {
		double limit = OrdealStage.RING_RADIUS - 1.0;
		assertTrue(BeastTamerMoves.lungeStops(FLOOR.add(limit + .1, 0, 0), FLOOR, limit));
		assertFalse(BeastTamerMoves.lungeStops(FLOOR.add(limit - .5, 0, 0), FLOOR, limit));
		Vec3 from = FLOOR, to = FLOOR.add(4, 0, 0);
		assertEquals(BeastTamerMoves.POUNCE_HEIGHT, BeastTamerMoves.pounceArc(from, to, .5).y - FLOOR.y, 1.0E-9);
		assertEquals(to, BeastTamerMoves.pounceArc(from, to, 1.0));
		Vec3 landing = BeastTamerMoves.pounceMark(FLOOR, FLOOR.add(20, 0, 0), Vec3.ZERO, FLOOR, limit);
		assertTrue(BeastTamerMoves.horizontal(landing, FLOOR) <= limit + 1.0E-9, "it never lands past the curb");
	}

	@Test
	void howlIsJumped() {
		double wave = BeastTamerMoves.howlRadius(BeastTamerMoves.HOWL_WAVE / 2);
		Vec3 onWave = FLOOR.add(wave, 0, 0);
		assertTrue(BeastTamerMoves.howlHits(FLOOR, wave, onWave));
		assertFalse(BeastTamerMoves.howlHits(FLOOR, wave, onWave.add(0, 1.2, 0)), "jumping clears it");
		assertEquals(BeastTamerMoves.HOWL_RADIUS, BeastTamerMoves.howlRadius(BeastTamerMoves.HOWL_WAVE), 1.0E-9);
	}
}
