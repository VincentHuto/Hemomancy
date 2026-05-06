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
}
