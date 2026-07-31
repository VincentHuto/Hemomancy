package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteRingTuningTest {
	@Test
	void plainArraysControlEachRingsAngleAndRadius() {
		double originalAngle = CardinalRiteRingTuning.ROTATION_DEGREES[1];
		double originalRadius = CardinalRiteRingTuning.RADIUS_BLOCKS[1];
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[1] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[1] = 5.0D;

			var anchor = CardinalRiteRingTuning.anchor(1, 0, 1);

			assertEquals(0, anchor.x());
			assertEquals(1, anchor.y());
			assertEquals(-5, anchor.z());
		} finally {
			CardinalRiteRingTuning.ROTATION_DEGREES[1] = originalAngle;
			CardinalRiteRingTuning.RADIUS_BLOCKS[1] = originalRadius;
		}
	}

	@Test
	void anchorOrderAdvancesClockwiseAroundTheSelectedRing() {
		double originalAngle = CardinalRiteRingTuning.ROTATION_DEGREES[0];
		double originalRadius = CardinalRiteRingTuning.RADIUS_BLOCKS[0];
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[0] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[0] = 2.0D;

			assertEquals(new CardinalRiteCeremonyDefinition.Anchor(0, 1, -2, 0, 0),
					CardinalRiteRingTuning.anchor(0, 0, 1));
			assertEquals(new CardinalRiteCeremonyDefinition.Anchor(2, 1, 0, 0, 1),
					CardinalRiteRingTuning.anchor(0, 1, 1));
		} finally {
			CardinalRiteRingTuning.ROTATION_DEGREES[0] = originalAngle;
			CardinalRiteRingTuning.RADIUS_BLOCKS[0] = originalRadius;
		}
	}

	@Test
	void generatedCardinalLayoutsUseTheSameLiveRingArrays() {
		double originalAngle = CardinalRiteRingTuning.ROTATION_DEGREES[2];
		double originalRadius = CardinalRiteRingTuning.RADIUS_BLOCKS[2];
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[2] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[2] = 6.0D;

			var anchors = CardinalRiteCeremonyDefinition.anchorsForLayout(
					3, 0, CardinalRiteCeremonyCatalog.Layout.CARDINAL);

			assertEquals(new CardinalRiteCeremonyDefinition.Anchor(0, 1, -6, 2, 0),
					anchors.get(8));
		} finally {
			CardinalRiteRingTuning.ROTATION_DEGREES[2] = originalAngle;
			CardinalRiteRingTuning.RADIUS_BLOCKS[2] = originalRadius;
		}
	}
}
