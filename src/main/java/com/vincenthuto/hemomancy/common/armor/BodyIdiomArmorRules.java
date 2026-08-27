package com.vincenthuto.hemomancy.common.armor;

public final class BodyIdiomArmorRules {
	public static final int SEARING_CONTACT_FIRE_TICKS = 80;

	private BodyIdiomArmorRules() {
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
