package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HematicAcclimationRulesTest {
	private static final double EPSILON = 0.000001D;

	@Test
	void attenuationUsesTheApprovedExposureBands() {
		assertEquals(1.0D, HematicAcclimationRules.multiplier(749.999D), EPSILON);
		assertEquals(0.5D, HematicAcclimationRules.multiplier(750.0D), EPSILON);
		assertEquals(0.5D, HematicAcclimationRules.multiplier(1249.999D), EPSILON);
		assertEquals(0.25D, HematicAcclimationRules.multiplier(1250.0D), EPSILON);
		assertEquals(0.25D, HematicAcclimationRules.multiplier(1499.999D), EPSILON);
		assertEquals(0.0D, HematicAcclimationRules.multiplier(1500.0D), EPSILON);
	}

	@Test
	void linearDecayClearsFullExposureAfterFiveIdleMinutes() {
		assertEquals(1500.0D, HematicAcclimationRules.decayedExposure(1500.0D, 0L), EPSILON);
		assertEquals(1000.0D, HematicAcclimationRules.decayedExposure(1500.0D, 2000L), EPSILON);
		assertEquals(0.0D, HematicAcclimationRules.decayedExposure(1500.0D, 6000L), EPSILON);
		assertEquals(0.0D, HematicAcclimationRules.decayedExposure(1500.0D, 9000L), EPSILON);
	}

	@Test
	void stateTracksEntityTypesIndependentlyAndDecaysBeforeRecording() {
		PowerGuardrailState state = new PowerGuardrailState();
		state.recordHematicExposure("minecraft:cow", 1000.0D, 100L);
		state.recordHematicExposure("minecraft:zombie", 250.0D, 100L);

		assertEquals(500.0D, state.hematicExposure("minecraft:cow", 2100L), EPSILON);
		assertEquals(0.0D, state.hematicExposure("minecraft:zombie", 2100L), EPSILON);

		state.recordHematicExposure("minecraft:cow", 100.0D, 2100L);
		assertEquals(600.0D, state.hematicExposure("minecraft:cow", 2100L), EPSILON);
	}

	@Test
	void stateCapsExposureAndRoundTripsThroughAttachmentNbt() {
		PowerGuardrailState original = new PowerGuardrailState();
		original.recordHematicExposure("minecraft:cow", 2000.0D, 400L);
		original.recordHematicExposure("minecraft:zombie", 375.0D, 500L);

		PowerGuardrailState restored = new PowerGuardrailState();
		restored.deserializeNBT(null, original.serializeNBT(null));

		assertEquals(1500.0D, restored.hematicExposure("minecraft:cow", 400L), EPSILON);
		assertEquals(350.0D, restored.hematicExposure("minecraft:zombie", 600L), EPSILON);
	}
}
