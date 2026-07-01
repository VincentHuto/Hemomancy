package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class WildMorphlingRules {
	public static final int WILD_BOUND_MAX_MATURITY = 2;

	private WildMorphlingRules() {
	}

	public static int applyMaturityCap(int maturityLevel, boolean wildBound, boolean primalized) {
		if (!wildBound || primalized) {
			return maturityLevel;
		}
		return Math.min(maturityLevel, WILD_BOUND_MAX_MATURITY);
	}
}
