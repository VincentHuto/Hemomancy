package com.vincenthuto.hemomancy.client.render.world.rite;

import static com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CardinalRiteVisualRulesTest {
	@Test
	void offeringRidesTheVesselFromBrazierToDaemonAndDissolvesOnArrival() {
		assertEquals(0, offeringReach(0), 1e-9);
		assertEquals(1, offeringReach(OFFERING_EXTEND_TICKS), 1e-9);
		assertEquals(0, offeringTravel(OFFERING_DEPART_TICKS), 1e-9);
		assertEquals(1, offeringTravel(OFFERING_ARRIVE_TICKS), 1e-9);
		assertEquals(0, offeringItemScale(OFFERING_ARRIVE_TICKS), 1e-9, "the item is gone once it arrives");
		assertEquals(1, offeringItemScale((OFFERING_DEPART_TICKS + OFFERING_ARRIVE_TICKS) / 2), 1e-9);
		assertEquals(0, offeringTail(OFFERING_ARRIVE_TICKS), 1e-9, "the vessel holds until the item arrives");
		assertEquals(1, offeringTail(OFFERING_RETRACT_END_TICKS), 1e-9);
		assertTrue(offeringArrival(OFFERING_ARRIVE_TICKS + 1) > 0.5);
		assertEquals(0, offeringArrival(OFFERING_TOTAL_TICKS), 1e-9);
		assertFalse(offeringVisible(OFFERING_TOTAL_TICKS));
		double previous = -1;
		for (float age = 0; age <= OFFERING_ARRIVE_TICKS; age += 0.5F) {
			double travel = offeringTravel(age);
			assertTrue(travel >= previous, "travel never runs backward");
			previous = travel;
		}
	}

	@Test
	void offeringArcLeavesTheBrazierRunsLowAndEndsInTheDaemon() {
		double[] out = new double[3];
		double ax = 4, ay = 66, az = 0, bx = 0, by = 68.5, bz = 0, floor = 65;
		offeringArc(ax, ay, az, bx, by, bz, floor, 0, out);
		assertArrayEquals(new double[] { ax, ay, az }, out, 1e-9);
		offeringArc(ax, ay, az, bx, by, bz, floor, 1, out);
		assertArrayEquals(new double[] { bx, by, bz }, out, 1e-9);
		offeringArc(ax, ay, az, bx, by, bz, floor, 0.35, out);
		assertTrue(out[1] < ay, "the vessel dips toward the floor: " + out[1]);
		assertTrue(out[1] > floor, "but stays above it: " + out[1]);
	}

	@Test
	void daemonSilhouetteSwellsToTheShouldersAndPinchesAtTheNeck() {
		assertTrue(contour(0.02) < contour(0.5));
		assertEquals(1.0, contour(0.72), 1e-9);
		assertTrue(contour(0.77) < contour(0.72));
		assertTrue(contour(0.89) > contour(0.78), "the head is wider than the neck");
		for (double h = 0; h <= 1.0; h += 0.01) assertTrue(contour(h) > 0 && contour(h) <= 1.0);
		for (int i = 0; i < CardinalRiteVisualRules.MAX_NODULES; i++) {
			double h = noduleHeight(i);
			assertTrue(h > 0.3 && h < 0.7, "nodules sit in the torso");
			assertTrue(noduleRadius(i) < contour(h), "nodules sit inside the silhouette");
		}
		assertEquals(1.0, firmness(-1, true), 1e-9);
		assertTrue(firmness(-1, false) < 1.0);
		assertEquals(1.0, firmness(SEAL_FIRM_TICKS, false), 1e-9);
	}

	@Test
	void heartbeatPhaseWrapsWithTheFloor() {
		for (double t = -50; t < 200; t += 3.7) {
			double phase = heartbeatPhase(t);
			assertTrue(phase >= 0 && phase < 1);
			float pulse = heartbeat(t);
			assertTrue(pulse >= 0 && pulse <= 1.6F);
		}
	}

	@Test
	void everyCulminationRisesThenFadesOut() {
		assertEquals(ASCENSION_HEIGHT, ascensionHeight(ASCENSION_RISE_TICKS), 1e-9);
		assertEquals(1, ascensionFray(ASCENSION_TICKS), 1e-9);
		assertEquals(0, ascensionThread(MERGE_START_TICKS), 1e-9);
		assertEquals(1, ascensionThread(CULMINATION_TICKS), 1e-9);

		double floor = 6;
		assertTrue(groundingFront(GROUNDING_FRONT_TICKS, floor) > floor + 10, "the shock runs past the floor edge");
		assertEquals(0, fadeOut(GROUNDING_TICKS, GROUNDING_TICKS, 24), 1e-9);

		assertEquals(0, bindingSnap(BINDING_SNAP_TICKS), 1e-9);
		assertEquals(1, bindingSnap(BINDING_SNAP_TICKS + 10), 1e-9);

		assertEquals(FORGING_KNOT_HEIGHT, forgingKnotHeight(-1), 1e-9);
		assertTrue(forgingKnotHeight(FORGING_DROP_TICKS) < 0.2, "the knot lands on the Focus");
		assertEquals(0, forgingFlash(FORGING_DROP_TICKS - 1), 1e-9);
		assertEquals(1, forgingFlash(FORGING_DROP_TICKS), 1e-9);

		assertEquals(DOMAIN_RADIUS, domainRadius(DOMAIN_SWELL_TICKS), 1e-9);
		assertEquals(48.0, DOMAIN_RADIUS, 1e-9, "three chunks");
		assertEquals(0, domainSink(DOMAIN_SWELL_TICKS), 1e-9);
		assertEquals(1, domainSink(DOMAIN_TICKS), 1e-9);

		for (float total : new float[] { ASCENSION_TICKS, GROUNDING_TICKS, BINDING_TICKS, FORGING_TICKS,
				DOMAIN_TICKS, COLLAPSE_TICKS }) {
			assertTrue(total >= 40 && total <= 70, "culminations stay brief: " + total);
		}
	}

	@Test
	void hashIsStableAndInRange() {
		for (int i = -100; i < 100; i++) {
			double h = hash(i);
			assertTrue(h >= 0 && h < 1);
			assertEquals(h, hash(i));
		}
	}
}
