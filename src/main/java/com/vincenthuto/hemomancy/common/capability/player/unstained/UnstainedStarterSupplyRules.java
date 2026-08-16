package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedStarterSupplyRules {
	private static final int REQUIRED_SOLUTIONS = 2;

	private UnstainedStarterSupplyRules() {}

	public static Grant grantFor(boolean alreadyClaimed, int carriedSolutions, boolean carriesGuide) {
		if (alreadyClaimed) return Grant.NONE;
		return new Grant(Math.max(0, REQUIRED_SOLUTIONS - carriedSolutions), !carriesGuide);
	}

	public record Grant(int solutionCount, boolean guide) {
		public static final Grant NONE = new Grant(0, false);
	}
}
