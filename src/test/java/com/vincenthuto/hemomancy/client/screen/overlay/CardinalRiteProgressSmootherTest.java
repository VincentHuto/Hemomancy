package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteProgressSmootherTest {
	@Test
	void authoritativeProgressStepsAreEasedWithoutOvershooting() {
		CardinalRiteProgressSmoother smoother = new CardinalRiteProgressSmoother();
		assertEquals(0.25D, smoother.update(7L, 0.25D, 0.0D), 0.0001D);

		double firstFrame = smoother.update(7L, 0.75D, 0.05D);
		assertTrue(firstFrame > 0.25D && firstFrame < 0.75D, "first frame should move without jumping");

		double laterFrame = firstFrame;
		for (int frame = 0; frame < 120; frame++) {
			laterFrame = smoother.update(7L, 0.75D, 1.0D / 60.0D);
		}
		assertTrue(laterFrame > firstFrame, "displayed progress should remain monotonic");
		assertTrue(laterFrame <= 0.75D, "displayed progress must not overshoot the server");
	}

	@Test
	void switchingToAnotherRiteResetsInsteadOfAnimatingFromStaleProgress() {
		CardinalRiteProgressSmoother smoother = new CardinalRiteProgressSmoother();
		smoother.update(7L, 0.80D, 0.0D);

		assertEquals(0.10D, smoother.update(8L, 0.10D, 0.016D), 0.0001D);
	}
}
