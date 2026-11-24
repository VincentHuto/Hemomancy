package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AwakenedIchorianSigilFacingTest {

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
