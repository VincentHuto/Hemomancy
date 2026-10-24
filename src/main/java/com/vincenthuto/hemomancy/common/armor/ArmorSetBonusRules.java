package com.vincenthuto.hemomancy.common.armor;

public final class ArmorSetBonusRules {
	public static final int SEARING_CONTACT_FIRE_TICKS = 80;

	private ArmorSetBonusRules() {
	}

	public static boolean nextSearingContactState(boolean enabled) {
		return !enabled;
	}

	public static boolean searingContactActive(boolean enabled, boolean fullSheolicSet) {
		return enabled && fullSheolicSet;
	}

	public static double pelagicSwimMultiplier(boolean fullPrismaticSet, boolean fullPhantasmalSet) {
		return fullPrismaticSet || fullPhantasmalSet ? 1.35D : 1.0D;
	}
}
