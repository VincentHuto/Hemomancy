package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingHeatRules {
	public static final int TEMPERATE = 0;
	public static final int WARM = 1;
	public static final int HOT = 2;
	public static final int EXTREME = 3;

	private MorphlingHeatRules() {
	}

	public static int environmentLevel(float temperature, boolean ultraWarm, boolean burning, boolean inLava) {
		if (ultraWarm || burning || inLava || temperature >= 2.0F) return EXTREME;
		if (temperature >= 1.5F) return HOT;
		return temperature >= 1.0F ? WARM : TEMPERATE;
	}

}
