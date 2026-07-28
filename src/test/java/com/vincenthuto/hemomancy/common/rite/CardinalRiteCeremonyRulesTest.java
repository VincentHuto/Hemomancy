package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public final class CardinalRiteCeremonyRulesTest {
	@Test
	void anchorCostsScaleByDegree() {
		assertEquals(4, CardinalRiteCeremonyRules.anchorCount(1), "degree one anchors");
		assertEquals(8, CardinalRiteCeremonyRules.anchorCount(2), "degree two anchors");
		assertEquals(32, CardinalRiteCeremonyRules.anchorCount(8), "degree eight anchors");
		assertEquals(200, CardinalRiteCeremonyRules.upfrontBloodCost(1), "degree one blood");
		assertEquals(1_600, CardinalRiteCeremonyRules.upfrontBloodCost(8), "degree eight blood");
	}

	@Test
	void wavesAndStillIntervalsScaleByForm() {
		assertEquals(2, CardinalRiteCeremonyRules.fullWaveCount(0), "minor waves");
		assertEquals(3, CardinalRiteCeremonyRules.fullWaveCount(1), "lesser waves");
		assertEquals(4, CardinalRiteCeremonyRules.fullWaveCount(2), "greater waves");
		assertEquals(6, CardinalRiteCeremonyRules.fullWaveCount(3), "grand waves");
		assertEquals(200, CardinalRiteCeremonyRules.stillIntervalTicks(0), "minor still interval");
		assertEquals(80, CardinalRiteCeremonyRules.stillIntervalTicks(3), "grand still interval");
	}

	@Test
	void onlyUpperDegreesReceiveAllies() {
		assertEquals(0, CardinalRiteCeremonyRules.allyQuota(4), "degree four quota");
		assertEquals(1, CardinalRiteCeremonyRules.allyQuota(5), "degree five quota");
		assertEquals(2, CardinalRiteCeremonyRules.allyQuota(6), "degree six quota");
		assertEquals(3, CardinalRiteCeremonyRules.allyQuota(7), "degree seven quota");
		assertEquals(3, CardinalRiteCeremonyRules.allyQuota(8), "degree eight quota");
	}

	@Test
	void instabilityBandsAreStableAndBounded() {
		assertEquals(CardinalRiteInstability.STABLE, CardinalRiteCeremonyRules.instabilityBand(0), "zero");
		assertEquals(CardinalRiteInstability.STABLE, CardinalRiteCeremonyRules.instabilityBand(39), "stable edge");
		assertEquals(CardinalRiteInstability.STRAINED, CardinalRiteCeremonyRules.instabilityBand(40), "strained");
		assertEquals(CardinalRiteInstability.RUPTURING, CardinalRiteCeremonyRules.instabilityBand(70), "rupturing");
		assertEquals(CardinalRiteInstability.COLLAPSED, CardinalRiteCeremonyRules.instabilityBand(100), "collapse");
	}

	@Test
	void professionFailureEscalatesByDegree() {
		assertEquals(CardinalRiteProfessionFailure.RETRY, CardinalRiteCeremonyRules.professionFailure(2), "early");
		assertEquals(CardinalRiteProfessionFailure.RECOVERY_WAVE, CardinalRiteCeremonyRules.professionFailure(4), "middle");
		assertEquals(CardinalRiteProfessionFailure.SEVERE_RECOVERY, CardinalRiteCeremonyRules.professionFailure(6), "high");
		assertEquals(CardinalRiteProfessionFailure.COLLAPSE, CardinalRiteCeremonyRules.professionFailure(7), "archon");
	}

	@Test
	void authoredLayoutsAlwaysCreateFourOrderedAnchorsPerRing() {
		for (CardinalRiteCeremonyCatalog.Layout layout : CardinalRiteCeremonyCatalog.Layout.values()) {
			var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(3, 1, layout);
			assertEquals(12, anchors.size(), layout + " anchor count");
			for (int index = 0; index < anchors.size(); index++) {
				assertEquals(index / 4, anchors.get(index).ring(), layout + " ring " + index);
				assertEquals(index % 4, anchors.get(index).order(), layout + " order " + index);
			}
		}
	}

	@Test
	void innermostGeneratedAnchorsMeetTheExpandedBoundaryOutsideAThreeByThreeAltar() {
		var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(
				1, 0, CardinalRiteCeremonyCatalog.Layout.CARDINAL);

		assertEquals(3, Math.abs(anchors.get(0).z()), "first ring radius");
	}

	@Test
	void successiveBoundaryRingsRotateTheirAnchorLocationsByFortyFiveDegrees() {
		var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(
				3, 0, CardinalRiteCeremonyCatalog.Layout.CARDINAL);

		assertEquals(0, anchors.get(0).x(), "inner ring first anchor x");
		assertEquals(-3, anchors.get(0).z(), "inner ring first anchor z");
		assertEquals(3, anchors.get(4).x(), "second ring first anchor x");
		assertEquals(-3, anchors.get(4).z(), "second ring first anchor z");
		assertEquals(5, anchors.get(8).x(), "third ring first anchor x");
		assertEquals(0, anchors.get(8).z(), "third ring first anchor z");
	}

	@Test
	void generatedAnchorCoordinatesNeverOverlapAcrossRings() {
		for (CardinalRiteCeremonyCatalog.Layout layout : CardinalRiteCeremonyCatalog.Layout.values()) {
			Set<String> occupied = new HashSet<>();
			for (var anchor : CardinalRiteCeremonyDefinition.anchorsForLayout(8, 0, layout)) {
				String coordinate = anchor.x() + "," + anchor.z();
				if (!occupied.add(coordinate)) {
					throw new AssertionError(layout + " generated duplicate anchor at " + coordinate);
				}
			}
		}
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
