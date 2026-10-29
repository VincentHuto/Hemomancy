package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

public final class MonolithFragmentBurdenRules {
	public static final int BURDEN_CHECK_INTERVAL_TICKS = 80;
	public static final int BURDEN_EFFECT_TICKS = 100;

	private MonolithFragmentBurdenRules() {
	}

	/** The fragment weighs on the hive's heir, not on a will that cut itself free. */
	public static boolean shouldBurdenCarrier(EnumArchonPath path) {
		return path == EnumArchonPath.APOTHEOS;
	}
}
