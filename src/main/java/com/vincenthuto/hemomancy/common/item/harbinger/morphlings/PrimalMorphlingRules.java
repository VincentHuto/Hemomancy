package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class PrimalMorphlingRules {
	public static final int APEX_LEVEL = 4;
	public static final int PRIMAL_LEVEL = 5;
	public static final int APOTHEOS_DEGREE = 8;

	private PrimalMorphlingRules() {
	}

	public static boolean isPrimalMaturity(float enzymePower, boolean primalized) {
		return primalized;
	}

	public static boolean canPrimalize(int maturityLevel, boolean primalized, int degreeNumber) {
		return maturityLevel == APEX_LEVEL && !primalized && degreeNumber >= APOTHEOS_DEGREE;
	}

	public static int morphicStrainDuration(int baseDurationTicks, int primalMorphogenesisLevel) {
		double multiplier = Math.max(0.55D, 1.0D - Math.max(0, primalMorphogenesisLevel) * 0.10D);
		return Math.max(1, (int) Math.round(baseDurationTicks * multiplier));
	}

	public static double primalBonusMultiplier(int primalMorphogenesisLevel) {
		return 1.0D + Math.max(0, primalMorphogenesisLevel) * 0.05D;
	}
}
