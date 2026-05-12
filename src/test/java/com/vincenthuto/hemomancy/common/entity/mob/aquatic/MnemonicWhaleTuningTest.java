package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class MnemonicWhaleTuningTest {
	public static void main(String[] args) {
		usesLargerSpermWhaleScale();
		usesThreePartBodyProfile();
		glidesSlowerThanADolphinButFastEnoughToCruise();
		keepsCruiseTargetsOffTheSeafloor();
		usesVerticalFlukeMotion();
	}

	private static void usesLargerSpermWhaleScale() {
		assertTrue(MnemonicWhaleTuning.HITBOX_WIDTH >= 4.0F,
				"mnemonic whale hitbox should read larger than the old 2.8-block body");
		assertTrue(MnemonicWhaleTuning.HITBOX_HEIGHT >= 1.6F,
				"mnemonic whale hitbox should have more vertical presence");
		assertTrue(MnemonicWhaleTuning.RENDER_SHADOW_RADIUS >= 1.6F,
				"larger whale should cast a larger renderer shadow");
	}

	private static void usesThreePartBodyProfile() {
		assertTrue(MnemonicWhaleTuning.MODEL_BODY_SEGMENTS >= 3,
				"sperm-whale profile should have head, body, and larger rear segment before the tail");
		assertTrue(MnemonicWhaleTuning.MODEL_TOTAL_LENGTH_UNITS >= 60,
				"mnemonic whale model should be substantially longer than the old 48-unit profile");
		assertTrue(MnemonicWhaleTuning.MODEL_HEAD_WIDTH_UNITS > MnemonicWhaleTuning.MODEL_MID_BODY_WIDTH_UNITS,
				"sperm-whale profile should have a heavier blocky head");
	}

	private static void glidesSlowerThanADolphinButFastEnoughToCruise() {
		assertTrue(MnemonicWhaleTuning.MOVEMENT_SPEED >= 0.4D,
				"mnemonic whales should have enough swim speed to avoid floor-hopping");
		assertTrue(MnemonicWhaleTuning.MOVEMENT_SPEED < 1.2D,
				"mnemonic whales should cruise slower than vanilla dolphins");
		assertTrue(MnemonicWhaleTuning.CRUISE_SPEED_MODIFIER >= 0.65D,
				"cruise goal should request a steady slow-dolphin pace");
	}

	private static void keepsCruiseTargetsOffTheSeafloor() {
		int targetY = MnemonicWhaleMovementRules.cruiseTargetY(63, 35, 3);
		assertTrue(targetY >= 35 + MnemonicWhaleTuning.CRUISE_MIN_FLOOR_CLEARANCE,
				"cruise targets should keep a large whale off the ocean floor");
		assertTrue(targetY <= 63 - MnemonicWhaleTuning.CRUISE_MIN_DEPTH_BELOW_SEA_LEVEL,
				"cruise targets should stay underwater instead of skimming the surface");

		int diveTargetY = MnemonicWhaleMovementRules.cruiseTargetY(63, 20,
				MnemonicWhaleTuning.CRUISE_RANDOM_DIVE_DEPTH);
		assertTrue(diveTargetY <= 63 - MnemonicWhaleTuning.CRUISE_BASE_DEPTH_BELOW_SEA_LEVEL,
				"some cruise targets should be true deep-water dives");

		double liftedMotion = MnemonicWhaleMovementRules.adjustVerticalMotion(-0.08D, false, false);
		assertTrue(liftedMotion > 0.0D, "whales touching the floor should lift instead of keep sinking");
	}

	private static void usesVerticalFlukeMotion() {
		assertTrue(MnemonicWhaleTuning.TAIL_FLUKE_VERTICAL_SWING_RADIANS >= 0.14F,
				"whale flukes should animate vertically like a dolphin/whale, not only side-to-side");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
