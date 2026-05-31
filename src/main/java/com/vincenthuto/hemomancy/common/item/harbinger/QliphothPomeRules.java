package com.vincenthuto.hemomancy.common.item.harbinger;

public final class QliphothPomeRules {
	public static final long BASE_EMPOWERMENT_DURATION_TICKS = 3600L;
	public static final double BASE_EMPOWERMENT_COST_MULTIPLIER = 0.75D;

	private QliphothPomeRules() {
	}

	public static long empowermentDurationTicks(int qliphothGestationLevel) {
		return BASE_EMPOWERMENT_DURATION_TICKS + Math.max(0, qliphothGestationLevel) * 360L;
	}

	public static double empowermentCostMultiplier(int qliphothGestationLevel) {
		return Math.max(0.6D, BASE_EMPOWERMENT_COST_MULTIPLIER - Math.max(0, qliphothGestationLevel) * 0.03D);
	}
}
