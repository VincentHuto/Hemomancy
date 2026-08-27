package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class EmberfangHeatRules {
	public static final int TEMPERATE = 0;
	public static final int WARM = 1;
	public static final int HOT = 2;
	public static final int EXTREME = 3;

	private EmberfangHeatRules() {
	}

	public static int environmentLevel(float temperature, boolean ultraWarm, boolean burning, boolean inLava) {
		if (ultraWarm || burning || inLava || temperature >= 2.0F) return EXTREME;
		if (temperature >= 1.5F) return HOT;
		return temperature >= 1.0F ? WARM : TEMPERATE;
	}

	public static double benefit(int level) {
		return switch (level) {
			case WARM -> 0.05D;
			case HOT -> 0.10D;
			case EXTREME -> 0.15D;
			default -> 0.0D;
		};
	}

	public static float exhaustionMultiplier(int level) {
		return switch (level) {
			case WARM -> 1.10F;
			case HOT -> 1.25F;
			case EXTREME -> 1.50F;
			default -> 1.0F;
		};
	}

	public static float incomingDamageMultiplier(int level) {
		return switch (level) {
			case HOT -> 1.05F;
			case EXTREME -> 1.10F;
			default -> 1.0F;
		};
	}
}
