package com.vincenthuto.hemomancy.common.rite;

public final class MemoryBrazierAbsorptionRules {
	private MemoryBrazierAbsorptionRules() {
	}

	public static boolean shouldAttempt(boolean lit, boolean standardMemory, boolean channelingAbsorption,
			double maxAmount) {
		return lit && standardMemory && channelingAbsorption && maxAmount > 0.0D;
	}
}
