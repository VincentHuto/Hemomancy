package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public final class TerrestrialSpeculumRules {
	public static final int REQUIRED_DEGREE = 2;

	private TerrestrialSpeculumRules() {
	}

	public static boolean canManifest(int degree, boolean bloodActive, int claimedVeins, boolean replaceable) {
		return degree >= REQUIRED_DEGREE && bloodActive && claimedVeins > 0 && replaceable;
	}
}
