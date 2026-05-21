package com.vincenthuto.hemomancy.common.item.harbinger;

public final class MonolithFragmentBurdenRules {
	public static final int BURDEN_CHECK_INTERVAL_TICKS = 80;
	public static final int BURDEN_EFFECT_TICKS = 100;

	private MonolithFragmentBurdenRules() {
	}

	public static boolean shouldBurdenCarrier(int degreeNumber) {
		return degreeNumber >= 8;
	}
}
