package com.vincenthuto.hemomancy.common.worldgen.feature;

public final class ErythrocoralReefRules {
	public static final int MIN_WATER_DEPTH = 8;
	public static final int MIN_FLOOR_BELOW_SEA_LEVEL = 6;
	public static final int MAX_FLOOR_DELTA = 3;

	private ErythrocoralReefRules() {
	}

	public static boolean canUseFloor(boolean waterAtOrigin, boolean waterAbove, boolean sturdyFloor,
			boolean floorLiquid, int waterDepthAbove, int floorBlockY, int seaLevel) {
		return waterAtOrigin && waterAbove && sturdyFloor && !floorLiquid
				&& waterDepthAbove >= MIN_WATER_DEPTH
				&& (seaLevel - floorBlockY) >= MIN_FLOOR_BELOW_SEA_LEVEL;
	}

	public static boolean isInsideReefCluster(int dx, int dz, int radius) {
		return dx * dx + dz * dz <= radius * radius;
	}
}
