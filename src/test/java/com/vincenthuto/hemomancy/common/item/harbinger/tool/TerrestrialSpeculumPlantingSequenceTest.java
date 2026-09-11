package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrestrialSpeculumPlantingSequenceTest {
	@Test
	void manifestationWaitsForThePlantingImpact() {
		long startedAt = 200L;

		assertFalse(TerrestrialSpeculumPlantingSequence.shouldManifest(startedAt, 213L));
		assertTrue(TerrestrialSpeculumPlantingSequence.shouldManifest(startedAt, 214L));
	}

	@Test
	void plantingOwnsThePlayerUntilTheSharedAnimationFinishes() {
		long startedAt = 200L;

		assertTrue(TerrestrialSpeculumPlantingSequence.isActive(startedAt, 221L));
		assertFalse(TerrestrialSpeculumPlantingSequence.isActive(startedAt, 222L));
	}

	@Test
	void recoveryHaulsTheRootedSpeculumUpward() {
		assertTrue(TerrestrialSpeculumPlantingSequence.extractionLift(0.75F) > 0.25F);
	}
}
