package com.vincenthuto.hemomancy.common.block.harbinger.plant;

public final class InfestedWoodGrowthRules {

	public enum Growth {
		INFECTED_FUNGUS,
		HYPHAE,
		STINKHORN_FUNGUS
	}

	private InfestedWoodGrowthRules() {}

	public static boolean canGrow(boolean targetEmpty, int blockLight) {
		return targetEmpty && blockLight <= 7;
	}

	public static Growth select(int roll) {
		if (roll < 0 || roll >= 10) {
			throw new IllegalArgumentException("Growth roll must be between 0 and 9");
		}
		if (roll < 5) return Growth.INFECTED_FUNGUS;
		if (roll < 8) return Growth.HYPHAE;
		return Growth.STINKHORN_FUNGUS;
	}
}
