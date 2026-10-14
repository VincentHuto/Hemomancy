package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardinalRiteCompletionRingTheatricsTest {
	@Test
	void offeringProcessionRaisesOuterRingsIntoTwoBlockFunnel() {
		var inner = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.OFFERING_PROCESSION, 30.0F,
				0, 4, 2.0F);
		var outer = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.OFFERING_PROCESSION, 30.0F,
				3, 4, 8.0F);

		assertEquals(0.0F, inner.verticalOffset(), 0.0001F);
		assertEquals(2.0F, outer.verticalOffset(), 0.0001F);
		assertEquals(1.0F, outer.radialScale(), 0.0001F);
		assertTrue(Math.abs(inner.rotationRadians()) > 0.01F);
		assertTrue(inner.rotationRadians() * outer.rotationRadians() < 0.0F,
				"successive rings counter-rotate");
	}

	@Test
	void daemonGrowthContractsRingsToThreeBlockDiameterAndStacksThem() {
		var inner = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.CULMINATION, 20.0F,
				0, 4, 2.0F);
		var outer = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.CULMINATION, 20.0F,
				3, 4, 8.0F);

		assertEquals(0.75F, inner.radialScale(), 0.0001F);
		assertEquals(0.1875F, outer.radialScale(), 0.0001F);
		assertEquals(0.45F, inner.verticalOffset(), 0.0001F);
		assertEquals(2.10F, outer.verticalOffset(), 0.0001F);
	}

	@Test
	void ringsCollapseAndFlashImmediatelyBeforeFlight() {
		var collapsing = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.CULMINATION, 36.0F,
				2, 4, 6.0F);
		var launch = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.CULMINATION, 40.0F,
				2, 4, 6.0F);

		assertTrue(collapsing.radialScale() > 0.0F);
		assertTrue(collapsing.radialScale() < 0.25F);
		assertTrue(CardinalRiteCompletionRingTheatrics.flashAlpha(38.0F) > 0.0F);
		assertEquals(0.0F, launch.radialScale(), 0.0001F);
		assertEquals(0.0F, CardinalRiteCompletionRingTheatrics.flashAlpha(40.0F), 0.0001F);
	}

	@Test
	void unrelatedAndTerminatedPhasesUseDefaultRingPose() {
		var ordeal = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.ORDEAL, 200.0F,
				2, 4, 6.0F);
		var collapsed = CardinalRiteCompletionRingTheatrics.pose(CardinalRitePhase.COLLAPSED, 1.0F,
				2, 4, 6.0F);

		assertEquals(CardinalRiteCompletionRingTheatrics.RingPose.DEFAULT, ordeal);
		assertEquals(CardinalRiteCompletionRingTheatrics.RingPose.DEFAULT, collapsed);
	}

	@Test
	void worldAnimationTimeKeepsRotationContinuousAcrossPhaseTransition() {
		var procession = CardinalRiteCompletionRingTheatrics.pose(
				CardinalRitePhase.OFFERING_PROCESSION, 90.0F, 500.0F, 1, 4, 4.0F);
		var culmination = CardinalRiteCompletionRingTheatrics.pose(
				CardinalRitePhase.CULMINATION, 0.0F, 500.0F, 1, 4, 4.0F);

		assertEquals(procession.rotationRadians(), culmination.rotationRadians(), 0.0001F);
	}

	@Test
	void processionSpinStartsFromRestAndAcceleratesSmoothly() {
		assertEquals(0.0F, CardinalRiteCompletionRingTheatrics.spinAngle(0.0F), 0.0001F);
		float early = CardinalRiteCompletionRingTheatrics.spinAngle(1.0F);
		float later = CardinalRiteCompletionRingTheatrics.spinAngle(60.0F);
		assertTrue(early < 0.01F, "the first tick barely turns the rings: " + early);
		float steadyStep = CardinalRiteCompletionRingTheatrics.spinAngle(61.0F) - later;
		assertEquals(0.018F, steadyStep, 0.0001F);
	}

	@Test
	void socketsFoldAwayAsCulminationContractsTheRings() {
		assertEquals(1.0F, CardinalRiteCompletionRingTheatrics.socketVisibility(
				CardinalRitePhase.OFFERING_PROCESSION, 80.0F), 0.0001F);
		assertEquals(1.0F, CardinalRiteCompletionRingTheatrics.socketVisibility(
				CardinalRitePhase.CULMINATION, 0.0F), 0.0001F);
		assertEquals(0.0F, CardinalRiteCompletionRingTheatrics.socketVisibility(
				CardinalRitePhase.CULMINATION, 12.0F), 0.0001F);
	}
}
