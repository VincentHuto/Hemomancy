package com.vincenthuto.hemomancy.common.worldgen.feature;

public final class ErythrocoralReefRules {
	public static final int MIN_WATER_DEPTH = 4;
	public static final int MAX_FLOOR_DELTA = 3;

	private ErythrocoralReefRules() {
	}

	public static boolean canUseFloor(boolean waterAtOrigin, boolean waterAbove, boolean sturdyFloor,
			boolean floorLiquid, int waterDepthAbove) {
		return waterAtOrigin && waterAbove && sturdyFloor && !floorLiquid && waterDepthAbove >= MIN_WATER_DEPTH;
	}

	public static boolean isInsideReefCluster(int dx, int dz, int radius) {
		return dx * dx + dz * dz <= radius * radius;
	}
}
