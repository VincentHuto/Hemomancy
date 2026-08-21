package com.vincenthuto.hemomancy.common.event;

public final class BloodInfusionRules {
	public static final int COLLAPSE_TICKS = 30;

	private BloodInfusionRules() {
	}

	public static boolean isValidCost(double cost) {
		return Double.isFinite(cost) && cost > 0.0D;
	}

	public static boolean canComplete(boolean inputStillMatches, boolean hasBlockEntity) {
		return inputStillMatches && !hasBlockEntity;
	}
}
