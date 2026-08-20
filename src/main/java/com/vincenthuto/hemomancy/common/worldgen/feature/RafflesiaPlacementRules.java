package com.vincenthuto.hemomancy.common.worldgen.feature;

final class RafflesiaPlacementRules {
	private RafflesiaPlacementRules() {
	}

	static boolean isGroundedBase(boolean candidateIsLog, boolean belowIsLog, boolean belowIsSturdy) {
		return candidateIsLog && !belowIsLog && belowIsSturdy;
	}
}
