package com.vincenthuto.hemomancy.client.particle;

final class AbocipherParticleMotionRulesTest {
	public static void main(String[] args) {
		horizontalSwimCanCrossAChunkRoom();
		verticalDriftDoesNotDominateSwim();
	}

	private static void horizontalSwimCanCrossAChunkRoom() {
		double minimumTravel = AbocipherParticleMotionRules.MIN_SWIM_SPEED
				* AbocipherParticleMotionRules.MIN_LIFETIME_TICKS;

		assertTrue(minimumTravel >= 3.5D,
				"abocipher particles should have enough swim travel to cross several blocks");
	}

	private static void verticalDriftDoesNotDominateSwim() {
		assertTrue(AbocipherParticleMotionRules.UPWARD_DRIFT_SCALE <= 0.35D,
				"abocipher particles should swim laterally instead of mostly floating upward");
		assertTrue(AbocipherParticleMotionRules.VERTICAL_BOB_STRENGTH
						< AbocipherParticleMotionRules.MIN_SWIM_SPEED,
				"vertical bob should be smaller than horizontal swim speed");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
