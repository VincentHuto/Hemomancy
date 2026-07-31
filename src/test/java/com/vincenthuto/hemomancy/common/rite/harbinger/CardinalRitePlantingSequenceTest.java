package com.vincenthuto.hemomancy.common.rite.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class CardinalRitePlantingSequenceTest {

	@Test
	void staffDoesNotBecomePlantedBeforeImpact() {
		assertFalse(CardinalRitePlantingSequence.isPlanted(0));
		assertFalse(CardinalRitePlantingSequence.isPlanted(13));
		assertTrue(CardinalRitePlantingSequence.isPlanted(14));
	}

	@Test
	void sequenceEndsAfterRecoveryFrame() {
		assertTrue(CardinalRitePlantingSequence.isAnimating(0));
		assertTrue(CardinalRitePlantingSequence.isAnimating(21));
		assertFalse(CardinalRitePlantingSequence.isAnimating(22));
	}

	@Test
	void strikeProgressAcceleratesIntoImpactAndThenSettles() {
		assertEquals(0.0F, CardinalRitePlantingSequence.strikeProgress(0.0F), 0.0001F);
		assertTrue(CardinalRitePlantingSequence.strikeProgress(10.0F)
				< CardinalRitePlantingSequence.strikeProgress(13.0F));
		assertEquals(1.0F, CardinalRitePlantingSequence.strikeProgress(14.0F), 0.0001F);
		assertEquals(1.0F, CardinalRitePlantingSequence.strikeProgress(21.0F), 0.0001F);
	}

	@Test
	void cameraShakeIsSmallAndDampsToZero() {
		float initial = Math.abs(CardinalRitePlantingSequence.cameraPitchShake(14.0F));
		float later = Math.abs(CardinalRitePlantingSequence.cameraPitchShake(18.0F));

		assertTrue(initial <= 1.25F);
		assertTrue(later < initial);
		assertEquals(0.0F, CardinalRitePlantingSequence.cameraPitchShake(22.0F), 0.0001F);
	}
}
