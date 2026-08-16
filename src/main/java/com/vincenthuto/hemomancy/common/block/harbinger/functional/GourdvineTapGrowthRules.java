package com.vincenthuto.hemomancy.common.block.harbinger.functional;

public final class GourdvineTapGrowthRules {
	private GourdvineTapGrowthRules() {
	}

	public static int advance(int currentStage, int growthBoost) {
		if (growthBoost <= 0) {
			return currentStage;
		}
		return Math.min(GourdvineTapBlock.MAX_STAGE, currentStage + growthBoost);
	}
}
