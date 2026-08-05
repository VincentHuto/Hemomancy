package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public final class CardinalRiteCeremonyRulesTest {
	@Test
	void authoredHelperRequirementIsARealStartGate() {
		assertFalse(CardinalRiteAllyService.hasRequiredHelperCount(0, 1));
		assertTrue(CardinalRiteAllyService.hasRequiredHelperCount(1, 1));
		assertTrue(CardinalRiteAllyService.hasRequiredHelperCount(0, 0));
	}

	@Test
	void authoredFailureProfilesProtectEarlyRitesFromPunitiveCollapse() {
		assertEquals(0.0F, CardinalRiteCeremonyRules.collapseDamage("safe_retry"),
				"safe retry damage");
		assertEquals(0, CardinalRiteCeremonyRules.fragileBlocksOnCollapse("offering_loss"),
				"offering loss fragile blocks");
		assertEquals(1, CardinalRiteCeremonyRules.fragileBlocksOnCollapse("fragile_damage"),
				"fragile damage blocks");
		assertEquals(3, CardinalRiteCeremonyRules.fragileBlocksOnCollapse("collapse"),
				"collapse fragile blocks");
	}

	@Test
	void anchorCostsFollowTheProgressiveDegreeCeiling() {
		assertEquals(4, CardinalRiteCeremonyRules.anchorCount(1), "degree one anchors");
		assertEquals(4, CardinalRiteCeremonyRules.anchorCount(2), "degree two anchors");
		assertEquals(4, CardinalRiteCeremonyRules.anchorCount(3), "degree three anchors");
		assertEquals(4, CardinalRiteCeremonyRules.anchorCount(4), "degree four anchors");
		assertEquals(8, CardinalRiteCeremonyRules.anchorCount(5), "degree five anchors");
		assertEquals(12, CardinalRiteCeremonyRules.anchorCount(6), "degree six anchors");
		assertEquals(12, CardinalRiteCeremonyRules.anchorCount(7), "degree seven anchors");
		assertEquals(200, CardinalRiteCeremonyRules.upfrontBloodCost(1), "degree one blood");
		assertEquals(200, CardinalRiteCeremonyRules.upfrontBloodCost(4), "degree four blood");
		assertEquals(400, CardinalRiteCeremonyRules.upfrontBloodCost(5), "degree five blood");
		assertEquals(600, CardinalRiteCeremonyRules.upfrontBloodCost(7), "degree seven blood");
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
		assertEquals(0, CardinalRiteCeremonyRules.allyQuota(5), "degree five quota");
		assertEquals(1, CardinalRiteCeremonyRules.allyQuota(6), "degree six quota");
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
	void innermostGeneratedAnchorsAreInsetOneBlockFromTheFormerBoundary() {
		double originalAngle = CardinalRiteRingTuning.ROTATION_DEGREES[0];
		double originalRadius = CardinalRiteRingTuning.RADIUS_BLOCKS[0];
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[0] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[0] = 2.0D;

			var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(
					1, 0, CardinalRiteCeremonyCatalog.Layout.CARDINAL);

			assertEquals(2, Math.abs(anchors.get(0).z()), "first ring radius");
		} finally {
			CardinalRiteRingTuning.ROTATION_DEGREES[0] = originalAngle;
			CardinalRiteRingTuning.RADIUS_BLOCKS[0] = originalRadius;
		}
	}

	@Test
	void secondBoundaryRingCanBeTunedClearOfThePillarCorners() {
		double[] originalAngles = CardinalRiteRingTuning.ROTATION_DEGREES.clone();
		double[] originalRadii = CardinalRiteRingTuning.RADIUS_BLOCKS.clone();
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[0] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[0] = 2.0D;
			CardinalRiteRingTuning.ROTATION_DEGREES[1] = 67.5D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[1] = Math.sqrt(10.0D);
			CardinalRiteRingTuning.ROTATION_DEGREES[2] = 90.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[2] = 4.0D;

			var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(
					3, 0, CardinalRiteCeremonyCatalog.Layout.CARDINAL);

			assertEquals(0, anchors.get(0).x(), "inner ring first anchor x");
			assertEquals(-2, anchors.get(0).z(), "inner ring first anchor z");
			assertEquals(3, anchors.get(4).x(), "second ring first anchor x");
			assertEquals(-1, anchors.get(4).z(), "second ring first anchor z");
			for (int index = 4; index < 8; index++) {
				var anchor = anchors.get(index);
				assertFalse(Math.abs(anchor.x()) == 2 && Math.abs(anchor.z()) == 2);
			}
			assertEquals(4, anchors.get(8).x(), "third ring first anchor x");
			assertEquals(0, anchors.get(8).z(), "third ring first anchor z");
		} finally {
			System.arraycopy(originalAngles, 0, CardinalRiteRingTuning.ROTATION_DEGREES,
					0, originalAngles.length);
			System.arraycopy(originalRadii, 0, CardinalRiteRingTuning.RADIUS_BLOCKS,
					0, originalRadii.length);
		}
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

	private static void assertTrue(boolean value) {
		if (!value) throw new AssertionError("expected true");
	}

	private static void assertFalse(boolean value) {
		if (value) throw new AssertionError("expected false");
	}
}
