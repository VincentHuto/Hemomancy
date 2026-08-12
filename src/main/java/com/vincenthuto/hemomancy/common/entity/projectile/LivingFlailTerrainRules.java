package com.vincenthuto.hemomancy.common.entity.projectile;

public final class LivingFlailTerrainRules {
	private LivingFlailTerrainRules() {
	}

	public static boolean mayFreezeWater(boolean sourceWater, boolean waterBlock, boolean hasBlockEntity,
			boolean protectionAllowed) {
		return sourceWater && waterBlock && !hasBlockEntity && protectionAllowed;
	}

	public static boolean mayPlaceSnow(boolean sturdySupport, boolean replaceableTarget, boolean snowTarget,
			int snowLayers, boolean hasBlockEntity, boolean protectionAllowed) {
		return sturdySupport && !hasBlockEntity && protectionAllowed
				&& (replaceableTarget || snowTarget && snowLayers < 8);
	}

	public static int nextSnowLayers(int currentLayers) {
		return Math.min(8, Math.max(0, currentLayers) + 1);
	}
}
