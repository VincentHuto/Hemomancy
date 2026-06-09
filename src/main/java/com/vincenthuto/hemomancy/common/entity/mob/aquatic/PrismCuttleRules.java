package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class PrismCuttleRules {
	public static final double FLASH_RANGE_BLOCKS = 5.0D;
	public static final int FLASH_TICKS = 24;
	public static final int BLINDNESS_DURATION_TICKS = 60;
	public static final int NAUSEA_DURATION_TICKS = 80;
	public static final int FLASH_COOLDOWN_TICKS = 180;
	public static final int CAMOUFLAGE_SAMPLE_INTERVAL_TICKS = 40;
	public static final int MIN_DEPTH_BELOW_SEA_LEVEL = 8;
	public static final int PREFERRED_DEPTH_BELOW_SEA_LEVEL = 12;
	public static final int MAX_COMFORT_DEPTH_BELOW_SEA_LEVEL = 20;
	public static final double MAX_DOWNWARD_DEPTH_CORRECTION = -0.018D;
	public static final double MAX_UPWARD_DEPTH_CORRECTION = 0.006D;
	public static final int FACE_TENTACLE_COUNT = 8;
	public static final int FACE_TENTACLE_SEGMENT_COUNT = 5;
	public static final float TENTACLE_SEGMENT_LENGTH = 1.15F;
	public static final int FIN_SEGMENT_COUNT = 5;
	public static final float TENTACLE_REST_X_ROT = 0.14F;

	private PrismCuttleRules() {
	}

	public static boolean canNaturalSpawn(boolean waterHere, boolean openWater, int seaLevel, int y) {
		return waterHere && openWater && y <= seaLevel - MIN_DEPTH_BELOW_SEA_LEVEL;
	}

	public static boolean shouldFlash(boolean alive, boolean creative, boolean spectator, int cooldownTicks,
			double distance) {
		return alive && !creative && !spectator && cooldownTicks <= 0 && distance <= FLASH_RANGE_BLOCKS;
	}

	public static double verticalDepthCorrection(int seaLevel, double y) {
		double preferredY = seaLevel - PREFERRED_DEPTH_BELOW_SEA_LEVEL;
		double lowestComfortY = seaLevel - MAX_COMFORT_DEPTH_BELOW_SEA_LEVEL;
		if (y > preferredY) {
			return Math.max(MAX_DOWNWARD_DEPTH_CORRECTION, (preferredY - y) * 0.006D);
		}
		if (y < lowestComfortY) {
			return Math.min(MAX_UPWARD_DEPTH_CORRECTION, (lowestComfortY - y) * 0.002D);
		}
		return 0.0D;
	}
}
