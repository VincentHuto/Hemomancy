package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AwakenedIchorianSigilFacingTest {

	@Test
	void stationaryMovementRetainsTheLastReadableFacing() {
		assertEquals(37.0F, AwakenedIchorianSigilFacing.update(37.0F, 0, 0, 0.25F));
	}

	@Test
	void movementFacesAlongMinecraftYawConventions() {
		assertEquals(-90.0F, AwakenedIchorianSigilFacing.update(0, 1, 0, 1), 0.001F);
		assertEquals(-180.0F, AwakenedIchorianSigilFacing.update(0, 0, -1, 1), 0.001F);
	}

	@Test
	void smoothingMovesOnlyTheConfiguredFraction() {
		assertEquals(-22.5F, AwakenedIchorianSigilFacing.update(0, 1, 0, 0.25F), 0.001F);
	}

	@Test
	void wraparoundTakesTheShortPath() {
		double radians = Math.toRadians(179.0D);
		float result = AwakenedIchorianSigilFacing.update(
				179.0F, Math.sin(radians), Math.cos(radians), 0.5F);

		assertEquals(180.0F, Math.abs(result), 0.001F);
	}

	@Test
	void threeDimensionalFacingPitchesIntoVerticalTravel() {
		var result = AwakenedIchorianSigilFacing.update(
				new AwakenedIchorianSigilFacing.Orientation(0.0F, 0.0F, 0.0F),
				0.0D, 1.0D, 1.0D, 1.0F);

		assertEquals(0.0F, result.yaw(), 0.001F);
		assertEquals(-45.0F, result.pitch(), 0.001F);
	}

	@Test
	void turningBodiesBankTowardTheirNewTravelDirection() {
		var result = AwakenedIchorianSigilFacing.update(
				new AwakenedIchorianSigilFacing.Orientation(0.0F, 0.0F, 0.0F),
				1.0D, 0.0D, 0.0D, 1.0F);

		assertEquals(-90.0F, result.yaw(), 0.001F);
		assertTrue(Math.abs(result.roll()) >= 20.0F);
	}

	@Test
	void authoredRigFrontIsCorrectedToTheTravelForwardAxis() {
		assertEquals(180.0F, Math.abs(
				AwakenedIchorianSigilFacing.authoredForwardCorrection(0.0D, -1.0D)), 0.001F);
		assertEquals(90.0F,
				AwakenedIchorianSigilFacing.authoredForwardCorrection(-1.0D, 0.0D), 0.001F);
		assertEquals(-90.0F,
				AwakenedIchorianSigilFacing.authoredForwardCorrection(1.0D, 0.0D), 0.001F);
	}

	@Test
	void minecraftYawIsConvertedToTheRendererRotationConvention() {
		assertEquals(90.0F, AwakenedIchorianSigilFacing.renderYaw(-90.0F), 0.001F);
		assertEquals(-90.0F, AwakenedIchorianSigilFacing.renderYaw(90.0F), 0.001F);
	}
}
