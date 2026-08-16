package com.vincenthuto.hemomancy.common.item.unstained;

public final class WhiteHumorCoatingRules {
	public static final int CHARGES_PER_FLASK = 24;
	private WhiteHumorCoatingRules() {}
	public static boolean isActive(int charges) { return charges > 0; }
	public static int afterHit(int charges) { return Math.max(0, charges - 1); }
}
