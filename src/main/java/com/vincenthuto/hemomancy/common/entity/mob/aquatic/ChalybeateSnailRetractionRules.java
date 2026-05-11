package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class ChalybeateSnailRetractionRules {
	static final int THREAT_HOLD_TICKS = 120;
	static final int INTERACTION_TUCK_TICKS = 360;
	static final int HARVEST_TUCK_TICKS = 480;
	static final int HURT_TUCK_TICKS = 260;

	private ChalybeateSnailRetractionRules() {
	}

	static int refreshForThreat(int currentTicks, boolean hasThreat) {
		return hasThreat ? Math.max(currentTicks, THREAT_HOLD_TICKS) : currentTicks;
	}

	static int tickDown(int currentTicks) {
		return Math.max(0, currentTicks - 1);
	}

	static boolean shouldRenderRetracted(boolean syncedFlag, int localTicks) {
		return syncedFlag || localTicks > 0;
	}
}
