package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class MnemonicWhaleMovementRules {
	private MnemonicWhaleMovementRules() {
	}

	public static int cruiseTargetY(int seaLevel, int floorY, int diveOffset) {
		int ceiling = seaLevel - MnemonicWhaleTuning.CRUISE_MIN_DEPTH_BELOW_SEA_LEVEL;
		int floorClearanceY = floorY + MnemonicWhaleTuning.CRUISE_MIN_FLOOR_CLEARANCE;
		int preferredY = seaLevel - MnemonicWhaleTuning.CRUISE_BASE_DEPTH_BELOW_SEA_LEVEL
				- clamp(diveOffset, 0, MnemonicWhaleTuning.CRUISE_RANDOM_DIVE_DEPTH);
		if (floorClearanceY > ceiling) {
			return Math.max(floorY + 1, ceiling);
		}
		return clamp(preferredY, floorClearanceY, ceiling);
	}

	public static double adjustVerticalMotion(double currentYMotion, boolean tooCloseToSurface, boolean hasWaterBelow) {
		double yMotion = currentYMotion;
		if (tooCloseToSurface) {
			yMotion = Math.max(yMotion - MnemonicWhaleTuning.SHALLOW_WATER_DOWNWARD_ACCELERATION,
					-MnemonicWhaleTuning.MAX_SHALLOW_DIVE_SPEED);
		}
		if (!hasWaterBelow) {
			yMotion = Math.max(yMotion, MnemonicWhaleTuning.FLOOR_LIFT_SPEED);
		}
		return yMotion;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
