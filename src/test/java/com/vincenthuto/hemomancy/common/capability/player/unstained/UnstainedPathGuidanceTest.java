package com.vincenthuto.hemomancy.common.capability.player.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnstainedPathGuidanceTest {
	@Test
	void nextRiteOnlyNamesActualRequiredProgressionRites() {
		assertEquals("lethean_baptism",
				UnstainedPathGuidance.nextRequiredRite(false, false, false));
		assertEquals("patient_purification",
				UnstainedPathGuidance.nextRequiredRite(true, false, false));
		assertEquals("clarity_ascension",
				UnstainedPathGuidance.nextRequiredRite(true, true, false));
		assertEquals("clarity_observances",
				UnstainedPathGuidance.nextRequiredRite(true, true, true));
	}

	@Test
	void targetScheduleIsMonotonicForBothTracksAndThreeStyles() {
		for (UnstainedPathGuidance.PlayStyle style : UnstainedPathGuidance.PlayStyle.values()) {
			assertTrue(UnstainedPathGuidance.estimatedPurityMinutes(style, 25)
					< UnstainedPathGuidance.estimatedPurityMinutes(style, 50));
			assertTrue(UnstainedPathGuidance.estimatedPurityMinutes(style, 50)
					< UnstainedPathGuidance.estimatedPurityMinutes(style, 75));
			assertTrue(UnstainedPathGuidance.estimatedPurityMinutes(style, 75)
					< UnstainedPathGuidance.estimatedPurityMinutes(style, 100));
			assertTrue(UnstainedPathGuidance.estimatedClarityMinutes(style, 25)
					< UnstainedPathGuidance.estimatedClarityMinutes(style, 100));
		}
	}
}
