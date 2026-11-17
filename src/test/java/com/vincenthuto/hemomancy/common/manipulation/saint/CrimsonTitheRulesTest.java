package com.vincenthuto.hemomancy.common.manipulation.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** D-1 spec 3.1: the Tithe's vessel, leak, iron, draw and the Vessel Empties' lanes. */
class CrimsonTitheRulesTest {
	@Test
	void theVesselLeaksByHemorathsCurveAtAPlayersScale() {
		assertEquals(0.6D, CrimsonTitheRules.leak(0.0D), 1e-9);
		assertEquals(6.6D, CrimsonTitheRules.leak(1.0D), 1e-9);
		assertEquals(0.6D + 6.0D * 0.25D, CrimsonTitheRules.leak(0.5D), 1e-9);
		assertEquals(6.6D, CrimsonTitheRules.leak(3.0D), 1e-9, "fill is clamped");
	}

	@Test
	void everyHundredAndFiftyLeakedSetsOneIronUpToFour() {
		assertEquals(0, CrimsonTitheRules.iron(149));
		assertEquals(1, CrimsonTitheRules.iron(150));
		assertEquals(4, CrimsonTitheRules.iron(10_000));
	}

	@Test
	void theBodyStatesFollowTheFill() {
		assertTrue(CrimsonTitheRules.hollow(0.34D));
		assertFalse(CrimsonTitheRules.hollow(0.35D));
		assertFalse(CrimsonTitheRules.brimming(0.70D));
		assertTrue(CrimsonTitheRules.brimming(0.71D));
	}

	@Test
	void theDrawGivesLessFromTheBloodlessTheSearingAndBosses() {
		assertEquals(25.0D, CrimsonTitheRules.drawn(false, false, false), 1e-9);
		assertEquals(10.0D, CrimsonTitheRules.drawn(true, false, false), 1e-9);
		assertEquals(15.0D, CrimsonTitheRules.drawn(false, true, false), 1e-9);
		assertEquals(12.5D, CrimsonTitheRules.drawn(false, false, true), 1e-9);
		assertEquals(1000.0D, CrimsonTitheRules.pour(990.0D, 25.0D), 1e-9, "the vessel holds 1,000");
	}

	@Test
	void theCasterHealsOnePerFiftyDrawnAtMostTenAHold() {
		assertEquals(0.5D, CrimsonTitheRules.heal(25.0D, 0.0D), 1e-9);
		assertEquals(0.25D, CrimsonTitheRules.heal(25.0D, 9.75D), 1e-9);
		assertEquals(0.0D, CrimsonTitheRules.heal(25.0D, 10.0D), 1e-9);
	}

	@Test
	void theReleaseScalesWithFillAndIron() {
		assertEquals(6, CrimsonTitheRules.lanes(0));
		assertEquals(10, CrimsonTitheRules.lanes(4));
		assertEquals(10, CrimsonTitheRules.lanes(9));
		assertEquals(3.0D, CrimsonTitheRules.laneLength(0.0D), 1e-9);
		assertEquals(10.0D, CrimsonTitheRules.laneLength(1.0D), 1e-9);
		assertEquals(4.0F, CrimsonTitheRules.releaseDamage(0.0D), 1e-6);
		assertEquals(16.0F, CrimsonTitheRules.releaseDamage(1.0D), 1e-6);
		assertEquals(6.0D, CrimsonTitheRules.pressureBonus(3), 1e-9);
	}

	@Test
	void theLanesAreStruckAndTheWedgesBetweenThemAreSafe() {
		int lanes = 6;
		double start = 0.0D;
		assertTrue(CrimsonTitheRules.inLane(5.0D, 0.0D, start, lanes, 8.0D), "on the first lane");
		assertTrue(CrimsonTitheRules.inLane(5.0D, 0.5D, start, lanes, 8.0D), "inside its half-width");
		assertFalse(CrimsonTitheRules.inLane(9.0D, 0.0D, start, lanes, 8.0D), "past its length");
		double wedge = Math.PI / 6.0D;
		assertFalse(CrimsonTitheRules.inLane(5.0D * Math.cos(wedge), 5.0D * Math.sin(wedge), start, lanes, 8.0D),
				"halfway between two lanes is safe");
		double second = CrimsonTitheRules.laneAngle(start, 1, lanes);
		assertTrue(CrimsonTitheRules.inLane(4.0D * Math.cos(second), 4.0D * Math.sin(second), start, lanes, 8.0D));
	}

	@Test
	void theReleaseTimelineRunsEmergeSpikesFloodClose() {
		assertEquals(0.0D, CrimsonTitheRules.spikeRise(2.0D, 0.0D), 1e-9);
		assertEquals(1.0D, CrimsonTitheRules.spikeRise(12.0D, 1.0D), 1e-9);
		assertTrue(CrimsonTitheRules.spikeRise(5.0D, 0.0D) > CrimsonTitheRules.spikeRise(5.0D, 1.0D), "near spikes rise first");
		assertEquals(0.0D, CrimsonTitheRules.flood(9.0D), 1e-9);
		assertEquals(1.0D, CrimsonTitheRules.flood(20.0D), 1e-9);
		assertEquals(0.0D, CrimsonTitheRules.trenchOpen(50.0D), 1e-9);
		assertTrue(CrimsonTitheRules.flooding(10.0D) && !CrimsonTitheRules.flooding(30.0D));
	}
}
